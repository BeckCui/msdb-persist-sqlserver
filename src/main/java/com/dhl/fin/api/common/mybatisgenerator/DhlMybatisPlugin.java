package com.dhl.fin.api.common.mybatisgenerator;

import com.dhl.fin.api.common.util.ArrayUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.*;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Created by CuiJianbo on 2020.02.13.
 */
public class DhlMybatisPlugin extends PluginAdapter {

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        sqlMap.setMergeable(false);
        return super.sqlMapGenerated(sqlMap, introspectedTable);
    }

    @SneakyThrows
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {


        String[] domainNameConfig = introspectedTable.getTableConfiguration().getDomainObjectName().split("\\.");
        String domainPackage = "com.dhl.fin.api.domain." + domainNameConfig[0];
        document.getRootElement().getElements().forEach(p -> {
            XmlElement element = (XmlElement) p;
            String idValue = element.getAttributes().stream()
                    .filter(item -> item.getName().equals("id"))
                    .map(item -> item.getValue())
                    .findFirst().get();
            switch (element.getName()) {
                case "sql":
                    changeSqlElement(document, element, domainPackage);
                    break;
                case "resultMap":
                    changeResultMap(document, element, domainPackage);
                    break;
                case "select":
                    changeSelectElement(element, domainPackage);
                    break;
                case "insert":
                    if (idValue.equals("insert")) {
                        changeInsertElement(element);
                    } else if (idValue.equals("insertSelective")) {
                        changeInsertSelectiveElement(element);
                    }
                    changeDomainType(element, domainPackage);
                    break;
                case "update":
                    if (idValue.equals("updateByPrimaryKey")) {
                        changeUpdateElement(element);
                    } else if (idValue.equals("updateByPrimaryKeySelective")) {
                        changeUpdateSelectiveElement(element);
                    }
                    changeDomainType(element, domainPackage);
                    break;
            }
        });

        addSelectionCommonSql(document, domainPackage);
        addSelectPageQuery(document, domainPackage);
        addSelectSelective(document);
        addSelectCount(document);
        changeDeleteElement(document);
        insertManyToMany(document, domainPackage);
        addSelectOne(document);

        //更换 root 的 namespace
        int index = -1;
        for (int j = 0; j < document.getRootElement().getAttributes().size(); j++) {
            if (document.getRootElement().getAttributes().get(j).getName().equals("namespace")) {
                index = j;
                break;
            }
        }
        if (index > -1) {
            document.getRootElement().getAttributes().remove(index);
            document.getRootElement().getAttributes().add(new Attribute("namespace", introspectedTable.getMyBatis3JavaMapperType()));
        }


        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }


    public void changeDomainType(XmlElement element, String domainPackage) {
        int index = -1;
        for (int j = 0; j < element.getAttributes().size(); j++) {
            if (element.getAttributes().get(j).getName().equals("parameterType")) {
                index = j;
                break;
            }
        }
        if (index > -1) {
            element.getAttributes().remove(index);
            element.getAttributes().add(new Attribute("parameterType", domainPackage));
        }
        XmlElement selectKeyElement = element.getElements().stream()
                .filter(item -> item instanceof XmlElement)
                .map(item -> (XmlElement) item)
                .filter(e -> e.getName().equals("selectKey"))
                .findFirst().orElse(null);
        if (ObjectUtil.notNull(selectKeyElement)) {
            List<VisitableElement> children = selectKeyElement.getElements();
            if (((TextElement) children.get(0)).getContent().contains("SCOPE_IDENTITY")) {
                TextElement textElement = new TextElement(" SELECT @@IDENTITY");
                children.clear();
                children.add(textElement);
            }
        }
//        element.getAttributes().add(new Attribute("useGeneratedKeys", "true"));
//        element.getAttributes().add(new Attribute("keyProperty", "id"));
    }


    private void changeSqlElement(Document document, XmlElement element, String domainPackage) {

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "joinMap != null"));

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("item", "domain"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("collection", "joinMap"));

        getJoinFiled(document, domainPackage).stream().forEach(p -> {
            String lineJoinDomain = StringUtil.toUnderlineCase(p);
            XmlElement ifElement1 = new XmlElement("if");
            ifElement1.addAttribute(new Attribute("test", "domain.tableAlia == '" + lineJoinDomain + "'"));
            String fieldsString = Arrays.stream(ObjectUtil.loadClass(domainPackage).getDeclaredFields())
                    .filter(k -> StringUtil.toUnderlineCase(k.getName()).equals(p))
                    .map(k ->
                            {
                                Class joinDomainClass;
                                if (k.getType().equals(List.class)) {
                                    joinDomainClass = ((Class) (((ParameterizedType) (k.getGenericType())).getActualTypeArguments()[0]));
                                } else {
                                    joinDomainClass = k.getType();
                                }
                                return ArrayUtil.addAll(joinDomainClass.getDeclaredFields(), joinDomainClass.getSuperclass().getDeclaredFields());
                            }

                    )
                    .flatMap(Arrays::stream)
                    .filter(k -> (k.getName().equals("id")) || (!k.getType().equals(List.class) && k.getAnnotation(Transient.class) == null && (k.getType().getSuperclass() == null || !k.getType().getSuperclass().getSimpleName().equals("BasicDomain"))))
                    .map(k -> StringUtil.join(lineJoinDomain, ".", StringUtil.toUnderlineCase(k.getName()), " as ", lineJoinDomain, "_", StringUtil.toUnderlineCase(k.getName())))
                    .collect(joining(","));
            ifElement1.addElement(new TextElement(StringUtil.join(",", fieldsString)));
            foreachElement.addElement(ifElement1);
        });


        String domainName = getSimpleDomainName(domainPackage);
        List<TextElement> textElements = element.getElements().stream()
                .filter(p -> p instanceof TextElement)
                .map(p -> ((TextElement) p).getContent().split(","))
                .map(p -> Arrays.stream(p).map(k -> StringUtils.isEmpty(k.trim()) ? "," : StringUtil.isEmpty(k.trim()) ? "," : StringUtil.join(StringUtil.toUnderlineCase(domainName), ".", k.trim(), " as ", StringUtil.toUnderlineCase(domainName), "_", k.trim())).collect(joining(",")).replaceAll(",,", ","))
                .map(TextElement::new)
                .collect(Collectors.toList());

        element.getElements().clear();
        element.getElements().addAll(textElements);

        ifElement.addElement(foreachElement);
        element.addElement(ifElement);
    }

    private void changeSelectElement(XmlElement element, String domainPackage) {

        int index = -1;
        for (int j = 0; j < element.getAttributes().size(); j++) {
            if (element.getAttributes().get(j).getName().equals("parameterType")) {
                index = j;
                break;
            }
        }
        if (index > -1) {
            element.getAttributes().remove(index);
        }

        String domainAliasName = StringUtil.toUnderlineCase(getSimpleDomainName(domainPackage));

        XmlElement ifElement = getJoinSql(domainAliasName);

        element.getElements().add(3, ifElement);

        String newFrom = ((TextElement) element.getElements().get(2)).getContent() + " " + domainAliasName;
        element.getElements().remove(2);
        element.getElements().add(2, new TextElement(newFrom));

        String newWhere = ((TextElement) element.getElements().get(4)).getContent().replaceAll("id = #", String.format("%s.id = #", domainAliasName));
        element.getElements().remove(4);
        element.getElements().add(4, new TextElement(newWhere));

    }


    private void changeResultMap(Document document, XmlElement element, String domainPackage) {
        int index = -1;
        if (element.getName().equals("resultMap")) {
            for (int j = 0; j < element.getAttributes().size(); j++) {
                if (element.getAttributes().get(j).getName().equals("type")) {
                    index = j;
                    break;
                }
            }
            if (index > -1) {
                element.getAttributes().remove(index);
                element.getAttributes().add(new Attribute("type", domainPackage));
            }
            element.getElements().clear();
            element.getElements().addAll(getResultMapElement(document, domainPackage));
        }

    }

    private List<XmlElement> getResultMapElement(Document document, String domainPackage) {
        List<XmlElement> elements = new LinkedList<XmlElement>();

        try {
            String mainDomainName = StringUtil.toUnderlineCase(getSimpleDomainName(domainPackage));
            XmlElement idElement = new XmlElement("id");
            idElement.addAttribute(new Attribute("property", "id"));
            idElement.addAttribute(new Attribute("column", mainDomainName + "_id"));
            elements.add(idElement);
            elements.addAll(getAllResultElement(domainPackage, mainDomainName));


            //添加association
            Class domainClass = ClassLoader.getSystemClassLoader().loadClass(domainPackage);
            getJoinFiled(document, domainPackage).forEach(p -> {
                String domainField = StringUtil.toCamelCase(p);
                String joinType = null;

                joinType = Arrays.stream(domainClass.getDeclaredFields())
                        .filter(q -> q.getName().equals(domainField))
                        .map(q -> q.getType().getTypeName())
                        .findFirst().orElse("");
                if (!joinType.endsWith(".List")) {
                    if (!StringUtil.isEmpty(joinType)) {
                        XmlElement associationElement = new XmlElement("association");
                        associationElement.addAttribute(new Attribute("property", domainField));
                        associationElement.addAttribute(new Attribute("javaType", joinType));
                        XmlElement idElement1 = new XmlElement("id");
                        idElement1.addAttribute(new Attribute("property", "id"));
                        idElement1.addAttribute(new Attribute("column", StringUtil.join(mainDomainName, "_", p, "_id")));
                        associationElement.addElement(idElement1);
                        associationElement.getElements().addAll(getAllResultElement(joinType, domainField));

                        elements.add(associationElement);
                    }
                }
            });


            //添加collection
            Arrays.stream(domainClass.getDeclaredFields())
                    .filter(q -> q.getAnnotation(ManyToMany.class) != null || q.getAnnotation(OneToMany.class) != null)
                    .forEach(p -> {
                        Class joinDomainClass = ((Class) (((ParameterizedType) (p.getGenericType())).getActualTypeArguments()[0]));
                        String joinDomainSimpleName = getSimpleDomainName(joinDomainClass.getSimpleName());
                        String joinDomainName = StringUtil.toUnderlineCase(joinDomainSimpleName);

                        XmlElement associationElement = new XmlElement("collection");
                        associationElement.addAttribute(new Attribute("property", p.getName()));
                        associationElement.addAttribute(new Attribute("ofType", joinDomainClass.getName()));
                        XmlElement idElement1 = new XmlElement("id");
                        idElement1.addAttribute(new Attribute("property", "id"));
                        idElement1.addAttribute(new Attribute("column", StringUtil.join(StringUtil.toUnderlineCase(p.getName()), "_id")));
                        associationElement.addElement(idElement1);
                        associationElement.getElements().addAll(getAllResultElement(joinDomainClass.getTypeName(), p.getName()));

                        elements.add(associationElement);
                    });

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return elements;
    }


    public String getSimpleDomainName(String packageName) {
        String[] pack = packageName.split("\\.");
        String packageDomainName = pack[pack.length - 1];
        return StringUtil.join(packageDomainName.substring(0, 1).toLowerCase(), packageDomainName.substring(1));
    }

    public List<String> getJoinFiled(Document document, String domainPackage) {
        List<String> list1 = null;
        try {
            Class domainClass = ClassLoader.getSystemClassLoader().loadClass(domainPackage);
            List<String> list2 = Arrays.stream(domainClass.getDeclaredFields())
                    .filter(q -> q.getAnnotation(ManyToMany.class) != null || q.getAnnotation(OneToMany.class) != null)
                    .map(Field::getName)
                    .map(StringUtil::toUnderlineCase)
                    .collect(Collectors.toList());

            list1 = Arrays.stream(document.getRootElement().getElements()
                    .stream()
                    .filter(p -> ((XmlElement) p).getName().equals("sql"))
                    .map(p -> ((XmlElement) p).getElements()).findFirst().get()
                    .stream()
                    .map(p -> ((TextElement) p).getContent()).collect(joining())
                    .split(","))
                    .filter(p -> p.endsWith("_id"))
                    .map(p -> p.replaceAll("_id", "").trim())
                    .collect(Collectors.toList());

            list1.addAll(list2);
            return list1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list1;
    }

    public List<XmlElement> getAllResultElement(String domainPackage, String joinDomainName) {
        List<XmlElement> elements = new LinkedList<>();
        try {
            Class joinDomainClass = ClassLoader.getSystemClassLoader().loadClass(domainPackage);
            Field[] fields = ArrayUtil.addAll(joinDomainClass.getDeclaredFields(), joinDomainClass.getSuperclass().getDeclaredFields());
            Arrays.stream(fields)
                    .filter(p ->
                            p.getAnnotation(Transient.class) == null &&
                                    !p.getType().equals(List.class)
                                    && (p.getType().getSuperclass() == null || !p.getType().getSuperclass().getSimpleName().equals("BasicDomain"))
                                    && !p.getName().equals("id")
                    )
                    .forEach(f -> {
                        XmlElement resultElement = new XmlElement("result");
                        resultElement.addAttribute(new Attribute("property", f.getName()));
                        resultElement.addAttribute(new Attribute("column", StringUtil.toUnderlineCase(joinDomainName) + "_" + StringUtil.toUnderlineCase(f.getName())));
                        elements.add(resultElement);
                    });


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return elements;
    }

    private void changeInsertElement(XmlElement element) {

        Map<Integer, String> fields = new HashMap();

        for (int i = 0; i < element.getElements().size(); i++) {
            VisitableElement visitableElement = element.getElements().get(i);
            if (visitableElement instanceof TextElement && ((TextElement) visitableElement).getContent().contains("Id,")) {
                TextElement t = ((TextElement) visitableElement);
                String newContent = Arrays.stream(t.getContent().trim().split(", "))
                        .map(item -> {
                            if (item.contains("Id,")) {
                                return item.replaceAll("#\\{.*,jdbcType=BIGINT}", "#{" + StringUtil.toCamelCase(item.substring(2, item.indexOf(",") - 2)) + ".id,jdbcType=BIGINT}");
                            } else {
                                return item;
                            }
                        }).collect(joining(","));
                fields.put(i, newContent);
            }
        }

        fields.entrySet().stream().forEach(p -> {
            element.getElements().remove(p.getKey().intValue());
            element.getElements().add(p.getKey(), new TextElement(p.getValue()));
        });


    }

    private void changeInsertSelectiveElement(XmlElement element) {
        List<String> newTestList = element.getElements().stream()
                .filter(p -> p instanceof XmlElement && ((XmlElement) p).getName().equals("trim"))
                .map(p -> (XmlElement) p)
                .findFirst().get().getElements().stream()
                .map(p -> (XmlElement) p)
                .map(p -> {
                    String testStr = p.getAttributes().stream().filter(m -> m.getName().equals("test")).map(m -> m.getValue()).findFirst().get();
                    if (testStr.contains("Id != null")) {
                        String joinDomian = testStr.replaceAll("Id != null", "");
                        return String.format("%s != null and %s.id != null", joinDomian, joinDomian);
                    } else {
                        return null;
                    }
                }).collect(Collectors.toList());

        List<XmlElement> ss = element.getElements()
                .stream()
                .filter(p -> p instanceof XmlElement && ((XmlElement) p).getName().equals("trim"))
                .map(p -> (XmlElement) p)
                .collect(Collectors.toList());

        for (int i = 0; i < newTestList.size(); i++) {
            if (StringUtil.isEmpty(newTestList.get(i))) {
                continue;
            }
            int j = i;
            ss.stream().forEach(p -> {
                List<VisitableElement> elements = p.getElements();
                XmlElement oldElement = (XmlElement) elements.remove(j);
                XmlElement newXmlElement = new XmlElement("if");
                newXmlElement.addAttribute(new Attribute("test", newTestList.get(j)));
                String oldContent = oldElement.getElements().stream().map(k -> ((TextElement) k).getContent()).findFirst().get();
                String newContent;
                if (oldContent.contains("Id,")) {
                    String domain = oldContent.substring(oldContent.indexOf("#{") + 2, oldContent.indexOf("Id"));
                    newContent = oldContent.replaceAll("#\\{" + domain + "Id", "#{" + domain + ".id");
                } else {
                    newContent = oldContent;
                }
                newXmlElement.addElement(new TextElement(newContent));
                elements.add(j, newXmlElement);
            });
        }
    }


    private void changeUpdateSelectiveElement(XmlElement element) {

        element.getAttributes().remove(1);
        List<String> newTestList = element.getElements().stream()
                .filter(p -> p instanceof XmlElement && ((XmlElement) p).getName().equals("set"))
                .map(p -> (XmlElement) p)
                .findFirst().get().getElements().stream()
                .map(p -> (XmlElement) p)
                .map(p -> {
                    String testStr = p.getAttributes().stream().filter(m -> m.getName().equals("test")).map(m -> m.getValue()).findFirst().get();
                    if (testStr.contains("Id != null")) {
                        String joinDomian = testStr.replaceAll("Id != null", "");
                        return String.format("%s != null and %s.id != null", joinDomian, joinDomian);
                    } else {
                        return null;
                    }
                }).collect(Collectors.toList());


        XmlElement ss = element.getElements()
                .stream()
                .filter(p -> p instanceof XmlElement && ((XmlElement) p).getName().equals("set"))
                .map(p -> (XmlElement) p)
                .findFirst().get();

        List<VisitableElement> elements = ss.getElements();
        for (int i = 0; i < newTestList.size(); i++) {
            if (StringUtil.isEmpty(newTestList.get(i))) {
                continue;
            }

            XmlElement oldElement = (XmlElement) elements.remove(i);
            XmlElement newXmlElement = new XmlElement("if");
            newXmlElement.addAttribute(new Attribute("test", newTestList.get(i)));

            String oldContent = oldElement.getElements().stream().map(k -> ((TextElement) k).getContent()).findFirst().get();
            String domain = oldContent.substring(oldContent.indexOf("= #{") + 4, oldContent.indexOf("Id"));
            String newDomainId = domain + ".id";
            newXmlElement.addElement(new TextElement(oldContent.replaceAll("id = #\\{" + domain + "Id", "id = #{" + newDomainId)));
            elements.add(i, newXmlElement);
        }


        //添加domain.前缀
        List<VisitableElement> newSetXml = new LinkedList<>();
        VisitableElement setXml = element.getElements().stream().filter(item -> item instanceof XmlElement && ((XmlElement) item).getName().equals("set")).findFirst().get();

        ((XmlElement) setXml).getElements().forEach(item -> {
            String testCondition = ((XmlElement) item).getAttributes().stream().filter(m -> m.getName().equals("test")).map(m -> m.getValue()).findFirst().get();
            String testStr = Arrays.stream(testCondition.split("and")).map(p -> "domain." + p.trim() + " ").collect(joining(" and "));
            String contextStr = ((TextElement) ((XmlElement) item).getElements().get(0)).getContent().replaceAll("#\\{", "#{domain.");

            XmlElement newXmlElement = new XmlElement("if");
            newXmlElement.addAttribute(new Attribute("test", testStr));
            newXmlElement.addElement(new TextElement(contextStr));
            newSetXml.add(newXmlElement);
        });

        ((XmlElement) setXml).getElements().clear();
        ((XmlElement) setXml).getElements().addAll(newSetXml);

        //添加where 语句
        XmlElement trimElement = new XmlElement("trim");
        trimElement.addAttribute(new Attribute("prefix", "where"));
        trimElement.addAttribute(new Attribute("suffixOverrides", "and"));
        XmlElement forElement = new XmlElement("foreach");
        forElement.addAttribute(new Attribute("collection", "query.whereCondition"));
        forElement.addAttribute(new Attribute("item", "condition"));
        TextElement textElement = new TextElement(" ${condition}");

        forElement.addElement(textElement);
        trimElement.addElement(forElement);

        element.getElements().remove(element.getElements().size() - 1);
        element.getElements().add(trimElement);

    }

    private void changeUpdateElement(XmlElement element) {

        Map<Integer, String> fields = new HashMap();

        for (int i = 0; i < element.getElements().size(); i++) {
            TextElement textElement = (TextElement) element.getElements().get(i);
            String oldContent = textElement.getContent();
            if (oldContent.contains("Id,")) {
                String domain = oldContent.substring(oldContent.indexOf("= #{") + 4, oldContent.indexOf("Id"));
                fields.put(i, oldContent.replaceAll("id = #\\{" + domain + "Id", "id = #{" + domain + ".id"));
            }
        }

        fields.entrySet().stream().forEach(p -> {
            element.getElements().remove(p.getKey().intValue());
            element.getElements().add(p.getKey(), new TextElement(p.getValue()));
        });


    }

    private void addSelectionCommonSql(Document document, String domainPackage) {

        XmlElement selectEle = document.getRootElement().getElements().stream().map(p -> (XmlElement) p).filter(p -> p.getName().equals("select")).findFirst().get();

        TextElement topOneText = new TextElement("select  <if test='selectOne'>top 1</if>");
        selectEle.getElements().set(0, topOneText);

        XmlElement sqlElement = new XmlElement("sql");
        sqlElement.addAttribute(new Attribute("id", "select_sql"));
        sqlElement.getElements().addAll(selectEle.getElements());
        sqlElement.getElements().remove(sqlElement.getElements().size() - 1);


        sqlElement.addElement(new TextElement("where 1=1 "));

        //添加whereCondition
        sqlElement.addElement(getWhereSql());

        //添加orderCondition
        sqlElement.addElement(getOrderSql());


        document.getRootElement().getElements().add(2, sqlElement);


    }

    private XmlElement getOrderSql() {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "orderCondition != null"));

        XmlElement trimElement = new XmlElement("trim");
        trimElement.addAttribute(new Attribute("prefix", "order by"));
        trimElement.addAttribute(new Attribute("suffixOverrides", ","));

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "orderCondition"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("item", "condition"));
        TextElement conditionElement = new TextElement(" ${condition},");

        ifElement.addElement(trimElement);
        trimElement.addElement(foreachElement);
        foreachElement.addElement(conditionElement);

        return ifElement;
    }

    private XmlElement getJoinSql(String domainAliasName) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "joinMap != null"));

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("item", "domain"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("collection", "joinMap"));

        XmlElement ifSingleElement = new XmlElement("if");
        ifSingleElement.addAttribute(new Attribute("test", "!domain.isM2M and !domain.isO2M"));
        ifSingleElement.addElement(new TextElement("left join t_${domain.tableName} ${domain.tableAlia} on " + domainAliasName + ".${domain.tableAlia}_id = ${domain.tableAlia}.id and ${domain.tableAlia}.remove = 0"));

        XmlElement ifO2MElement = new XmlElement("if");
        ifO2MElement.addAttribute(new Attribute("test", "domain.isO2M"));
        ifO2MElement.addElement(new TextElement("left join t_${domain.tableName} ${domain.tableAlia} on " + domainAliasName + ".id = ${domain.tableAlia}.${domain.joinField}_id and ${domain.tableAlia}.remove = 0"));

        XmlElement ifNotSingleElement = new XmlElement("if");
        ifNotSingleElement.addAttribute(new Attribute("test", "domain.isM2M"));
        ifNotSingleElement.addElement(new TextElement("left join ${domain.middleTable} ${domain.middleTable}_alis on " + domainAliasName + ".id=${domain.middleTable}_alis." + domainAliasName + "_id left join t_${domain.tableName} ${domain.tableAlia} on ${domain.tableAlia}.id = ${domain.middleTable}_alis.${domain.invertJoinId} and ${domain.tableAlia}.remove = 0"));

        foreachElement.addElement(ifSingleElement);
        foreachElement.addElement(ifO2MElement);
        foreachElement.addElement(ifNotSingleElement);

        ifElement.addElement(foreachElement);

        return ifElement;
    }

    private XmlElement getWhereSql() {

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "whereCondition != null"));


        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "whereCondition"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("item", "condition"));
        TextElement conditionElement = new TextElement("and ${condition}");


        ifElement.addElement(foreachElement);
        foreachElement.addElement(conditionElement);

        return ifElement;
    }

    private void addSelectPageQuery(Document document, String domainPackage) throws ClassNotFoundException {
        String domainSimpleName = getSimpleDomainName(domainPackage);
        domainSimpleName = StringUtil.toUnderlineCase(domainSimpleName);

        XmlElement selectElement = new XmlElement("select");
        selectElement.addAttribute(new Attribute("id", "selectPageQuery"));
        selectElement.addAttribute(new Attribute("resultMap", "BaseResultMap"));

        TextElement withElement = new TextElement(String.format("with dd as (select distinct ${orderFields} from t_%s %s ", domainSimpleName, domainSimpleName));
        selectElement.addElement(withElement);
        selectElement.addElement(getJoinSql(domainSimpleName));
        selectElement.addElement(new TextElement("where 1 = 1 "));
        selectElement.addElement(getWhereSql());
        selectElement.addElement(getOrderSql());
        selectElement.addElement(new TextElement("offset ${startIndex} rows fetch next ${length} rows only)"));

        selectElement.addElement(new TextElement("select <include refid=\"Base_Column_List\" />"));
        selectElement.addElement(new TextElement("from t_" + domainSimpleName + " " + domainSimpleName));
        selectElement.addElement(getJoinSql(domainSimpleName));

        selectElement.addElement(new TextElement(String.format("where exists(select 1 from dd where dd.id = %s.id)", domainSimpleName)));
        selectElement.addElement(getOrderSql());

        document.getRootElement().getElements().add(4, selectElement);

    }

    private void addSelectSelective(Document document) {
        XmlElement selectElement = new XmlElement("select");
        selectElement.addAttribute(new Attribute("id", "selectSelective"));
        selectElement.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid", "select_sql"));
        selectElement.addElement(includeElement);
        document.getRootElement().getElements().add(5, selectElement);
    }

    private void addSelectCount(Document document) {
        XmlElement selectEle = document.getRootElement().getElements().stream()
                .map(p -> (XmlElement) p)
                .filter(p -> p.getName().equals("sql"))
                .filter(p -> p.getAttributes().get(0).getValue().equalsIgnoreCase("select_sql"))
                .findFirst().get();

        XmlElement selectElement = new XmlElement("select");
        selectElement.addAttribute(new Attribute("id", "selectCount"));
        selectElement.addAttribute(new Attribute("resultType", "java.lang.Integer"));
        selectElement.addElement(new TextElement("select count(1) total from ("));

        List<VisitableElement> elements = selectEle.getElements();
        List<VisitableElement> countElements = new LinkedList<>();
        for (int n = 0; n < elements.size(); n++) {
            if (n == 1) {
                countElements.add(new TextElement(" distinct ${fields}"));
            } else {
                countElements.add(elements.get(n));
            }
        }
        selectElement.getElements().addAll(countElements);
        selectElement.addElement(new TextElement(") as s"));
        document.getRootElement().getElements().add(6, selectElement);
    }

    private void changeDeleteElement(Document document) {

        int index = -1;
        List<VisitableElement> elements = document.getRootElement().getElements();
        for (int i = 0; i < elements.size(); i++) {
            if (((XmlElement) elements.get(i)).getName().equals("delete")) {
                index = i;
                break;
            }
        }

        String tableName = ((TextElement) (((XmlElement) elements.get(index)).getElements().get(0))).getContent().trim().split(" ")[2];
        XmlElement updateElement = new XmlElement("update");
        updateElement.addAttribute(new Attribute("id", "deleteByPrimaryKey"));
        updateElement.getElements().add(new TextElement("  update " + tableName + " set remove = 1 , update_time=#{updateTime} , update_user=#{updateUser} where id = #{id,jdbcType=BIGINT}"));

        if (index > -1) {
            elements.remove(index);
            elements.add(index, updateElement);
        }
    }

    private void insertManyToMany(Document document, String domainPackage) {
        try {
            Class domainClass = ClassLoader.getSystemClassLoader().loadClass(domainPackage);
            Arrays.stream(domainClass.getDeclaredFields())
                    .filter(q -> q.getAnnotation(ManyToMany.class) != null)
                    .map(q -> {
                        String tableName = q.getDeclaredAnnotation(JoinTable.class).name();
                        Class joinDomainClass = ((Class) (((ParameterizedType) (q.getGenericType())).getActualTypeArguments()[0]));
                        String joinDomainSimpleName = getSimpleDomainName(joinDomainClass.getSimpleName());
                        String joinDomainName = StringUtil.toUnderlineCase(joinDomainSimpleName);
                        String joinFieldName = StringUtil.upperFirst(q.getName());
                        String domainSimpleName = getSimpleDomainName(domainPackage);
                        String domainName = StringUtil.toUnderlineCase(domainSimpleName);
                        String joinId = Arrays.stream(q.getDeclaredAnnotation(JoinTable.class).joinColumns()).map(p -> p.name()).findFirst().get();
                        String invertJoinId = Arrays.stream(q.getDeclaredAnnotation(JoinTable.class).inverseJoinColumns()).map(p -> p.name()).findFirst().get();
                        String domainIdName = StringUtil.toCamelCase(joinId);
                        String joinDomainIdName = StringUtil.toCamelCase(invertJoinId);
                        XmlElement insertElement = new XmlElement("insert");
                        insertElement.addAttribute(new Attribute("id", "insert" + domainClass.getSimpleName() + joinFieldName));
                        TextElement textElement1 = new TextElement(String.format("insert into %s(%s,%s) values(#{%s},#{%s})",
                                tableName, joinId, invertJoinId, domainIdName, joinDomainIdName));

                        XmlElement deleteElement = new XmlElement("delete");
                        deleteElement.addAttribute(new Attribute("id", "delete" + domainClass.getSimpleName() + joinFieldName));
                        TextElement textElement = new TextElement(String.format("delete from %s where %s=#{%s} and %s=#{%s};",
                                tableName, joinId, domainIdName, invertJoinId, joinDomainIdName));


                        XmlElement deleteMiddleElement = new XmlElement("delete");
                        deleteMiddleElement.addAttribute(new Attribute("id", "delete" + domainClass.getSimpleName() + joinFieldName + "Middle"));
                        TextElement textElement2 = new TextElement(String.format("delete from %s where %s=#{%s}", tableName, joinId, domainIdName));

                        deleteElement.addElement(textElement);
                        insertElement.addElement(textElement1);
                        deleteMiddleElement.addElement(textElement2);
                        List<XmlElement> elementList = new LinkedList<>();
                        elementList.add(insertElement);
                        elementList.add(deleteElement);
                        elementList.add(deleteMiddleElement);
                        return elementList;
                    })
                    .filter(ObjectUtil::notNull)
                    .flatMap(List::stream)
                    .forEach(p -> {
                        document.getRootElement().getElements().add(p);
                    });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addSelectOne(Document document) {
        XmlElement selectElement = new XmlElement("select");
        selectElement.addAttribute(new Attribute("id", "selectOne"));
        selectElement.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        selectElement.addElement(new TextElement("<include refid=\"select_sql\"/>"));
        document.getRootElement().getElements().add(selectElement);
    }
}
