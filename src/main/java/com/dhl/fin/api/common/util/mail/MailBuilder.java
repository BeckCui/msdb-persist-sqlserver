package com.dhl.fin.api.common.util.mail;

import cn.hutool.core.io.FileUtil;
import com.dhl.fin.api.common.exception.mail.MailTotalFileSizeOverLongException;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.StringUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 7/29/2020
 */
public class MailBuilder {


    MailUtil mailUtil = new MailUtil();

    public MailBuilder addTo(String to) {
        if (StringUtil.isNotEmpty(to)) {
            mailUtil.getMailBean().getTo().add(to);
        }
        return this;
    }

    public MailBuilder addAllTo(List<String> to) {
        if (CollectorUtil.isNoTEmpty(to)) {
            mailUtil.getMailBean().getTo().addAll(to);
        }
        return this;
    }

    public MailBuilder setFrom(String from) {
        if (StringUtil.isNotEmpty(from)) {
            mailUtil.getMailBean().setFrom(from);
        }
        return this;
    }

    public MailBuilder addCC(String cc) {
        if (StringUtil.isNotEmpty(cc)) {
            mailUtil.getMailBean().getCc().add(cc);
        }
        return this;
    }

    public MailBuilder addAllCC(List<String> cc) {
        if (CollectorUtil.isNoTEmpty(cc)) {
            mailUtil.getMailBean().getCc().addAll(cc);
        }
        return this;
    }

    public MailBuilder setTitle(String title) {
        title = StringUtil.isNotEmpty(title) ? title : "non subject";
        mailUtil.getMailBean().setTitle(title);
        return this;
    }

    public MailBuilder setContent(String content) {
        if (StringUtil.isEmpty(content)) {
            return this;
        }

        mailUtil.getMailBean().setContent(content);
        return this;
    }


    public MailBuilder setTemplate(String template) {
        if (StringUtil.isEmpty(template)) {
            return this;
        }

        template = template.endsWith(".ftl") ? template : template + ".ftl";
        mailUtil.getMailBean().setTemplate(template);
        return this;
    }


    public MailBuilder setTemplateParams(Map params) {
        mailUtil.getMailBean().setTemplateParams(params);
        return this;
    }

    public MailBuilder addTemplateParams(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            return this;
        }
        mailUtil.getMailBean().getTemplateParams().put(key, value);
        return this;
    }


    public MailBuilder addAttachFiles(File file) {
        long fileSize = FileUtil.size(file);
        Long totalSize = mailUtil.getMailBean().getAttachFiles().stream().map(p -> FileUtil.size(file)).reduce((x, y) -> x + y).orElse(0L);
        if (totalSize + fileSize > 1000 * 1000 * 10) {
            throw new MailTotalFileSizeOverLongException();
        }

        mailUtil.getMailBean().getAttachFiles().add(file);
        return this;
    }

    public MailBuilder addAllAttachFiles(List<File> files) {
        if (CollectorUtil.isNoTEmpty(files)) {
            for (File file : files) {
                addAttachFiles(file);
            }
        }
        return this;
    }

    public MailBuilder addImg(String id, String filePath) {
        if (StringUtil.isNotEmpty(id)) {
            mailUtil.getMailBean().getImgs().put(id, filePath);
        }

        return this;
    }


//    public MailBuilder addImg(String id, InputStream fileInputStream) {
//
//        if (StringUtil.isNotEmpty(id)) {
//            mailUtil.getMailBean().getImgs().put(id, fileInputStream);
//        }
//
//        return this;
//    }

    public MailUtil build() {
        return mailUtil;
    }

}
















