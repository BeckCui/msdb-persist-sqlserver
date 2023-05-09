package com.dhl.fin.api.common.service;

import com.dhl.fin.api.common.dto.UserInfo;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author becui
 * @date 6/12/2020
 */
@Service
public class LogService {

    @Autowired
    private RedisService redisService;


    public void log(ActionEnum actionEnum, LogStatus logStatus, String remark, String tableName) {

        String uuid;

        if (!"t_log".equalsIgnoreCase(tableName)) {

            HttpServletRequest request = WebUtil.getRequest();
            if (request instanceof MockHttpServletRequest) {
                return;
            }

            Boolean flag = ObjectUtil.isNull(request) ? false : true;
            UserInfo userInfo = WebUtil.getLoginUser();
            if (ObjectUtil.notNull(WebUtil.getRequest()) && WebUtil.getRequest().getRequestURI().endsWith("login/login")) {
                uuid = WebUtil.getStringParam("userName");
            } else if (ObjectUtil.isNull(userInfo)) {
                uuid = "system";
            } else {
                uuid = userInfo.getUuid();
            }
            String menu = flag ? WebUtil.getStringParam("menu") : "定时器";
            String ip = flag ? WebUtil.getClientIP() : "";
            String browser = flag ? WebUtil.getClientBrowser() : "";
            String os = flag ? WebUtil.getClientOs() : "";
            String appCode = SpringContextUtil.getPropertiesValue("custom.projectCode");
            String appName = "";
            Map project = redisService.getProject(appCode);
            if (ObjectUtil.notNull(project)) {
                appName = MapUtil.getString(project, "name");
            }
            /*fintpService.log(logBean.newBuilder()
                    .setAction(actionEnum.getCode())
                    .setStatus(logStatus.getCode())
                    .setRemark(StringUtil.isEmpty(remark) ? "无" : remark)
                    .setTable(StringUtil.isEmpty(tableName) ? "无" : tableName)
                    .setMenu(StringUtil.isEmpty(menu) ? "无" : menu)
                    .setIp(StringUtil.isEmpty(ip) ? "无" : ip)
                    .setOs(StringUtil.isEmpty(os) ? "无" : os)
                    .setBrowser(StringUtil.isEmpty(browser) ? "无" : browser)
                    .setUuid(uuid)
                    .setAppCode(appCode)
                    .setAppName(appName)
                    .build(), new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });*/
        }
    }


}
