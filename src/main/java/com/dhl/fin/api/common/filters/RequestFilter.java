package com.dhl.fin.api.common.filters;

import com.dhl.fin.api.common.config.WebConfig;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 7/18/2020
 */
@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilterBase {


    @Autowired
    private RedisService redisService;

    public RequestFilter(WebConfig webConfig) {
        super(webConfig);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        if (!httpServletRequest.getRequestURI().contains("/ws/")) {
            recordRequestTimes();
        }

        HttpServletRequest modifiedRequest = new SomeHttpServletRequest(httpServletRequest);

        filterChain.doFilter(modifiedRequest, httpServletResponse);

    }


    private void recordRequestTimes() {
        String appCode = SpringContextUtil.getPropertiesValue("custom.projectCode");
        countActiveUser(appCode);
    }


    /**
     * 统计活跃用户
     *
     * @param appCode
     */
    public void countActiveUser(String appCode) {
        Map appActiveUsers = redisService.getMap(CacheKeyEnum.ACTIVE_USER);
        String uuid = WebUtil.getLoginUser().getUuid();

        if (StringUtil.isEmpty(uuid)) {
            return;
        }

        if (ObjectUtil.isNull(appActiveUsers)) {
            appActiveUsers = new HashMap();
        }

        Map appKV = MapUtil.getMap(appActiveUsers, appCode);
        Map app = redisService.getProject(appCode);
        String appEnName = ObjectUtil.notNull(app) ? MapUtil.getString(app, "enName") : null;
        Map users = null;
        Map loginUsers = null;
        if (ObjectUtil.isNull(appKV)) {
            appKV = new HashMap();
            users = new HashMap<>();
            loginUsers = new HashMap<>();
            appKV.put("users", users);
            appKV.put("loginUsers", loginUsers);
        } else {
            users = MapUtil.getMap(appKV, "users");
            loginUsers = MapUtil.getMap(appKV, "loginUsers");
            if (ObjectUtil.isNull(loginUsers)) {
                loginUsers = new HashMap();
            }
            if (ObjectUtil.isNull(users)) {
                users = new HashMap();
            }
        }


        appKV.put("appEnName", appEnName);

        users.put(uuid, DateUtil.getFullTime(new Date()));
        loginUsers.put(uuid, DateUtil.getFullTime(new Date()));

        appActiveUsers.put(appCode, appKV);

        redisService.put(CacheKeyEnum.ACTIVE_USER, appActiveUsers);
    }


    class SomeHttpServletRequest extends HttpServletRequestWrapper {
        HttpServletRequest request;

        SomeHttpServletRequest(final HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public String getQueryString() {
            String queryString = request.getQueryString();

            if (StringUtil.isNotEmpty(queryString)) {
                queryString = queryString.replaceAll("\\+", "%2B");
            }

            return queryString;
        }

        @Override
        public String getParameter(final String name) {
            Map<String, String[]> parameterMap = getParameterMap();
            String[] params = ObjectUtil.isNull(parameterMap) ? null : parameterMap.get(name);
            return ObjectUtil.isNull(params) ? null : params[0];
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            String queryString = getQueryString();
            return StringUtil.isEmpty(queryString) ? this.request.getParameterMap() : getParamsFromQueryString(queryString);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            Map<String, String[]> params = getParameterMap();
            return ObjectUtil.isNull(params) ? this.request.getParameterNames() : Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public String[] getParameterValues(final String name) {
            Map<String, String[]> params = getParameterMap();
            return ObjectUtil.isNull(params) ? this.request.getParameterValues(name) : getParameterMap().get(name);
        }

        private Map<String, String[]> getParamsFromQueryString(final String queryString) {
            Map<String, List<String>> collect = Arrays.stream(queryString.split("&"))
                    .map(x -> x.split("="))
                    .filter(x -> x.length > 1)
                    .collect(Collectors.groupingBy(
                            x -> x[0],
                            Collectors.mapping(x -> x.length > 1 ? x[1] : null, Collectors.toList())
                    ));

            Map<String, String[]> result = MapUtil.builder().build();
            for (Map.Entry<String, List<String>> stringListEntry : collect.entrySet()) {
                String key = stringListEntry.getKey();
                List<String> value = stringListEntry.getValue();

                if (CollectorUtil.isEmpty(value) || StringUtil.isEmpty(key)) {
                    continue;
                }

                try {
                    String[] valueArray = null;
                    for (String s : value) {
                        if (s.contains(",")) {
                            String[] arrayValue = s.split(",");
                            for (int i = 0; i < arrayValue.length; i++) {
                                arrayValue[i] = URLDecoder.decode(arrayValue[i], "UTF-8");
                            }
                            valueArray = arrayValue;
                        } else {
                            valueArray = new String[]{URLDecoder.decode(s, "UTF-8")};
                        }

                    }
                    result.put(key, valueArray);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }
    }

}




