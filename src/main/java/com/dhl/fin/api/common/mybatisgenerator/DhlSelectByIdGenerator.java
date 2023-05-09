package com.dhl.fin.api.common.mybatisgenerator;

import cn.hutool.core.util.ReflectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectByPrimaryKeyMethodGenerator;

import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by CuiJianbo on 2020.02.15.
 */
public class DhlSelectByIdGenerator extends SelectByPrimaryKeyMethodGenerator {

    private boolean isSimple;

    public DhlSelectByIdGenerator(boolean isSimple) {
        super(isSimple);
        this.isSimple = isSimple;
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Method method = new Method(introspectedTable.getSelectByPrimaryKeyStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(getDomainType());
        method.setReturnType(returnType);

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        importedTypes.add(returnType);
        importedTypes.add(new FullyQualifiedJavaType("java.util.Map"));
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));
        importedTypes.add(new FullyQualifiedJavaType("QueryDto"));

        if (!isSimple && introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                    introspectedTable.getPrimaryKeyType());
            importedTypes.add(type);
            method.addParameter(new Parameter(type, "key")); //$NON-NLS-1$
        } else {
            List<IntrospectedColumn> introspectedColumns = introspectedTable
                    .getPrimaryKeyColumns();
            boolean annotate = introspectedColumns.size() > 1;
            if (annotate) {
                importedTypes.add(new FullyQualifiedJavaType(
                        "org.apache.ibatis.annotations.Param")); //$NON-NLS-1$
            }
            Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("Map"), "data");
            method.addParameter(parameter1);
        }

        addMapperAnnotations(interfaze, method);

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);


        addSelectPageQueryMethod(interfaze);
        addSelectMethod(interfaze);
        addSelectCountMethod(interfaze);
        addSelectOneMethod(interfaze);
        addInsertMiddleMethod(interfaze, importedTypes);

        if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(
                method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }


    }

    public void addSelectPageQueryMethod(Interface interfaze) {
        Method method = new Method("selectPageQuery");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(String.format("List<%s>", getDomainType()));
        method.setReturnType(returnType);

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("QueryDto"), "queryDto");
        method.addParameter(parameter1);

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addExtraImports(interfaze);
        interfaze.addMethod(method);

    }

    public void addSelectMethod(Interface interfaze) {
        Method method = new Method("selectSelective");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(String.format("List<%s>", getDomainType()));
        method.setReturnType(returnType);

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("QueryDto"), "queryDto");
        method.addParameter(parameter1);

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addExtraImports(interfaze);
        interfaze.addMethod(method);
    }

    public void addSelectOneMethod(Interface interfaze) {
        Method method = new Method("selectOne");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(getDomainType());
        method.setReturnType(returnType);

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("QueryDto"), "queryDto");
        method.addParameter(parameter1);

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addExtraImports(interfaze);
        interfaze.addMethod(method);
    }

    public void addSelectCountMethod(Interface interfaze) {
        Method method = new Method("selectCount");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("int");
        method.setReturnType(returnType);

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("QueryDto"), "queryDto");
        method.addParameter(parameter1);

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addExtraImports(interfaze);
        interfaze.addMethod(method);
    }

    public String getDomainType() {
        String domainName = introspectedTable.getTableConfiguration().getDomainObjectName();
        return "com.dhl.fin.api.domain." + domainName;
    }

    public void addInsertMiddleMethod(Interface interfaze, Set<FullyQualifiedJavaType> importedTypes) {
        Boolean flag[] = new Boolean[]{false};
        try {
            Class domainClass = ClassLoader.getSystemClassLoader().loadClass(getDomainType());
            Arrays.stream(domainClass.getDeclaredFields())
                    .filter(q -> q.getAnnotation(ManyToMany.class) != null)
                    .forEach(p -> {
                        flag[0] = true;
                        String joinFieldName = p.getName();
                        Class joinDomainClass = ((Class) (((ParameterizedType) (p.getGenericType())).getActualTypeArguments()[0]));
                        addInsertMiddle(interfaze, domainClass, joinFieldName, joinDomainClass, "delete");
                        addInsertMiddle(interfaze, domainClass, joinFieldName, joinDomainClass, "insert");
                        deleteMiddle(interfaze, domainClass, joinFieldName);
                    });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (flag[0]) {
            importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        }

    }

    private void addInsertMiddle(Interface interfaze, Class domainClass, String joinFieldName, Class joinDomainClass, String action) {

        String joinId = Arrays.stream(ReflectUtil.getField(domainClass, joinFieldName).getDeclaredAnnotation(JoinTable.class).joinColumns()).map(p -> p.name()).findFirst().get();
        String invertJoinId = Arrays.stream(ReflectUtil.getField(domainClass, joinFieldName).getDeclaredAnnotation(JoinTable.class).inverseJoinColumns()).map(p -> p.name()).findFirst().get();

        String methodName = action + domainClass.getSimpleName() + StringUtil.upperFirst(joinFieldName);
        String domainIdName = StringUtil.toCamelCase(joinId);
        String joinDomainIdName = StringUtil.toCamelCase(invertJoinId);
        Method method = new Method(methodName);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("int");
        method.setReturnType(returnType);

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("@Param(\"" + domainIdName + "\")Long"), domainIdName);
        method.addParameter(parameter1);

        Parameter parameter2 = new Parameter(new FullyQualifiedJavaType("@Param(\"" + joinDomainIdName + "\")Long"), joinDomainIdName);
        method.addParameter(parameter2);

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addExtraImports(interfaze);
        interfaze.addMethod(method);
    }

    private void deleteMiddle(Interface interfaze, Class domainClass, String joinFieldName) {

        String methodName = "delete" + domainClass.getSimpleName() + StringUtil.upperFirst(joinFieldName) + "Middle";
        String domainIdName = toLowerCaseFirstOne(domainClass.getSimpleName()) + "Id";
        Method method = new Method(methodName);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("int");
        method.setReturnType(returnType);

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("@Param(\"" + domainIdName + "\")Long"), domainIdName);
        method.addParameter(parameter1);

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addExtraImports(interfaze);
        interfaze.addMethod(method);
    }

    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

}
