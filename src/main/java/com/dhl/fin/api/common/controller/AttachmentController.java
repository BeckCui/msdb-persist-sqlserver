package com.dhl.fin.api.common.controller;

import com.dhl.fin.api.common.domain.AttachFile;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.enums.*;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.service.AttachFileServiceImpl;
import com.dhl.fin.api.common.util.FilesUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.ftp.FTPUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;


/**
 * @author CuiJianbo
 * @date 2020.02.05
 */
@RestController
@RequestMapping("attachment")
public class AttachmentController {


    @Autowired
    private AttachFileServiceImpl attachFileService;


    /**
     * 上传文件
     *
     * @param request
     */

    @ResponseBody
    @RequestMapping("upload")
    public ApiResponse upload(MultipartHttpServletRequest request, String storeLocation) throws Exception {

        AttachFile attachFile = attachFileService.uploadToLocal(request);
        String checkResult = attachFileService.checkUploadFile(attachFile);
        if (StringUtil.isNotEmpty(checkResult)) {
            String localFile = attachFile.getDirectory() + File.separator + attachFile.getFileName();
            FilesUtil.delete(localFile);
            return ApiResponse.error(checkResult, NotifyTypeEnum.MESSAGE, MsgTypeEnum.ERROR);
        }

        if (ObjectUtil.notNull(storeLocation) && storeLocation.equals(StoreLocationEnum.FTP)) {
            attachFileService.uploadToFTP(attachFile);
            attachFile.setStoreLocation(StoreLocationEnum.FTP.getName());
        }

        attachFileService.insert(attachFile);

        return ApiResponse.success(attachFile);

    }

    @RequestMapping("download/{id}")
    public void download(HttpServletResponse response, @PathVariable Long id) throws Exception {
        AttachFile attachFile = attachFileService.get(id);
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(attachFile.getFileName(), "UTF-8"));

        String location = attachFile.getStoreLocation();
        String filePath = attachFile.getDirectory() + File.separator + attachFile.getMd5();
        if (StringUtil.isNotEmpty(location)) {
            if (location.equals(StoreLocationEnum.LOCAL.getName())) {
                File targetFile = FileUtils.getFile(filePath);
                FileUtils.copyFile(targetFile, response.getOutputStream());
            } else if (location.equals(StoreLocationEnum.FTP.getName())) {
                FTPUtil.build().downLoadToClient(filePath, response);
            }
        }
    }


    @ResponseBody
    @RequestMapping("/file/{id}")
    public ApiResponse getFileById(@PathVariable Long id) throws Exception {
        return ApiResponse.success(attachFileService.get(id));
    }

    @ResponseBody
    @RequestMapping("/files")
    public ApiResponse getFiles(@RequestParam Long[] ids) throws Exception {
        return ApiResponse.success(attachFileService.findDomainListByIds(ids));
    }

    @ResponseBody
    @RequestMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Long id) throws Exception {
        attachFileService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 从本地下载excel
     */
    @RequestMapping("download")
    public void downloadExcel(String filePath, HttpServletResponse response) {
        try {
            FilesUtil.writeFileToClient(new File(filePath), response);
            FilesUtil.delete(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("", ActionEnum.EXPORT, LogStatus.FAILED, "导出失败");
        }
    }


}
