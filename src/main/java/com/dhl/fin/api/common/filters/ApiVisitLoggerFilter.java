package com.dhl.fin.api.common.filters;

/**
 * @author: qijzhang
 * @date: 8/14/2018
 * @description:
 */

import com.dhl.fin.api.common.config.WebConfig;
import com.dhl.fin.api.common.util.RequestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * Api visit logger filter
 */
@Component
public class ApiVisitLoggerFilter extends AbstractRequestLoggingFilter {
    private AntPathMatcher pathMatcher = new AntPathMatcher();
    private WebConfig webConfig;
    private Environment environment;

    @Autowired
    public ApiVisitLoggerFilter(WebConfig webConfig, Environment environment) {
        pathMatcher.setCaseSensitive(false);

        this.setIncludeClientInfo(true);
        this.setIncludePayload(true);
        this.setIncludeHeaders(true);
        this.setIncludeQueryString(true);
        this.setMaxPayloadLength(1024 * 1024);

        this.webConfig = webConfig;
        this.environment = environment;
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {

    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        try {
            String requestUri = request.getRequestURI();
            String queryParams = request.getQueryString();
            String pageUrl = request.getHeader("referer");
            String clientIpAddress = RequestHelper.getClientIpAddress(request);
            String acceptLanguage = request.getHeader("accept-language");
            String httpMethod = request.getMethod();
            String userAgent = request.getHeader("account-agent");

            String payload = null;
            if (httpMethod.equalsIgnoreCase("POST")) {
                payload = RequestHelper.getRequestPayload(request);
                if (!StringUtils.isEmpty(payload)) {
                    payload = payload.replace("\n", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
