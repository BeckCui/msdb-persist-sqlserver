package com.dhl.fin.api.common.controller;

import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.util.FilesUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CuiJianbo
 * @date 2020.02.10
 */
@Slf4j
@RestController
@RequestMapping({"common"})
public class CommonController<T extends BasicDomain> {

    @Autowired
    protected CommonService<T> commonService;


    /**
     * 分页查询
     *
     * @param pageIndex 第几页
     * @param length    每页长度，默认10条数据
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("page/{pageIndex}")
    public ApiResponse pageQuery(@PathVariable("pageIndex") int pageIndex, @RequestParam(value = "length", defaultValue = "10") Integer length) throws Exception {
        Map responseData = null;
        responseData = commonService.pageQueryDomain(pageIndex, length);
        return ApiResponse.success(responseData);
    }

    /**
     * 查询
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("list")
    public ApiResponse list() throws Exception {
        List<T> responseData = null;
        responseData = commonService.selectDomain();
        return ApiResponse.success(responseData);
    }


    @ResponseBody
    @RequestMapping("get/{id}")
    public ApiResponse pageQuery(@PathVariable("id") Long id, String[] joinDomain) throws Exception {
        T domain = null;
        domain = commonService.getDomain(id, joinDomain);
        return ApiResponse.success(domain);
    }


    /**
     * 新增和更新
     *
     * @param domain
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("save")
    public ApiResponse saveOrUpdate(T domain) throws Exception {

        List<Field> fields = Arrays.stream(domain.getClass().getDeclaredFields())
                .filter(p -> p.getType().equals(String.class) && p.getName().endsWith("Str"))
                .collect(Collectors.toList());
        commonService.parseDate(domain, fields);
        String message = commonService.saveDomain(domain);
        if (StringUtil.isNotEmpty(message)) {
            return ApiResponse.error(message, NotifyTypeEnum.ALERT, MsgTypeEnum.ERROR);
        }
        return ApiResponse.success(domain);
    }

    /**
     * 导出前检查
     */
    @ResponseBody
    @RequestMapping("exportcheck")
    public ApiResponse exportCheck() {
        return ApiResponse.success();
    }

    /**
     * 生成导出文件
     */
    @ResponseBody
    @RequestMapping("export")
    public ApiResponse export() {
        try {
            String filePath = commonService.export();
            return ApiResponse.success(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("", ActionEnum.EXPORT, LogStatus.FAILED, commonService.recordLog());
        }
    }

    /**
     * 下载excel
     */
    @RequestMapping("downloadexcel")
    public void export(String filePath, HttpServletResponse response) {
        try {
            ExcelUtil.writeLocalToClient(filePath, response);
            FilesUtil.delete(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("", ActionEnum.GET_DOMAIN, LogStatus.FAILED, "导出失败");
        }
    }


    /**
     * 下载模板
     *
     * @param response
     */
    @RequestMapping("template")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        commonService.downloadTemplate(response);
    }


    /**
     * 批量导入
     *
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("import")
    public ApiResponse importDomain(MultipartHttpServletRequest request) throws Exception {
        return commonService.importDomain(request);
    }

    /**
     * 下载导入日志
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("download/importlog")
    public void downloadImportLog(HttpServletResponse response, String filePath) throws Exception {
        ExcelUtil.writeLocalToClient(filePath, response);
    }

    /**
     * 删除
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("delete/{id}")
    public ApiResponse delete(@PathVariable Long id) throws Exception {
        T domain = commonService.get(id);
        if (ObjectUtil.notNull(domain)) {
            commonService.deleteDomain(domain);
        }
        return ApiResponse.success();
    }

    /**
     * 批量删除
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("batchdelete")
    public ApiResponse batchDelete(Long[] ids) throws Exception {
        commonService.batchDelete(ids);
        return ApiResponse.success();
    }


    /**
     * 文件导入的方式批量删除
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("batchdel")
    public ApiResponse batchDel(MultipartHttpServletRequest request) throws Exception {
        commonService.batchDel(request);
        return ApiResponse.success();
    }


    /**
     * 下载批量删除的模板
     *
     * @param response
     */
    @ResponseBody
    @RequestMapping("batchdeltemplate")
    public void batchDeleteTemplate(HttpServletResponse response) throws Exception {
        commonService.batchDeleteTemplate(response);
    }


    @ResponseBody
    @RequestMapping({"savecustfields"})
    public ApiResponse saveFields(String[] fields) throws SQLException {
        String uuid = WebUtil.getLoginUser().getUuid();
        String tableCode = WebUtil.getStringParam("tableCode");

        commonService.deleteCustFieldByUuid(uuid, tableCode);

        Arrays.stream(fields).forEach(p -> {
            try {
                commonService.saveCustField(p, tableCode, uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return ApiResponse.success();
    }

    @ResponseBody
    @RequestMapping({"getfields"})
    public ApiResponse getFields() throws Exception {
        String uuid = WebUtil.getLoginUser().getUuid();
        String tableCode = WebUtil.getStringParam("tableCode");
        List<String> fields = commonService.selectCustFieldByUuid(uuid, tableCode);
        return ApiResponse.success(fields);
    }

    @ResponseBody
    @RequestMapping({"updatefield"})
    public ApiResponse updateField(Long id, String primaryKey, String primaryKeyValue, String key, String value) throws Exception {

        if (ObjectUtil.notNull(id)) {
            commonService.updateFieldById(id, key, value);
        } else if (StringUtil.isNotEmpty(primaryKey) && StringUtil.isNotEmpty(primaryKeyValue)) {
            commonService.updateFieldByPrimaryKey(primaryKey, primaryKeyValue, key, value);
        }


        return ApiResponse.success();
    }
}






