package com.dhl.fin.api.common.filters;

import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.config.WebConfig;
import com.dhl.fin.api.common.dto.ApiResponseStatus;
import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * OncePerRequestFilter 基类
 */
public abstract class OncePerRequestFilterBase extends OncePerRequestFilter {
    protected WebConfig webConfig;
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    public OncePerRequestFilterBase(WebConfig webConfig) {
        this.webConfig = webConfig;
        pathMatcher.setCaseSensitive(false);
    }

    private boolean contains(String[] apiList, String path) {
        return !(apiList == null || apiList.length == 0)
                && Arrays.stream(apiList).anyMatch(p -> pathMatcher.match(p, path));
    }

    protected final void writeMsgResponse(HttpServletResponse response,
                                          String errorMessage,
                                          NotifyTypeEnum notifyTypeEnum,
                                          MsgTypeEnum msgTypeEnum
    ) throws IOException {

        ApiResponse<String> unauthorizedResponse = msgTypeEnum.equals(MsgTypeEnum.ERROR) ? ApiResponse.error(errorMessage, notifyTypeEnum, msgTypeEnum) : new ApiResponse<>(ApiResponseStatus.INVALID_TOKEN, errorMessage, errorMessage);
        String json = new ObjectMapper().writeValueAsString(unauthorizedResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(json);
    }

    protected final boolean allowAnonymous(String path) {
        return this.contains(this.webConfig.allowAnonymousApiList, path);
    }

    protected final boolean inInternalAuthApiList(String path) {
        return this.contains(this.webConfig.internalAuthApiList, path);
    }

    protected final boolean inAppCredentialApiList(String path) {
        return this.contains(this.webConfig.appCredentialApiList, path);
    }

    protected final boolean inXAppAuthApiList(String path) {
        return this.contains(this.webConfig.xappAuthApiList, path);
    }
}