package com.dhl.fin.api.common.service;

import cn.hutool.core.io.FileUtil;
import com.dhl.fin.api.common.domain.AttachFile;
import com.dhl.fin.api.common.enums.StoreLocationEnum;
import com.dhl.fin.api.common.util.FilesUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.common.util.ftp.FTPUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by CuiJianbo on 2020.02.20.
 */
@Primary
@Service
@Transactional(rollbackFor = Exception.class)
public class AttachFileServiceImpl extends CommonService<AttachFile> {

    @Value("${custom.uploadPath}")
    private String fileUploadPath;


    public void insert(AttachFile domain) throws SQLException {
        SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
        String uuid = WebUtil.getLoginUser().getUuid();
        String sql = String.format("insert into t_attachfile(create_time,create_user,update_time,update_user,directory,file_name,md5,size,type,store_location) " +
                        "values(getDate(),'%s',getDate(),'%s','%s','%s','%s',%s,'%s','%s')",
                uuid, uuid, domain.getDirectory(), domain.getFileName(), domain.getMd5(), domain.getSize(), domain.getType(), domain.getStoreLocation());

        String columnNames[] = new String[]{"id"};
        PreparedStatement statement = sqlSessionFactory.openSession().getConnection().prepareStatement(sql, columnNames);
        statement.executeUpdate();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            Long primKey = generatedKeys.getLong(1);
            domain.setId(primKey);
        }

    }

    public AttachFile get(Long id) throws SQLException {
        SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
        Statement statement = sqlSessionFactory.openSession().getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("select * from t_attachfile where id = " + id);
        if (resultSet.next()) {
            return getDomain(resultSet);
        }
        return null;
    }

    public List<AttachFile> findDomainListByIds(Long[] ids) throws SQLException {
        SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
        Statement statement = sqlSessionFactory.openSession().getConnection().createStatement();
        String idStr = Arrays.stream(ids).map(Objects::toString).collect(Collectors.joining(","));
        ResultSet resultSet = statement.executeQuery("select * from t_attachfile where id in (" + idStr + ")");
        List<AttachFile> attachFiles = new LinkedList<>();
        while (resultSet.next()) {
            attachFiles.add(getDomain(resultSet));
        }
        return attachFiles;
    }

    private AttachFile getDomain(ResultSet resultSet) throws SQLException {
        String directory = resultSet.getString("directory");
        String fileName = resultSet.getString("file_name");
        String md5 = resultSet.getString("md5");
        Long size = resultSet.getLong("size");
        String type = resultSet.getString("type");
        Long id = resultSet.getLong("id");
        String storeLocation = resultSet.getString("store_location");
        AttachFile attachFile = new AttachFile();
        attachFile.setId(id);
        attachFile.setDirectory(directory);
        attachFile.setFileName(fileName);
        attachFile.setMd5(md5);
        attachFile.setSize(size);
        attachFile.setType(type);
        attachFile.setStoreLocation(storeLocation);
        return attachFile;
    }

    public void delete(Long id) throws SQLException {
        SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
        Statement statement = sqlSessionFactory.openSession().getConnection().createStatement();


        AttachFile file = get(id);
        String filePath = file.getDirectory() + File.separator + file.getMd5();
        FilesUtil.delete(filePath);

        statement.execute("delete from t_attachfile where id = " + id);

    }

    public Long saveIntoDB(File file) throws IOException, SQLException {

        BufferedInputStream fileInputStream = FileUtil.getInputStream(file);
        String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));
        String fileName = file.getName();
        AttachFile attachFile = new AttachFile();
        attachFile.setFileName(fileName);
        attachFile.setMd5(md5);
        attachFile.setDirectory(file.getParent());
        attachFile.setSize(file.length());
        attachFile.setType(fileName.substring(fileName.lastIndexOf(".") + 1));

        insert(attachFile);


        return attachFile.getId();

    }


    public AttachFile uploadToLocal(MultipartHttpServletRequest request) throws IOException {
        MultipartFile file = request.getFileMap().get("file");
        InputStream in = file.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        bis.mark(bis.available() + 1);
        String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(bis));
        String fileOrgName = file.getOriginalFilename();
        AttachFile attachFile = new AttachFile();
        attachFile.setFileName(fileOrgName);
        attachFile.setMd5(md5);
        attachFile.setSize(file.getSize());
        attachFile.setType(fileOrgName.substring(fileOrgName.lastIndexOf(".") + 1));
        attachFile.setStoreLocation(StoreLocationEnum.LOCAL.getName());


        String targetDir;
        String storeDir = WebUtil.getStringParam("storeDir");
        boolean notUserMD5 = WebUtil.getBooleanParam("notUserMD5");

        if (StringUtil.isEmpty(storeDir)) {
            targetDir = fileUploadPath + File.separator + "default";
        } else {
            if (storeDir.matches("^[a-z|A-Z]{1}:.+") || storeDir.matches("^/.+")) {
                targetDir = storeDir;
            } else {
                targetDir = fileUploadPath + File.separator + storeDir;
            }
        }

        String fileName = notUserMD5 ? attachFile.getFileName() : md5;
        File targetFile = new File(targetDir + File.separator + fileName);

        bis.reset();
        FileUtils.copyInputStreamToFile(bis, targetFile);

        attachFile.setMd5(fileName);
        attachFile.setDirectory(targetDir);

        return attachFile;
    }

    public void uploadToFTP(AttachFile attachFile) throws IOException {
        String targetDir;
        String storeDir = WebUtil.getStringParam("storeDir");
        String ftpPath = SpringContextUtil.getPropertiesValue("custom.ftp.path");

        targetDir = StringUtil.isNotEmpty(storeDir) ? ftpPath + File.separator + storeDir : ftpPath;

        boolean notUserMD5 = WebUtil.getBooleanParam("notUserMD5");
        String fileName = notUserMD5 ? attachFile.getFileName() : attachFile.getMd5();

        String ftpFileName = StringUtil.isNotEmpty(storeDir) ? storeDir + File.separator + fileName : fileName;

        String localFile = attachFile.getDirectory() + File.separator + attachFile.getFileName();
        FTPUtil.build().uploadFromLocal(ftpFileName, localFile);
        FilesUtil.delete(localFile);

        attachFile.setDirectory(targetDir);
        attachFile.setMd5(fileName);

    }


    public String checkUploadFile(AttachFile attachFile) {
        return null;
    }


}












