package com.dhl.fin.api.common.filters;

import com.dhl.fin.api.common.authentication.TokenAuthentication;
import com.dhl.fin.api.common.authentication.TokenService;
import com.dhl.fin.api.common.config.WebConfig;
import com.dhl.fin.api.common.dto.UserInfo;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import com.dhl.fin.api.common.exception.SqlInjectionException;
import com.dhl.fin.api.common.exception.UserFriendlyException;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.ArrayUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Token based authentication filter
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilterBase {

    private TokenService tokenService;

    @Autowired
    private RedisService redisService;

    @Autowired
    public TokenAuthenticationFilter(
            WebConfig webConfig,
            TokenService tokenService
    ) {
        super(webConfig);

        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return allowAnonymous(request.getServletPath()) || inAppCredentialApiList(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        try {
            String userId = tokenService.verifyRequest(request);
//            checkSqlInj();

            if (StringUtil.isEmpty(userId)) {
                writeMsgResponse(response, "Token validation failed", NotifyTypeEnum.MESSAGE, MsgTypeEnum.SUCCESS);
                return;
            } else {
                TokenAuthentication auth = new TokenAuthentication(userId, null);
                SecurityContextHolder.getContext().setAuthentication(auth);
                if (checkAppIsForbidden()) {
                    writeMsgResponse(response, "应用以被禁用，暂时不可使用。", NotifyTypeEnum.MESSAGE, MsgTypeEnum.ERROR);
                    return;
                }
            }

        } catch (UserFriendlyException ex) {
            writeMsgResponse(response, ex.getMessage(), NotifyTypeEnum.NOTIFY, MsgTypeEnum.ERROR);
            return;
        } catch (Exception e) {
            writeMsgResponse(response, "Token validation failed.", NotifyTypeEnum.NOTIFY, MsgTypeEnum.ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查sql注入
     */
    public void checkSqlInj() {

        String inj_str = "'|\"| and |exec| insert | select | delete |drop| update |count|*|#|chr|mid|master|truncate|char|declare| ; | - |+";

        String inj_stra[] = inj_str.split("\\|");

        Map<String, String[]> params = WebUtil.getRequestParams();
        for (Map.Entry<String, String[]> o : params.entrySet()) {
            for (int i = 0; i < inj_stra.length; i++) {
                if (ArrayUtil.isNotEmpty(o.getValue()) && StringUtil.isNotEmpty(o.getValue()[0])) {
                    String v = o.getValue()[0];
                    if (v.indexOf(inj_stra[i]) >= 0) {
                        throw new SqlInjectionException();
                    }
                }
            }
        }
    }


    /**
     * 检查app是否被禁用
     */
    public boolean checkAppIsForbidden() {
        String[] url = WebUtil.getRequest().getContextPath().split("/");
        String context = url[url.length - 1];
        String apps = redisService.getString(CacheKeyEnum.FORBIDDEN_APP);
        UserInfo userInfo = WebUtil.getLoginUser();
        String activeCode = userInfo.getActiveRole().getCode();

        String uuid = userInfo.getUuid();

        Boolean isSuperManager = userInfo.getAdminType().equals("superManager");


        if (!isSuperManager && StringUtil.isNotEmpty(apps) && apps.toLowerCase().contains(context)) {

            return !Arrays.stream(apps.split("&"))
                    .filter(p -> p.contains(context + "-"))
                    .map(p -> ";" + p.replace(context + "-", "").replace("-", ";") + ";")
                    .anyMatch(p -> p.contains(";" + uuid + ";") || p.contains(";" + activeCode + ";"));

        } else {
            return false;
        }
    }


}
