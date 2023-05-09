package com.dhl.fin.api.common.filters;

import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.exception.ConnectionException;
import com.dhl.fin.api.common.exception.MessageAlert;
import com.dhl.fin.api.common.exception.SqlInjectionException;
import com.dhl.fin.api.common.service.LogService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.mail.MailUtil;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.NestedServletException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 6/4/2020
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private LogService logService;

    @Autowired
    private RedisService redisService;

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ApiResponse handleException(Exception e, HttpServletResponse response) throws TemplateException, IOException, MessagingException {
        String uuid = WebUtil.getLoginUser().getUuid();
        e.printStackTrace();

        if (StringUtil.isNotEmpty(e.getMessage()) && e.getMessage().equalsIgnoreCase("Could not find acceptable representation")) {
            return ApiResponse.success();
        }


        log.error(String.format("account: %s, Error Message: %s ", uuid, e.getMessage()));

        if (ObjectUtil.notNull(e.getCause())) {
            log.error(String.format(" Cause: %s", e.getCause().getMessage()));
        }

        Arrays.stream(e.getStackTrace()).forEach(p -> {
            log.error("    " + p.toString());
        });


        String appcode = SpringContextUtil.getPropertiesValue("custom.projectCode");
        String springEnv = SpringContextUtil.getPropertiesValue("spring.profiles.active");
        RedisService redisService = SpringContextUtil.getBean(RedisService.class);
        Map project = redisService.getProject(appcode);
        String appName = ObjectUtil.notNull(project) ? MapUtil.getString(project, "name") : appcode;
        List<String> toList = new LinkedList<>();

        List<Map> managers = redisService.getAllSuperMangers();
        if (CollectorUtil.isNoTEmpty(managers)) {
            toList = managers.stream().map(p -> MapUtil.getString(p, "email")).filter(StringUtil::isNotEmpty).collect(Collectors.toList());
        } else {
            toList.add("beck.cui@dhl.com");
        }

        InetAddress addr = InetAddress.getLocalHost();

        String content = String.format("服务器地址：%s<br/>环境: %s<br/>Module：%s<br/>异常信息：<br/>", addr.getHostAddress(), springEnv, appName);

        if (e instanceof ConnectionException) {
            MailUtil.builder()
                    .addAllTo(toList)
                    .setTitle("连接失败")
                    .setContent(content + e.getMessage())
                    .build().send();
            return ApiResponse.error("connection failed");
        } else if (e instanceof SqlInjectionException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ApiResponse.error(e.getMessage());
        } else if (e instanceof NestedServletException && e.getCause().getMessage().contains("GC overhead limit exceeded")) {
            MailUtil.builder()
                    .addAllTo(toList)
                    .setTitle("内存溢出")
                    .setContent(content + String.format("发生了内存溢出，当前使用人是%s, 请查看应用运行状况和日志", uuid))
                    .build().send();
            return ApiResponse.error("内存溢出");
        } else if (e instanceof BusinessException) {
            BusinessException businessException = (BusinessException) e;
            ActionEnum actionEnum = businessException.getActionEnum();
            LogStatus logStatus = businessException.getLogStatus();
            String tableName = businessException.getTableName();
            String remark = businessException.getMessage();
            logService.log(actionEnum, logStatus, remark, tableName);
            return ApiResponse.error(e.getMessage(), NotifyTypeEnum.ALERT, MsgTypeEnum.ERROR);
        } else if (e instanceof MessageAlert) {
            MessageAlert messageAlert = (MessageAlert) e;
            String msg = messageAlert.getMessage();
            NotifyTypeEnum notifyTypeEnum = messageAlert.getNotifyTypeEnum();
            MsgTypeEnum msgTypeEnum = messageAlert.getMsgTypeEnum();
            return ApiResponse.error(msg, notifyTypeEnum, msgTypeEnum);
        } else {
            return ApiResponse.error("后台异常");
        }
    }
}



