package com.dhl.fin.api.common.service;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.dhl.fin.api.common.annotation.BusinessId;
import com.dhl.fin.api.common.annotation.Excel;
import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.dto.UserInfo;
import com.dhl.fin.api.common.enums.*;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.excel.ExcelSheet;
import com.dhl.fin.api.common.util.excel.ExcelSheetBuilder;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Created by CuiJianbo on 2020.02.20.
 */

@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CommonService<T extends BasicDomain> {

    /**
     * 判断对于每次请求是  add，update，delete,query
     */
    public ActionEnum actionEnum;
    private Object proxyDao;
    private Class<T> domainClass;
    private String tableName;


    @Value("${spring.profiles.active}")
    private String profiles;

    @Value("${custom.uploadPath}")
    private String uploadPath;

    @Value("${custom.projectCode}")
    private String projectCode;


    @Autowired
    private RedisService redisService;

    @Autowired
    private LogService logService;


    public CommonService() {
        instanceInitData();
    }

    public CommonService(Class<T> domainClass) {
        this.domainClass = domainClass;
        instanceInitData();
    }

    /*****************************查询************************************/

    /**
     * 面向数据库的查询
     *
     * @param queryDto
     * @return
     * @throws Exception
     */
    public List<T> select(QueryDto queryDto) throws Exception {
        addJoinMap(queryDto);
        addIsRemove(queryDto);
        return (List<T>) proxyDao.getClass().getDeclaredMethod("selectSelective", QueryDto.class).invoke(proxyDao, queryDto);
    }

    /**
     * 面向controller层的查询，保存业务逻辑
     *
     * @return
     * @throws Exception
     */
    public List<T> selectDomain() throws Exception {
        this.actionEnum = ActionEnum.LIST;
        QueryDto queryDto = parseRequestFilterParams();
        queryDto.setRemove(false);
        addJoinMap(queryDto);
        beforeList(queryDto);
        List<T> datas = select(queryDto);
        afterList(datas);
        return datas;
    }


    /**
     * 分页查询
     *
     * @param pageIndex
     * @param length
     * @return
     * @throws Exception
     */
    public Map pageQueryDomain(int pageIndex, Integer length) throws Exception {
        this.actionEnum = ActionEnum.PAGE;
        QueryDto queryCountDto = parseRequestFilterParams();
        queryCountDto.setRemove(false);
        QueryDto queryDto = getPageQueryDto();
        queryDto.setLength(length);
        queryDto.setStartIndex((pageIndex > 0 ? pageIndex - 1 : 0) * length);
        queryDto.setRemove(false);
        queryCountDto.getWhereCondition().addAll(queryDto.getWhereCondition());
        queryCountDto.getJoinDomain().addAll(queryDto.getJoinDomain());
        queryCountDto.setJoinDomain(queryCountDto.getJoinDomain().stream().distinct().collect(Collectors.toList()));
        int total = count(queryCountDto);
        List<T> datas = selectPageQuery(queryDto);
        afterPageQuery(datas);

        formatPageData(datas);

        if (profiles.equals(EnvType.DEV.getType())) {
            log.info(String.format("%s到%s数据(不分页的总数:%s): %s", actionEnum.getName(), domainClass.getSimpleName(), total, JSON.toJSON(datas)));
        }
        return MapUtil.builder().add("total", total).add("datas", datas).build();
    }


    /**
     * 根据Ids列表查询domain List
     *
     * @param ids
     * @param domainClass
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T extends BasicDomain> List<T> findDomainListByIds(List<Long> ids, Class<T> domainClass) throws Exception {
        if (CollectorUtil.isNoTEmpty(ids)) {
            String treeIdStr = CollectorUtil.join(ids, ",");
            String domainTableAlis = StringUtil.toUnderlineCase(StringUtil.lowerFirst(domainClass.getSimpleName()));
            QueryDto queryDto = QueryDto.builder()
                    .available()
                    .addWhere(String.format("%s.id in (%s)", domainTableAlis, treeIdStr))
                    .build();
            CommonService domainService = SpringContextUtil.getServiceImplByDomain(domainClass);
            return domainService.select(queryDto);
        } else {
            return null;
        }
    }


    /**
     * 根据查询条件获取Domain
     *
     * @param queryDto
     * @return
     * @throws Exception
     */
    public T get(QueryDto queryDto) throws Exception {
        addJoinMap(queryDto);
        addIsRemove(queryDto);
        queryDto.setSelectOne(true);
        return (T) proxyDao.getClass().getDeclaredMethod("selectOne", QueryDto.class).invoke(proxyDao, queryDto);
    }


    /**
     * 根据id获取Domain
     *
     * @param id
     * @param joinDomain
     * @return
     * @throws Exception
     */
    public T get(Long id, String... joinDomain) throws Exception {
        QueryDto queryDto = new QueryDto();
        if (ObjectUtil.notNull(joinDomain)) {
            queryDto.setJoinDomain(Arrays.asList(joinDomain));
        }
        addJoinMap(queryDto);
        Map params = MapUtil.builder()
                .add("id", id)
                .addWhen("joinMap", queryDto.getJoinMap(), CollectorUtil.isNoTEmpty(queryDto.getJoinMap()))
                .build();
        return selectById(params);
    }


    /**
     * 关联joinDomain获取Domain
     *
     * @param id
     * @param joinDomain
     * @return
     * @throws Exception
     */
    public T getDomain(Long id, String... joinDomain) throws Exception {
        this.actionEnum = ActionEnum.GET_DOMAIN;
        T domain = domainClass.newInstance();
        if (id > 0) {
            domain = get(id, joinDomain);
        }

        dateFieldFormat(domain);
        afterGet(domain);
        return domain;
    }


    /**
     * 统计查询数量
     *
     * @param queryDto
     * @return
     * @throws Exception
     */
    public int count(QueryDto queryDto) throws Exception {
        addJoinMap(queryDto);
        addIsRemove(queryDto);
        addFields(queryDto);
        queryDto.getOrderCondition().clear();
        return (Integer) proxyDao.getClass().getDeclaredMethod("selectCount", QueryDto.class).invoke(proxyDao, queryDto);
    }

    /*****************************新增，更新************************************/
    /**
     * 面向controller层的，带有业务逻辑的save
     *
     * @param domain
     * @param cascadeUpdate
     * @return
     * @throws Exception
     */
    public String saveDomain(T domain, String... cascadeUpdate) throws Exception {
        if (ObjectUtil.isNull(domain.getId())) {
            this.actionEnum = ActionEnum.ADD;
        } else {
            this.actionEnum = ActionEnum.UPDATE;
        }

        String validateMessage = validate(domain);

        if (StringUtil.isNotEmpty(validateMessage)) {
            return validateMessage;
        }

        if (this.actionEnum.equals(ActionEnum.UPDATE)) {
            T dbDomain = get(domain.getId());
            ObjectUtil.copyFieldValue(domain, dbDomain);
            domain = dbDomain;
        }

        beforeSave(domain);
        saveOrUpdate(domain, cascadeUpdate);
        afterSave(domain);

        return null;
    }


    /**
     * 面向数据库的单纯save操作
     *
     * @param domain
     * @param cascadeUpdate
     * @throws Exception
     */
    public void saveOrUpdate(T domain, String... cascadeUpdate) throws Exception {
        if (ObjectUtil.notNull(domain)) {
            //保存关联的domain
            List<Field> fieldList = Arrays.stream(domain.getClass().getDeclaredFields())
                    .filter(p -> {
                        String annotationStr = Arrays.stream(p.getDeclaredAnnotations()).map(k -> k.annotationType().getSimpleName()).collect(joining(","));
                        return StringUtil.isNotEmpty(annotationStr) && (
                                annotationStr.contains("OneToOne") ||
                                        annotationStr.contains("OneToMany") ||
                                        annotationStr.contains("ManyToOne") ||
                                        annotationStr.contains("ManyToMany")
                        );
                    })
                    .filter(p -> {
                        try {
                            Object data = getReturnData(domain, p);
                            return ObjectUtil.notNull(data) ? true : false;
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }).collect(Collectors.toList());

            if (CollectorUtil.isEmpty(fieldList)) {
                save(domain);
            } else {
                for (Field p : fieldList) {
                    Class joinDomainClass;
                    String annotations = Arrays.stream(p.getDeclaredAnnotations()).map(k -> k.annotationType().getName()).collect(joining(","));
                    if (p.getType().equals(List.class)) {
                        joinDomainClass = ((Class) (((ParameterizedType) (p.getGenericType())).getActualTypeArguments()[0]));
                        CommonService joinDomainService = SpringContextUtil.getServiceImplByDomain(joinDomainClass);
                        List<BasicDomain> data = (List<BasicDomain>) getReturnData(domain, p);
                        if (annotations.contains("OneToMany")) {
                            String mapField = p.getDeclaredAnnotation(OneToMany.class).mappedBy();

                            if (!ObjectUtil.notNull(domain.getId()) && CollectorUtil.isNoTEmpty(data)) {
                                BasicDomain joinDomainInstance = ((BasicDomain) joinDomainClass.newInstance());
                                joinDomainInstance.setRemove(true);
                                joinDomainService.updateBySelective(joinDomainInstance, QueryDto.builder().available().addWhere(StringUtil.toUnderlineCase(mapField) + "_id = " + domain.getId()).build());
                            }

                            save(domain);
                            Method setDomainMethod = Arrays.stream(joinDomainClass.getMethods())
                                    .filter(k -> k.getName().equalsIgnoreCase("set" + mapField))
                                    .findFirst()
                                    .orElse(null);
                            for (BasicDomain joinDomain : data) {
                                if (ObjectUtil.notNull(joinDomain.getId()) && !ArrayUtil.contains(cascadeUpdate, p.getName())) {
                                    joinDomain = joinDomainService.get(joinDomain.getId());
                                }
                                joinDomain.setRemove(false);
                                setDomainMethod.invoke(joinDomain, domain);
                                joinDomainService.save(joinDomain);
                                ReflectUtil.setFieldValue(joinDomain, mapField, null);
                            }
                        } else if (annotations.contains("ManyToMany")) {

                            if (ObjectUtil.notNull(domain.getId()) && ObjectUtil.notNull(data)) {
                                deleteMiddleTable(p.getName(), domain.getId());
                            }

                            save(domain);

                            for (BasicDomain joinDomain : data) {
                                if (ObjectUtil.isNull(joinDomain.getId()) || ArrayUtil.contains(cascadeUpdate, p.getName())) {
                                    joinDomainService.save(joinDomain);
                                }
                                insertMiddleTable(p.getName(), domain.getId(), joinDomain.getId());
                            }
                        }
                    } else {
                        joinDomainClass = p.getType();
                        CommonService joinDomainService = SpringContextUtil.getServiceImplByDomain(joinDomainClass);
                        Object data = getReturnData(domain, p);
                        if (ObjectUtil.notNull(data) && data instanceof BasicDomain) {
                            BasicDomain joinDomain = (BasicDomain) data;
                            if (annotations.contains("OneToOne") || annotations.contains("ManyToOne")) {
                                if (ObjectUtil.isNull(joinDomain.getId()) || ArrayUtil.contains(cascadeUpdate, p.getName())) {
                                    joinDomainService.save(joinDomain);
                                }
                            }
                        }

                        save(domain);
                    }
                }
            }

        }
    }


    /**
     * 根据where批量更新不为null的字段
     *
     * @param record
     * @return
     * @throws Exception
     */
    public int updateBySelective(T record, QueryDto queryDto) throws Exception {

        if (CollectorUtil.isEmpty(queryDto.getWhereCondition())) {
            throw new BusinessException("", ActionEnum.UPDATE, LogStatus.FAILED, "更新domain，不能没有where条件");
        }
        setUpdateTime(record);
        return (Integer) proxyDao.getClass().getDeclaredMethod("updateByPrimaryKeySelective", domainClass, QueryDto.class).invoke(proxyDao, record, queryDto);
    }


    /**
     * 根据domain id 更新不为null的字段
     *
     * @param record
     * @return
     * @throws Exception
     */
    public int updateBySelective(T record) throws Exception {

        if (ObjectUtil.isNull(record.getId())) {
            throw new BusinessException("", ActionEnum.UPDATE, LogStatus.FAILED, "更新domain，不能没有id");
        }

        QueryDto queryDto = QueryDto.builder().addWhere(String.format("id = %s", record.getId())).build();
        int n = updateBySelective(record, queryDto);

        logService.log(ActionEnum.UPDATE, LogStatus.SUCCESS, String.format("%s : id = %s ", ActionEnum.UPDATE.getName(), record.getId()), this.tableName);

        return n;
    }


    /**
     * 新增一条记录，只使用不为空的字段
     *
     * @param record
     * @return
     * @throws Exception
     */
    public int insertSelective(T record) throws Exception {
        int n = (Integer) proxyDao.getClass().getDeclaredMethod("insertSelective", domainClass).invoke(proxyDao, record);

        logService.log(ActionEnum.ADD, LogStatus.SUCCESS, String.format("%s ", ActionEnum.ADD.getName()), this.tableName);

        return n;
    }


    /*****************************删除************************************/
    /**
     * 是逻辑删除，不是物理删除，标识remove字段为true
     *
     * @return
     * @throws Exception
     */
    public int delete(T domain) throws Exception {
        this.actionEnum = ActionEnum.DELETE;
        domain.setRemove(true);
        setOperateTime(domain);
        int n = (Integer) proxyDao.getClass().getDeclaredMethod("deleteByPrimaryKey", domainClass).invoke(proxyDao, domain);

        logService.log(ActionEnum.DELETE, LogStatus.SUCCESS, String.format("%s : id = %s", ActionEnum.DELETE.getName(), domain.getId()), this.tableName);

        return n;
    }

    /**
     * 面向controller层的，带有业务逻辑的delete
     *
     * @param domain
     * @throws Exception
     */
    public void deleteDomain(T domain) throws Exception {
        this.actionEnum = ActionEnum.DELETE;
        beforeDelete(domain);
        delete(domain);
        afterDelete(domain);
        log.info(String.format("%s了一个%s, id: %s", actionEnum.getName(), domainClass.getSimpleName(), domain.getId()));
    }

    /**
     * 批量删除
     *
     * @param ids
     * @throws Exception
     */
    public void batchDelete(Long[] ids) throws Exception {
        if (ArrayUtil.isNotEmpty(ids)) {
            this.actionEnum = ActionEnum.BATCH_DELETE;
            QueryDto queryDto = QueryDto.builder().addWhere(String.format("id in (%s)", ArrayUtil.join(ids, ","))).build();
            T domain = domainClass.newInstance();
            domain.setRemove(true);
            updateBySelective(domain, queryDto);

            logService.log(ActionEnum.BATCH_DELETE, LogStatus.SUCCESS, String.format("%s : ids (%s) ", ActionEnum.BATCH_DELETE.getName(), ids), this.tableName);

        }
    }

    /**
     * 删除
     *
     * @param id
     * @throws Exception
     */
    public void deleteById(Long id) throws Exception {
        if (ObjectUtil.notNull(id)) {
            this.actionEnum = ActionEnum.BATCH_DELETE;
            QueryDto queryDto = QueryDto.builder().addWhere(String.format("id = %s ", id)).build();
            T domain = domainClass.newInstance();
            domain.setRemove(true);
            updateBySelective(domain, queryDto);

            logService.log(ActionEnum.BATCH_DELETE, LogStatus.SUCCESS, String.format("%s : id (%s) ", ActionEnum.DELETE.getName(), id), this.tableName);

        }
    }


    /*****************************中间关联表新增、删除************************************/
    /**
     * 对中间关联表做新增
     *
     * @param joinDomainField
     * @param domainId
     * @param joinDomainId
     * @return
     * @throws Exception
     */
    public int insertMiddleTable(String joinDomainField, Long domainId, Long joinDomainId) throws Exception {
        String methodName = "insert" + domainClass.getSimpleName() + StringUtil.upperFirst(joinDomainField);

        String tableName = ReflectUtil.getField(domainClass, joinDomainField).getDeclaredAnnotation(JoinTable.class).name();
        int n = (Integer) proxyDao.getClass().getDeclaredMethod(methodName, Long.class, Long.class).invoke(proxyDao, domainId, joinDomainId);

        logService.log(ActionEnum.ADD, LogStatus.SUCCESS, String.format("绑定%s和%s的关联：domainId(%s),joinDomainId(%s),joinDomainField(%s)", domainClass.getSimpleName(), joinDomainField, domainId, joinDomainId, joinDomainField), tableName);

        return n;
    }

    /**
     * 对中间关联表做删除，删除所有的指定的domainId
     *
     * @param joinDomainField 关联的domain字段名字
     * @param domainId        domainId
     * @return
     * @throws Exception
     */
    public int deleteMiddleTable(String joinDomainField, Long domainId) throws Exception {
        String methodName = "delete" + domainClass.getSimpleName() + StringUtil.upperFirst(joinDomainField) + "Middle";
        Integer n = (Integer) proxyDao.getClass().getDeclaredMethod(methodName, Long.class).invoke(proxyDao, domainId);
        String tableName = ReflectUtil.getField(domainClass, joinDomainField).getDeclaredAnnotation(JoinTable.class).name();
        logService.log(ActionEnum.DELETE, LogStatus.SUCCESS, String.format("解除%s和%s所有的关联：%sId(%s)", domainClass.getSimpleName(), joinDomainField, domainClass.getSimpleName(), domainId), tableName);

        return n;
    }

    /**
     * 对中间关联表做删除，删除指定的domainId和joinDomainId
     *
     * @param joinDomainField 关联的domain字段名字
     * @param domainId        domainId
     * @param joinDomainId    joinDomainId
     * @return
     * @throws Exception
     */
    public int deleteMiddleTable(String joinDomainField, Long domainId, Long joinDomainId) throws Exception {
        String methodName = "delete" + domainClass.getSimpleName() + StringUtil.upperFirst(joinDomainField);
        int n = (Integer) proxyDao.getClass().getDeclaredMethod(methodName, Long.class, Long.class).invoke(proxyDao, domainId, joinDomainId);
        String tableName = ReflectUtil.getField(domainClass, joinDomainField).getDeclaredAnnotation(JoinTable.class).name();
        logService.log(ActionEnum.DELETE, LogStatus.SUCCESS, String.format("解除%s和%s的关联：domainId(%s),joinDomainId(%s),joinDomainField(%s) ", domainClass.getSimpleName(), joinDomainField, domainId, joinDomainId, joinDomainField), tableName);

        return n;
    }


    /***************************导出************************************/

    /**
     * 下载模板
     *
     * @param response
     */
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        ExcelSheet sheet = ExcelSheet.builder().setTitle(domainClass).build();
        String fileName = "导入模板";
        Excel excel = domainClass.getDeclaredAnnotation(Excel.class);
        if (ObjectUtil.notNull(excel)) {
            fileName = excel.value();
        }
        ExcelUtil.builder()
                .addSheet(sheet)
                .setFileName(fileName)
                .build()
                .writeToClient(response);
    }

    /**
     * 下载批量删除的模板
     *
     * @param response
     */
    public void batchDeleteTemplate(HttpServletResponse response) throws Exception {
        List<Field> fields = Arrays.stream(domainClass.getDeclaredFields())
                .filter(p -> p.getDeclaredAnnotation(BusinessId.class) != null
                        && p.getDeclaredAnnotation(ExcelTitle.class) != null
                ).collect(Collectors.toList());
        ExcelSheetBuilder sheetBuilder = ExcelSheet.builder();
        if (CollectorUtil.isNoTEmpty(fields)) {
            for (Field field : fields) {
                ExcelTitle excelTitle = field.getDeclaredAnnotation(ExcelTitle.class);
                String name = excelTitle.name();
                String key = StringUtil.toUnderlineCase(field.getName());
                sheetBuilder.addTitle(key, name, 20);
            }
        }
        ExcelUtil.builder()
                .addSheet(sheetBuilder.build())
                .setFileName("批量删除的模板")
                .build()
                .writeToClient(response);
    }


    public ApiResponse batchDel(MultipartHttpServletRequest request) throws Exception {
        List<T> domainList = ExcelUtil.getExcelFromRequest(request, domainClass);
        String whereSql = getBusinessIdWhereSql(domainList);
        QueryDto queryDto = QueryDto.builder().addWhere(whereSql).build();
        T domain = domainClass.newInstance();
        domain.setRemove(true);
        updateBySelective(domain, queryDto);

        logService.log(ActionEnum.BATCH_DELETE, LogStatus.SUCCESS, String.format("根据筛选条件批量删除 : where条件 %s ", JSONUtil.toJsonStr(queryDto.getWhereCondition())), this.tableName);

        return ApiResponse.success();
    }

    private String getBusinessIdWhereSql(List<T> domainList) {
        List<Field> fields = Arrays.stream(domainClass.getDeclaredFields())
                .filter(p -> p.getDeclaredAnnotation(BusinessId.class) != null
                        && p.getDeclaredAnnotation(ExcelTitle.class) != null
                ).collect(Collectors.toList());

        if (fields.size() == 0) {
            throw new BusinessException("", ActionEnum.GET_DOMAIN, LogStatus.FAILED, "account没有配置业务主键");
        }

        String whereSql = "";
        for (int j = 0; j < domainList.size(); j++) {
            T domain = domainList.get(j);
            String fieldSql = "(";
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                String value = ReflectUtil.getFieldValue(domain, field).toString();
                String key = StringUtil.toUnderlineCase(field.getName());
                boolean isNumber = (field.getType().equals(Integer.class) || field.getType().equals(Long.class)) ? true : false;
                if (isNumber) {
                    fieldSql += key + " = " + value;
                } else {
                    fieldSql += key + " = '" + value + "'";
                }
                if (i != fields.size() - 1) {
                    fieldSql += " and ";
                }
            }
            fieldSql += ")";
            whereSql += fieldSql;
            if (j != domainList.size() - 1) {
                whereSql += " or ";
            }
        }

        return whereSql;
    }


    /**
     * 批量导入(全量更新和增量更新)
     *
     * @param request
     * @return
     * @throws Exception
     */
    public ApiResponse importDomain(MultipartHttpServletRequest request) throws Exception {

        String type = WebUtil.getStringParam("uploadType");
        List<Field> businessIdFields = Arrays.stream(domainClass.getDeclaredFields()).filter(p -> p.getDeclaredAnnotation(BusinessId.class) != null && p.getDeclaredAnnotation(ExcelTitle.class) != null).collect(Collectors.toList());
        List<Field> dictionaryFields = Arrays.stream(domainClass.getDeclaredFields())
                .filter(p -> p.getDeclaredAnnotation(ExcelTitle.class) != null)
                .filter(p -> !p.getDeclaredAnnotation(ExcelTitle.class).dictionary().equals("-"))
                .collect(Collectors.toList());
        List<T> domainList = ExcelUtil.getExcelFromRequest(request, domainClass);

        Map excelContext = MapUtil.builder().add("isCheckNotNull", "all".equalsIgnoreCase(type) ? true : false).build();
        List<ExcelSheet> validateResult = validateExcelData(domainList, excelContext);


        if (CollectorUtil.isNoTEmpty(validateResult)) {
            String filePath = uploadPath + File.separator + domainClass.getSimpleName();
            ExcelUtil.builder()
                    .setFileName("异常数据")
                    .addSheetAll(validateResult).build()
                    .writeToLocal(filePath);

            return ApiResponse.error("导入失败，请下载导入日志查看异常数据", filePath + File.separator + "异常数据.xlsx");
        }

        String whereSql = getBusinessIdWhereSql(domainList);
        QueryDto queryDto = QueryDto.builder().addWhere(whereSql).build();

        List<T> dbDomains = select(queryDto);
        MapBuilder<String, Long> mapBuilder = MapUtil.builder();
        for (T domain : dbDomains) {
            String ids = businessIdFields.stream().map(k -> ReflectUtil.getFieldValue(domain, k).toString()).collect(joining(","));
            mapBuilder.add(ids, domain.getId());
        }

        Map<String, Long> map = mapBuilder.build();
        for (T domain : domainList) {
            String ids = businessIdFields.stream().map(k -> ReflectUtil.getFieldValue(domain, k).toString()).collect(joining(","));

            if (map.containsKey(ids)) {
                Long domainId = map.get(ids);
                T dbDomain = dbDomains.stream().filter(p -> p.getId().equals(domainId)).findFirst().orElse(null);
                domain.setId(dbDomain.getId());
                domain.setCreateTime(dbDomain.getCreateTime());
                domain.setCreateUser(dbDomain.getCreateUser());
                domain.setRemove(false);
                if ("all".equals(type)) {
                    saveOrUpdate(domain);
                } else {
                    updateBySelective(domain, QueryDto.builder().addWhere("id = " + domainId).build());
                }
            } else {
                saveOrUpdate(domain);
            }
        }

        String ids = domainList.stream().map(p -> p.getId().toString()).collect(joining(","));
        if (StringUtil.isNotEmpty(ids)) {

            logService.log(ActionEnum.BATCH_IMPORT, LogStatus.SUCCESS, String.format("%s : id = %s ", ActionEnum.BATCH_IMPORT.getName(), ids), this.tableName);

        }
        return ApiResponse.success();
    }


    /**
     * 导出到本地
     *
     * @param
     * @throws Exception
     */
    public String export() throws Exception {
        String[] titles = WebUtil.getStringArrayParam("titles");
        Long[] checkedDataIds = WebUtil.getLongArrayParam("checkedIds");
        boolean isTrailDate = WebUtil.getBooleanParam("trailDate");
        String fileName = WebUtil.getStringParam("fileName");
        String[] joinDomains = WebUtil.getStringArrayParam("joinDomain");

        String tableAlis = StringUtil.toUnderlineCase(StringUtil.lowerFirst(domainClass.getSimpleName()));

        Map checkedMap = MapUtil.builder().build();
        ExcelSheetBuilder excelSheetBuilder = ExcelSheet.builder();
        for (int i = 0; i < titles.length; i++) {
            String item = titles[i];
            String[] ss = item.split("\\|");
            checkedMap.put(ss[0], ss[1]);
            int width = -1;
            String dicCode = null;
            Field field = ReflectUtil.getField(domainClass, ss[0]);
            if (ObjectUtil.notNull(field)) {
                ExcelTitle excelTitle = field.getDeclaredAnnotation(ExcelTitle.class);
                if (ObjectUtil.notNull(excelTitle)) {
                    width = excelTitle.width();
                    if (!excelTitle.dictionary().equals("-")) {
                        dicCode = excelTitle.dictionary();
                    }
                }
            }
            excelSheetBuilder.addTitle(ss[0], ss[1], dicCode, i, width < 0 ? null : width);
        }

        List<String> joinList = new LinkedList<>();
        QueryDto queryDto = getPageQueryDto();
        if (ArrayUtil.isNotEmpty(checkedDataIds)) {
            String ids = ArrayUtil.join(checkedDataIds, ",");
            queryDto.getWhereCondition().add(String.format("%s.id in (%s)", tableAlis, ids));
        }

        if (ArrayUtil.isNotEmpty(joinDomains)) {
            joinList.addAll(ArrayUtil.arrayToList(joinDomains));
            queryDto.setJoinDomain(joinList);
        }


        queryDto.setRemove(false);
        List<T> datas = select(queryDto);
        formatPageData(datas);

        String filePath = uploadPath + File.separator + domainClass.getSimpleName();
        excelSheetBuilder.addRowList(datas);
        filePath = ExcelUtil.builder()
                .setIsTailDate(isTrailDate)
                .setFileName(fileName)
                .addSheet(excelSheetBuilder.build())
                .build()
                .writeToLocal(filePath);
        return filePath;
    }

    /***************************字段唯一性校验************************************/
    public <K extends BasicDomain> K checkUnique(Class<K> domainClass, String field, Object value, String... whereConditions) throws Exception {
        CommonService<K> service = SpringContextUtil.getServiceImplByDomain(domainClass);

        String tableName = StringUtil.toUnderlineCase(domainClass.getSimpleName());
        String valueStr = null;
        if (value instanceof Integer || value instanceof Long) {
            valueStr = value.toString();
        } else {
            valueStr = "'" + value + "'";
        }

        List<String> conditions = Arrays.stream(whereConditions).filter(StringUtil::isNotEmpty).collect(Collectors.toList());
        QueryDto queryDto = QueryDto.builder()
                .addWhere(String.format("%s.%s=%s", tableName, field, valueStr))
                .addAllWhere(conditions)
                .build();
        K domain = service.select(queryDto).stream().findFirst().orElse(null);
        return domain;
    }


    private void instanceInitData() {


        if (ObjectUtil.isNull(domainClass)) {
            domainClass = (Class<T>) (((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        }
        Class domainDaoClass;
        String domainPackage = domainClass.getPackage().getName();
        String prePackageName = domainPackage.substring(0, domainPackage.indexOf(".domain"));

        List<Class> classes = ObjectUtil.getClasses("com.dhl.fin.api.dao");
        domainDaoClass = classes.stream().filter(p -> p.getSimpleName().equalsIgnoreCase(domainClass.getSimpleName() + "DAO")).findFirst().orElse(null);

        Map<String, Object> datas = SpringContextUtil.getBeanOfType(domainDaoClass);
        if (datas.size() > 0) {
            proxyDao = datas.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .get();
        }

        Object t = this.domainClass.getDeclaredAnnotation(Table.class);
        if (ObjectUtil.notNull(t)) {
            tableName = ((Table) t).name();
        }

    }

    private T selectById(Map params) throws Exception {
        return (T) proxyDao.getClass().getDeclaredMethod("selectByPrimaryKey", Map.class).invoke(proxyDao, params);
    }

    /**
     * 分页查询
     *
     * @param pageQuery
     * @return
     * @throws Exception
     */
    private List<T> selectPageQuery(QueryDto pageQuery) throws Exception {
        addJoinMap(pageQuery);
        addIsRemove(pageQuery);
        addOrderField(pageQuery);
        return (List<T>) proxyDao.getClass().getDeclaredMethod("selectPageQuery", QueryDto.class).invoke(proxyDao, pageQuery);
    }


    private void addOrderField(QueryDto queryDto) {
        if (CollectorUtil.isNoTEmpty(queryDto.getOrderCondition())) {
            String orderFields = queryDto.getOrderCondition().stream()
                    .map(p -> p.toLowerCase().replace("asc", "").replace("desc", "").trim())
                    .collect(joining(","));
            String domainAlias = StringUtil.toUnderlineCase(this.domainClass.getSimpleName()) + ".id";
            if (!orderFields.contains(domainAlias)) {
                orderFields += "," + domainAlias;
            }
            queryDto.setOrderFields(orderFields);
        }
    }

    private void addJoinMap(QueryDto queryDto) {
        if (ObjectUtil.notNull(queryDto) && CollectorUtil.isNoTEmpty(queryDto.getJoinDomain())) {
            List<Map> joinMap = queryDto.getJoinDomain()
                    .stream()
                    .filter(ObjectUtil::notNull)
                    .map(p -> Arrays.stream(domainClass.getDeclaredFields())
                            .filter(k -> k.getName().equals(p))
                            .findFirst()
                            .orElse(null)
                    )
                    .filter(ObjectUtil::notNull)
                    .map(p -> {
                        boolean isM2M = false;
                        boolean isO2M = false;
                        String middleTable = null;
                        String joinDomainName = null;
                        String invertJoinId = null;
                        String joinFieldName = null;
                        if (ObjectUtil.notNull(p.getDeclaredAnnotation(ManyToMany.class))) {
                            invertJoinId = Arrays.stream(p.getDeclaredAnnotation(JoinTable.class).inverseJoinColumns()).map(k -> k.name()).findFirst().get();
                            middleTable = p.getDeclaredAnnotation(JoinTable.class).name().trim();
                            isM2M = true;
                        }
                        if (ObjectUtil.notNull(p.getDeclaredAnnotation(OneToMany.class))) {
                            joinFieldName = p.getDeclaredAnnotation(OneToMany.class).mappedBy().trim();
                            isO2M = true;
                        }

                        if (ObjectUtil.notNull(p.getDeclaredAnnotation(ManyToMany.class)) || ObjectUtil.notNull(p.getDeclaredAnnotation(OneToMany.class))) {
                            joinDomainName = ((Class) (((ParameterizedType) (p.getGenericType())).getActualTypeArguments()[0])).getSimpleName().trim();
                        } else {
                            joinDomainName = p.getType().getSimpleName().trim();
                        }

                        joinDomainName = StringUtil.join(joinDomainName.substring(0, 1).toLowerCase(), joinDomainName.substring(1));
                        return MapUtil.builder()
                                .add("middleTable", middleTable)
                                .add("isM2M", isM2M)
                                .add("isO2M", isO2M)
                                .add("joinField", StringUtil.toUnderlineCase(joinFieldName))
                                .add("invertJoinId", invertJoinId)
                                .add("tableName", StringUtil.toUnderlineCase(joinDomainName))
                                .add("tableAlia", StringUtil.toUnderlineCase(p.getName()))
                                .build();
                    })
                    .collect(Collectors.toList());
            queryDto.setJoinMap(joinMap);
        }
    }


    public int save(T record) throws Exception {
        setOperateTime(record);
        if (ObjectUtil.isNull(record.getId())) {
            return this.insert(record);
        } else {
            return this.update(record);
        }
    }


    private Object getReturnData(T domain, Field p) throws InvocationTargetException, IllegalAccessException {
        String getMethodName = StringUtil.upperFirstAndAddPre(p.getName(), "get");
        Method getMethod = Arrays.stream(domain.getClass().getDeclaredMethods())
                .filter(k -> k.getName().equalsIgnoreCase(getMethodName))
                .findFirst()
                .orElse(null);

        return getMethod.invoke(domain);
    }


    /**
     * 新增一条记录，使用全部的字段
     *
     * @param record
     * @return
     * @throws Exception
     */
    private int insert(T record) throws Exception {
        int n = (Integer) proxyDao.getClass().getDeclaredMethod("insert", domainClass).invoke(proxyDao, record);

        logService.log(ActionEnum.ADD, LogStatus.SUCCESS, String.format("%s", ActionEnum.ADD.getName()), this.tableName);


        return n;
    }

    /**
     * 更新所有字段的值
     *
     * @param record
     * @return
     * @throws Exception
     */
    private int update(T record) throws Exception {
        checkDomainId(record);
        setOperateTime(record);
        int n = (Integer) proxyDao.getClass().getDeclaredMethod("updateByPrimaryKey", domainClass).invoke(proxyDao, record);

        logService.log(ActionEnum.UPDATE, LogStatus.SUCCESS, String.format("%s : id = %s", ActionEnum.UPDATE.getName(), record.getId()), this.tableName);

        return n;
    }


    private void checkDomainId(T record) throws Exception {
        if (ObjectUtil.isNull(record.getId())) {
            String logInfo = String.format("update %s 失败,请加上id号", domainClass.getTypeName());
            log.error(logInfo);
            throw new BusinessException(this.tableName, ActionEnum.UPDATE, LogStatus.FAILED, logInfo);
        }
    }


    public void afterGet(T domain) throws Exception {

    }


    private QueryDto getPageQueryDto() throws Exception {
        QueryDto queryDto = parseRequestFilterParams();
        QueryDto pageQuery = QueryDto.builder()
                .addAllWhere(queryDto.getWhereCondition())
                .addAllJoinDomain(queryDto.getJoinDomain())
                .addAllOrder(queryDto.getOrderCondition())
                .build();
        addJoinMap(pageQuery);
        beforePageQuery(pageQuery);
        return pageQuery;
    }


    /**
     * 从request里获取filter_ 过滤条件
     *
     * @return
     */
    private QueryDto parseRequestFilterParams() {
        List<String> whereList = new LinkedList<>();
        List<String> joinList = new LinkedList<>();

        WebUtil.getRequestParams()
                .entrySet()
                .stream()
                .filter(p -> p.getKey().startsWith("filter_"))
                .forEach(p -> {
                    String[] s = p.getKey().split("_");
                    String fieldName = s[1];
                    if (!s[1].contains(".")) {
                        fieldName = StringUtil.toUnderlineCase(StringUtil.lowerFirst(domainClass.getSimpleName())) + "." + s[1];
                    }

                    String[] k = fieldName.split("\\.");
                    String whereField = null;
                    if (k.length == 2) {
                        String tableAlias = StringUtil.toUnderlineCase(k[0]);
                        whereField = StringUtil.join(tableAlias, ".", StringUtil.toUnderlineCase(k[1]));
                    } else {
                        whereField = StringUtil.toUnderlineCase(k[0]);
                    }
                    OperatorEnum operatorEnum = OperatorEnum.valueOf(s[2].toUpperCase());
                    String operator = operatorEnum.getOperator();

                    String v = operatorEnum.equals(OperatorEnum.BT) ? StringUtil.join(p.getValue(), "' and '") : StringUtil.join(p.getValue(), ",");

                    String value;
                    DataTypeEnum dataTypeEnum = DataTypeEnum.getByCode(s[3]);
                    switch (dataTypeEnum) { // 数据类型
                        case INT:
                        case LONG:
                            value = v;
                            break;
                        case STRING:
                            String valueModel = "'%s'";
                            if (operatorEnum.equals(OperatorEnum.LIKE)) {
                                valueModel = "'%%%s%%'";
                            } else if (operatorEnum.equals(OperatorEnum.LIKES)) {
                                valueModel = "'%s%%'";
                            }
                            value = String.format(valueModel, v);
                            break;
                        case ARRAY_INT:
                            value = String.format("(%s)", v);
                            break;
                        case ARRAY_STRING:
                            value = String.format("(%s)", StringUtil.join("'", v.replaceAll(",", "','"), "'"));
                            break;

                        default:
                            value = v;
                    }

                    whereList.add(StringUtil.join(whereField, " ", operator, " ", value));
                    joinList.add(StringUtil.toUnderlineCase(k[0]));
                });


        String[] orderBy = WebUtil.getStringArrayParam("orderBy");
        String domainTableName = StringUtil.toUnderlineCase(StringUtil.lowerFirst(domainClass.getSimpleName()));
        if (ObjectUtil.isNull(orderBy)) {
            orderBy = new String[]{domainTableName + ".id desc"};
        } else {
            orderBy = Arrays.stream(orderBy)
                    .map(String::trim)
                    .map(p -> p.endsWith("Str asc") ? p.replaceAll("Str asc", " asc") : p)
                    .map(p -> p.endsWith("Str desc") ? p.replaceAll("Str desc", " desc") : p)
                    .map(item -> item.contains(".") ? StringUtil.toUnderlineCase(item) : StringUtil.join(domainTableName, ".", StringUtil.toUnderlineCase(item)))
                    .toArray(String[]::new);
        }


        String[] joinDomains = WebUtil.getStringArrayParam("joinDomain");
        if (ArrayUtil.isNotEmpty(joinDomains)) {
            joinList.addAll(ArrayUtil.arrayToList(joinDomains));
        }

        return QueryDto.builder()
                .addAllWhere(whereList)
                .addAllOrder(Arrays.asList(orderBy))
                .addAllJoinDomain(joinList)
                .build();
    }

    public String recordLog() {
        return String.format("后台%s异常，%s失败", actionEnum.getName(), actionEnum.getName());
    }

    private void addIsRemove(QueryDto queryDto) {
        Boolean remove = queryDto.getRemove();
        String domainAlias = StringUtil.toUnderlineCase(domainClass.getSimpleName());
        if (ObjectUtil.notNull(remove)) {
            queryDto.getWhereCondition().add(String.format("%s.remove = %s", domainAlias, remove ? "1" : "0"));
        }
    }

    private void addFields(QueryDto queryDto) {
        String underlineDomainName = StringUtil.toUnderlineCase(domainClass.getSimpleName());
        String fields = Arrays.stream(domainClass.getDeclaredFields())
                .filter(p -> p.getDeclaredAnnotation(BusinessId.class) != null)
                .map(p -> StringUtil.toUnderlineCase(p.getName()))
                .map(p -> underlineDomainName + "." + p)
                .collect(joining(","));
        if (StringUtil.isEmpty(fields)) {
            queryDto.setFields(underlineDomainName + ".id");
        } else {
            queryDto.setFields(fields);
        }
    }

    private void setUpdateTime(BasicDomain domain) {
        if (ObjectUtil.isNull(domain)) {
            return;
        }
        domain.setUpdateUser(getUserUUID());
        domain.setUpdateTime(DateUtil.getSysTime());
    }

    private void setOperateTime(BasicDomain domain) {

        if (ObjectUtil.isNull(domain)) {
            return;
        }
        String uuid = getUserUUID();
        if (ObjectUtil.isNull(domain.getId())) {
            domain.setCreateUser(uuid);
            domain.setUpdateUser(uuid);
            domain.setCreateTime(DateUtil.getSysTime());
            domain.setUpdateTime(DateUtil.getSysTime());
        } else {
            domain.setUpdateUser(uuid);
            domain.setUpdateTime(DateUtil.getSysTime());
        }
        domain.setUpdateUser(uuid);
    }

    /**
     * 交给各自service格式化分页查询的数据
     *
     * @param domain
     */
    protected void formatRowData(T domain) throws Exception {
    }

    /**
     * 数据字典的字段转中文
     *
     * @param domain
     * @return
     */
    private void fieldToCN(T domain) throws Exception {
        List<Field> fields = Arrays.stream(domainClass.getDeclaredFields())
                .filter(p ->
                        p.getDeclaredAnnotation(ExcelTitle.class) != null
                                && !p.getDeclaredAnnotation(ExcelTitle.class).dictionary().equals("-")
                                && p.getType().equals(String.class)
                )
                .collect(Collectors.toList());

        fieldToCN(domain, fields);
    }

    /**
     * 数据字典的字段转中文
     *
     * @param domain
     * @return
     */
    private void fieldToCN(T domain, List<Field> fields) throws Exception {
        fields.forEach(field -> {
            ExcelTitle excelTitle = field.getDeclaredAnnotation(ExcelTitle.class);
            Object value = ReflectUtil.getFieldValue(domain, field);
            String optionCode = ObjectUtil.isNull(value) ? "" : value.toString();
            String optionValue = redisService.getDictionaryValue(excelTitle.dictionary(), optionCode);
            ReflectUtil.setFieldValue(domain, field, optionValue);
        });
    }


    /**
     * date time格式化 "yyyy-MM-dd HH:mm:ss"
     *
     * @param domain
     * @throws Exception
     */
    private void dateFieldFormat(T domain) throws Exception {
        Field[] domainFields = ArrayUtil.addAll(domainClass.getDeclaredFields(), domainClass.getSuperclass().getDeclaredFields());
        List<Field> dateFields = Arrays.stream(domainFields)
                .filter(p -> p.getType().equals(Date.class) || p.getType().equals(Timestamp.class))
                .collect(Collectors.toList());
        dateFieldFormat(domain, dateFields);
    }

    /**
     * date time格式化 "yyyy-MM-dd HH:mm:ss"
     *
     * @param domain
     * @param fields
     * @throws Exception
     */
    private void dateFieldFormat(T domain, List<Field> fields) throws Exception {
        fields.forEach(field -> {
            Object dateObject = ReflectUtil.getFieldValue(domain, field);
            String dateStr = null;

            if (dateObject instanceof Date) {
                dateStr = DateUtil.getFullTime((Date) dateObject);
            } else if (dateObject instanceof Timestamp) {
                dateStr = DateUtil.getFullTime((Timestamp) dateObject);
            }

            if (StringUtil.isNotEmpty(dateStr)) {
                String dateStrField = field.getName() + "Str";
                Field field1 = ReflectUtil.getField(domain.getClass(), dateStrField);
                if (ObjectUtil.notNull(field1)) {
                    ExcelTitle excelTitle = field1.getDeclaredAnnotation(ExcelTitle.class);
                    SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (ObjectUtil.notNull(excelTitle)) {
                        String dateFormat = excelTitle.dateFormat();
                        fullSdf = new SimpleDateFormat(StringUtil.isEmpty(dateFormat) ? "yyyy-MM-dd HH:mm:ss" : dateFormat);
                    }
                    ReflectUtil.setFieldValue(domain, dateStrField, fullSdf.format(dateObject));
                }
            }
        });
    }

    /**
     * date time 解析成日期对象
     *
     * @param domain
     * @param fields
     */
    public void parseDate(T domain, List<Field> fields) throws Exception {
        List<Field> fieldsList = fields.stream().filter(p -> p.getName().endsWith("Str")).collect(Collectors.toList());
        for (Field field : fieldsList) {
            String shortField = field.getName().substring(0, field.getName().length() - 3);
            Field sField = ReflectUtil.getField(domain.getClass(), shortField);
            if (ObjectUtil.notNull(sField)) {

                Object v = ReflectUtil.getFieldValue(domain, field);
                String dateTimeStr = ObjectUtil.notNull(v) ? v.toString() : null;

                if (StringUtil.isEmpty(dateTimeStr)) {
                    continue;
                }
                DateUtil.parseDateField(domain, dateTimeStr, sField);
            }
        }
    }

    /**
     * 格式化分页查询的数据
     *
     * @param datas
     * @throws Exception
     */
    public void formatPageData(List<T> datas) throws Exception {
        for (T domain : datas) {
            fieldToCN(domain);
            dateFieldFormat(domain);
            formatRowData(domain);
        }
    }

   /*   public void formatPageData(List<T> datas) throws Exception {
        Future completableFuture = null;
        for (T domain : datas) {
            Future createOrder = fintpAsyncThreadPool.submit(new AsyncFormatData(domain));
        }
    }

    private class AsyncFormatData implements Runnable {

        private T domain;

        public AsyncFormatData(T domain) {
            this.domain = domain;
        }

        @SneakyThrows
        @Override
        public void run() {
            fieldToCN(domain);
            dateFieldFormat(domain);
            formatRowData(domain);
        }
    }*/
/*

    public void formatPageData(List<T> datas) throws Exception {
        CompletableFuture completableFuture = null;
        for (T domain : datas) {
            CompletableFuture createOrder1 = asyncFormatData(domain);
            completableFuture = CompletableFuture.allOf(createOrder1);
        }
        completableFuture.join();

    }

    @Async("FintpAsyncThreadPool")
    public CompletableFuture asyncFormatData(T domain) throws Exception {
        Long startTime = DateUtil.getSysDate().getTime();
        fieldToCN(domain);
        dateFieldFormat(domain);
        formatRowData(domain);

        log.info(Thread.currentThread().getName() + "-----" + (DateUtil.getSysDate().getTime() - startTime));

        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }
*/

    /**
     * 格式化每个excel sheet内的数据
     *
     * @param excelSheets
     * @throws Exception
     */
    public void formatSheetData(List<ExcelSheet> excelSheets) throws Exception {
        for (ExcelSheet excelSheet : excelSheets) {
            for (T o : (List<T>) excelSheet.getRowList()) {
                fieldToCN(o);
                dateFieldFormat(o);
            }
        }
    }

    /**
     * 在删除前
     *
     * @param domain
     */
    public void beforeDelete(T domain) throws Exception {

    }

    /**
     * 删除后
     *
     * @param domain
     */
    public void afterDelete(T domain) throws Exception {

    }


    /**
     * 用于saveDomain方法前对domain的数据校验, 主要是服务前端
     * <p>
     * 返回数据为前端显示的提示信息
     *
     * @param domain
     * @return 为null为校验通过，非null的为校验不通过
     * @throws Exception
     */
    public String validate(T domain) throws Exception {

//        if (ObjectUtil.isNull(domain.getId())) {
        List<Field> fields = Arrays.stream(domain.getClass().getDeclaredFields())
                .filter(p -> p.getDeclaredAnnotation(BusinessId.class) != null)
                .collect(Collectors.toList());
        List<String> wheresqls = new LinkedList<>();

        if (ObjectUtil.notNull(domain.getId())) {
            wheresqls.add("id != " + domain.getId());
        }

        if (CollectorUtil.isNoTEmpty(fields)) {
            for (Field field1 : fields) {
                Object valueObject = ReflectUtil.getFieldValue(domain, field1);
                String value = ObjectUtil.isNull(valueObject) ? null : valueObject.toString();
                String key = StringUtil.toUnderlineCase(field1.getName());
                Class s = field1.getType();
                String whereSql = "";
                if (ObjectUtil.isNull(value)) {
                    whereSql = key + " is null ";
                } else if (s.equals(Integer.class) || s.equals(Long.class)) {
                    whereSql = String.format("%s = %s", key, value);
                } else {
                    whereSql = String.format("%s = '%s'", key, value);
                }
                wheresqls.add(whereSql);
            }
            T dbDomain = get(QueryDto.builder()
                    .available()
                    .addAllWhere(wheresqls)
                    .build());


            String message = fields.stream().map(p -> p.getName() + " = " + ReflectUtil.getFieldValue(domain, p)).collect(joining(","));

            if (ObjectUtil.notNull(dbDomain)) {
                return "已经存在" + message;
            }
        }
//        }


        return null;
    }


    /**
     * 批量导入，校验excel数据
     *
     * @param domainList
     * @param excelContext excel的上线环境
     * @return
     * @throws Exception
     */
    public List<ExcelSheet> validateExcelData(List<T> domainList, Map<String, Object> excelContext) throws Exception {
        List<ExcelSheet> excelSheets = new LinkedList<>();
        List<Field> filterField = Arrays.stream(domainClass.getDeclaredFields())
                .filter(p -> p.getDeclaredAnnotation(ExcelTitle.class) != null)
                .filter(p -> {
                    if (MapUtil.isEmpty(excelContext)) {
                        return true;
                    }
                    ExcelTitle excelTitle = p.getDeclaredAnnotation(ExcelTitle.class);
                    String key = excelTitle.disable();
                    boolean disableTitle = MapUtil.getBoolean(excelContext, key, false);
                    return !disableTitle;
                }).collect(Collectors.toList());
        List<Field> notAllowNullFields = filterField.stream()
                .filter(p -> p.getDeclaredAnnotation(ExcelTitle.class) != null && !p.getDeclaredAnnotation(ExcelTitle.class).allowNull())
                .collect(Collectors.toList());
        List<Field> businessIdFields = filterField.stream().filter(p -> p.getDeclaredAnnotation(BusinessId.class) != null && p.getDeclaredAnnotation(ExcelTitle.class) != null).collect(Collectors.toList());
        List<Field> formatFields = filterField.stream().filter(p ->
                p.getDeclaredAnnotation(ExcelTitle.class) != null
                        && StringUtil.isNotEmpty(p.getDeclaredAnnotation(ExcelTitle.class).formatCheck())
                        && String.class.equals(p.getType())
        ).collect(Collectors.toList());
        List<Field> fields = filterField.stream()
                .filter(p -> p.getDeclaredAnnotation(ExcelTitle.class) != null && !p.getDeclaredAnnotation(ExcelTitle.class).dictionary().equals("-"))
                .collect(Collectors.toList());
        List<T> dulpData = new LinkedList<>();
        List<T> noBusinessId = new LinkedList<>();
        List<T> formatNotRight = new LinkedList<>();
        List<T> notAllowNull = new LinkedList<>();

        //查看是否有重复数据
        if (CollectorUtil.isNoTEmpty(domainList)) {
            String[] temp = new String[]{""};
            domainList = domainList.stream().filter(p -> {
                String fieldV = "";
                for (Field f : businessIdFields) {
                    Object v = ReflectUtil.getFieldValue(p, f);
                    if (ObjectUtil.isNull(v)) {
                        noBusinessId.add(p);
                        return false;
                    }
                    fieldV += v.toString() + ",";
                }

                if (temp[0].contains(fieldV)) {
                    dulpData.add(p);
                } else {
                    temp[0] += fieldV;
                }
                return true;
            }).collect(Collectors.toList());

            for (T domain : domainList) {

                //给数据字典类型字段赋值
                for (Field field : fields) {
                    String parentCode = field.getDeclaredAnnotation(ExcelTitle.class).dictionary();
                    Object fieldValue = ReflectUtil.getFieldValue(domain, field);
                    if (ObjectUtil.isNull(fieldValue)) {
                        continue;
                    }
                    Map<String, Object> dics = redisService.getDictionary(parentCode);
                    for (Map.Entry<String, Object> item : dics.entrySet()) {
                        if (item.getValue().equals(fieldValue)) {
                            ReflectUtil.setFieldValue(domain, field, item.getKey());
                        }
                    }
                }

                //查看是否有格式不对的数据
                for (Field formatField : formatFields) {
                    String valueFormat = formatField.getDeclaredAnnotation(ExcelTitle.class).formatCheck();
                    Object value = ReflectUtil.getFieldValue(domain, formatField);
                    if (ObjectUtil.notNull(value)) {
                        if (!value.toString().matches(valueFormat) && !notAllowNull.contains(domain)) {
                            formatNotRight.add(domain);
                        }
                    }
                }

                //查看是否有不允许为空的数据
                boolean isCheckNotNull = true;
                if (ObjectUtil.notNull(excelContext)) {
                    isCheckNotNull = MapUtil.getBoolean(excelContext, "isCheckNotNull", true);
                }

                if (isCheckNotNull) {
                    for (Field notAllowNullField : notAllowNullFields) {
                        Object value = ReflectUtil.getFieldValue(domain, notAllowNullField);
                        if (ObjectUtil.isNull(value) && !notAllowNull.contains(domain)) {
                            notAllowNull.add(domain);
                        }
                    }
                }

            }
        }
        //查看是否有重复数据
        if (CollectorUtil.isNoTEmpty(dulpData)) {
            excelSheets.add(
                    ExcelSheet.builder()
                            .addProperty("monitorValue", true)
                            .setHeadName("重复数据")
                            .setTitle(domainClass)
                            .addRowList(dulpData)
                            .build()
            );
        }
        //查看是否没有设置业务主键
        if (CollectorUtil.isNoTEmpty(noBusinessId)) {
            notAllowNull.addAll(noBusinessId);
        }

        //查看是否有格式不对的数据
        if (CollectorUtil.isNoTEmpty(formatNotRight)) {
            String titles = formatFields.stream().map(p -> p.getDeclaredAnnotation(ExcelTitle.class).name()).collect(joining(","));
            excelSheets.add(
                    ExcelSheet.builder()
                            .addProperty("monitorValue", true)
                            .setHeadName("这些列[" + titles + "]格式不对")
                            .setTitle(domainClass)
                            .addRowList(formatNotRight)
                            .build()
            );
        }

        //查看是否有不允许为空的数据
        if (CollectorUtil.isNoTEmpty(notAllowNull)) {
            String titles =
                    businessIdFields.stream().map(p -> p.getDeclaredAnnotation(ExcelTitle.class).name()).collect(joining(",")) +
                            "," +
                            notAllowNullFields.stream().map(p -> p.getDeclaredAnnotation(ExcelTitle.class).name()).collect(joining(","));

            excelSheets.add(
                    ExcelSheet.builder()
                            .addProperty("monitorValue", true)
                            .setHeadName("这几列不能为空: " + titles)
                            .setTitle(domainClass)
                            .addRowList(notAllowNull)
                            .build()
            );
        }

        formatSheetData(excelSheets);

        return excelSheets;
    }

    /**
     * 在保存前
     *
     * @param domain
     */
    public void beforeSave(T domain) throws Exception {

    }

    /**
     * 保存后
     *
     * @param domain
     */
    public void afterSave(T domain) throws Exception {

    }

    /**
     * 查询前
     *
     * @param pageQuery
     */
    public void beforePageQuery(QueryDto pageQuery) throws Exception {
    }

    /**
     * 查询后
     *
     * @param datas
     * @return
     */
    public void afterPageQuery(List<T> datas) throws Exception {
    }

    /**
     * list查询前
     *
     * @param queryDto
     */
    public void beforeList(QueryDto queryDto) throws Exception {
    }

    /**
     * list查询后
     *
     * @param datas
     */
    public void afterList(List<T> datas) throws Exception {
    }


    private String getUserUUID() {
        UserInfo userInfo = WebUtil.getLoginUser();
        return ObjectUtil.isNull(userInfo) ? "system" : userInfo.getUuid();
    }

    public void deleteCustFieldByUuid(String uuid, String tableCode) throws SQLException {
        Connection connection = SpringContextUtil.getBean(SqlSessionFactory.class).openSession().getConnection();
        connection.createStatement().execute(String.format("delete from t_custom_field where uuid = '%s' and table_code = '%s'", uuid, tableCode));
    }

    public void saveCustField(String field, String tableCode, String uuid) throws SQLException {
        Connection connection = SpringContextUtil.getBean(SqlSessionFactory.class).openSession().getConnection();
        connection.createStatement().execute(String.format("insert into t_custom_field(field,table_code,uuid) values('%s','%s','%s')", field, tableCode, uuid));
    }

    public List<String> selectCustFieldByUuid(String uuid, String tableCode) throws SQLException {
        Connection connection = SpringContextUtil.getBean(SqlSessionFactory.class).openSession().getConnection();
        ResultSet rs = connection.createStatement().executeQuery(String.format("select field from t_custom_field where uuid = '%s' and table_code = '%s'", uuid, tableCode));
        List<String> fields = new LinkedList<>();
        while (rs.next()) {
            fields.add(rs.getString("field"));
        }
        return fields;
    }

    public void updateFieldById(Long id, String fieldName, Object value) throws Exception {
        updateField(id, null, null, fieldName, value);
    }

    public void updateFieldByPrimaryKey(String primaryKey, String primaryKeyValue, String fieldName, Object value) throws Exception {
        updateField(null, primaryKey, primaryKeyValue, fieldName, value);
    }

    private void updateField(Long id, String primaryKey, String primaryKeyValue, String fieldName, Object value) throws Exception {
        if (StringUtil.isNotEmpty(fieldName)) {
            Class joinDomainClass;
            T domainInstance = null;
            String whereSql = null;

            if (ObjectUtil.notNull(id)) {
                domainInstance = get(id);
            } else if (StringUtil.isNotEmpty(primaryKey)) {

                Field pky = ReflectUtil.getField(this.domainClass, primaryKey);

                if (pky.getType().equals(String.class)) {
                    whereSql = String.format(" %s = '%s'", primaryKey, primaryKeyValue);
                } else {
                    whereSql = String.format(" %s = %s", primaryKey, primaryKeyValue);
                }
                domainInstance = get(QueryDto.builder().available().addWhere(whereSql).build());
            }

            if (ObjectUtil.isNull(domainInstance)) {
                throw new BusinessException("沒有找要更新的数据，更新失败");
            }

            if (fieldName.contains(".")) {
                String[] temp = fieldName.split("\\.");
                Field field = ReflectUtil.getField(this.domainClass, temp[0]);
                if (field.getType().newInstance() instanceof BasicDomain) {
                    joinDomainClass = field.getType();
                } else {
                    throw new BusinessException("关联的对象不是BasicDomain类型，更新失败");
                }
                Object joinDomainInstance = getReturnData(domainInstance, ReflectUtil.getField(domainInstance.getClass(), temp[0]));
                if (ObjectUtil.isNull(joinDomainInstance)) {
                    throw new BusinessException("没有" + joinDomainClass.getName() + "的关联数据，更新失败");
                } else {
                    ReflectUtil.setFieldValue(joinDomainInstance, temp[1], value);
                    CommonService joinDomainService = SpringContextUtil.getServiceImplByDomain(joinDomainClass);
                    joinDomainService.updateBySelective((BasicDomain) joinDomainInstance);
                }

            } else {
                ReflectUtil.setFieldValue(domainInstance, fieldName, value);
                updateBySelective(domainInstance);
            }
        }
    }

}










