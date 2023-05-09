package com.dhl.fin.api.common.util.mail;

import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.exception.mail.MailSendFailedException;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 7/29/2020
 */
@Data
public class MailUtil {

    private MailBean mailBean = new MailBean();

    public static MailBuilder builder() {
        return new MailBuilder();
    }

    public void send() throws MessagingException, IOException, TemplateException, MailSendFailedException {

        System.setProperty("mail.mime.splitlongparameters", "false");
        String env = SpringContextUtil.getPropertiesValue("spring.profiles.active");

        List<String> toList = mailBean.getTo();
        String from = mailBean.getFrom();
        from = StringUtil.isEmpty(from) ? "FINTP@dhl.com" : from;

        if (CollectorUtil.isEmpty(toList)) {
            return;
        }

        FreeMarkerConfigurer freemarkerConfigurer = SpringContextUtil.getBean(FreeMarkerConfigurer.class);
        JavaMailSender emailSender = SpringContextUtil.getBean(JavaMailSender.class);


        String htmlBody = "  ";
        if (StringUtil.isNotEmpty(mailBean.getTemplate())) {
            Template freemarkerTemplate = freemarkerConfigurer.getConfiguration().getTemplate(mailBean.getTemplate());
            htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, mailBean.getTemplateParams());
        } else if (StringUtil.isNotEmpty(mailBean.getContent())) {
            htmlBody = mailBean.getContent();
        }


        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setSubject(mailBean.getTitle());
        helper.setText(htmlBody, true);


        //添加接受者
        for (String to : toList) {
            if (StringUtil.isNotEmpty(to)) {
                if (!env.equalsIgnoreCase("prod") && !to.contains("@dhl.com")) {
                    throw new BusinessException("测试环境不能给非DHL(" + to + ")的人发送邮件");
                }
                helper.addTo(to);
            }
        }


        //添加cc
        List<String> ccList = mailBean.getCc();
        for (String cc : ccList) {
            if (StringUtil.isNotEmpty(cc)) {
                if (!env.equalsIgnoreCase("prod") && !cc.contains("@dhl.com")) {
                    throw new BusinessException("测试环境不能给非DHL(" + cc + ")的人发送邮件");
                }
                helper.addCc(cc);
            }
        }


        //添加附件
        List<File> attachFiles = mailBean.getAttachFiles();
        for (File attachFile : attachFiles) {
            helper.addAttachment(MimeUtility.encodeWord(attachFile.getName()), new FileSystemResource(attachFile));
        }

        //添加图片
        ApplicationHome applicationHome = new ApplicationHome(MailUtil.class);
        Map<String, String> imgMap = mailBean.getImgs();
        int n = 0;
        for (Map.Entry<String, String> item : imgMap.entrySet()) {
            String id = item.getKey();
            String filePath = item.getValue();
            InputStream is = new ClassPathResource(filePath).getInputStream();
            if (ObjectUtil.notNull(applicationHome.getSource())) {
                String rootPath = applicationHome.getSource().getParentFile().toString();
                File f = new File(rootPath + "\\" + n + ".png");
                FileUtils.copyInputStreamToFile(is, f);
                helper.addInline(id, f);
            } else {
                File file = ResourceUtils.getFile("classpath:" + filePath);
                helper.addInline(id, file);
            }
        }
        try {
            emailSender.send(message);
        } catch (MailException e) {
            throw new MailSendFailedException(e.getMessage());
        }

    }


}













