package com.dhl.fin.api.common.util;

import com.dhl.fin.api.common.dto.UserInfo;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class WebUtil {


    public static final String FIRST_LOGIN_ACCESS = "firstLoginAccess";

    private static String CTX;

    private static SessionRegistry sessionRegistry;


    public static HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
                return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            }
        }
        return null;
    }

    public static MultipartHttpServletRequest getMulRequet() {
        HttpServletRequest request = getRequest();
        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        return resolver.resolveMultipart(request);
    }

    public static boolean containsKey(String key) {
        if (StringUtil.isNotEmpty(key)) {
            return MapUtil.hasKey(getRequestParams(), key);
        }
        return false;
    }

    public static Map<String, String[]> getRequestParams() {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : request.getParameterMap();
    }

    public static Integer getIntegerParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getInteger(request.getParameterMap(), key);
    }

    public static Integer[] getIntegerArrayParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getIntegerArray(request.getParameterMap(), key);
    }

    public static String getStringParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getString(request.getParameterMap(), key);
    }

    public static String[] getStringArrayParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getStringArray(request.getParameterMap(), key);
    }

    public static Long getLongParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getLong(request.getParameterMap(), key);
    }

    public static Long[] getLongArrayParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getLongArray(request.getParameterMap(), key);
    }

    public static Boolean getBooleanParam(String key) {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : MapUtil.getBoolean(request.getParameterMap(), key);
    }

    public static UserInfo getLoginUser() {
        if (ObjectUtil.notNull(getRequest()) && ObjectUtil.notNull(getRequest().getAttribute("loginUser"))) {
            return (UserInfo) getRequest().getAttribute("loginUser");
        } else {
            return UserInfo.builder().userName("system").uuid("system").build();
        }
    }


    public static String getProjectRoot() {
        HttpServletRequest request = getRequest();
        return ObjectUtil.isNull(request) ? null : getRequest().getServletContext().getRealPath("/");
    }

    public static Object getSessionAttribute(String key) {
        HttpServletRequest request = getRequest();

        return ObjectUtil.isNull(request) ? null : request.getSession().getAttribute(key);

    }

    public static void putSessionAttribute(String key, Object value) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            request.getSession().setAttribute(key, value);
        }
    }


    /**
     * 获取登录用户IP地址
     *
     * @return
     */
    public static String getClientIP() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            String s = request.getHeader("X-Forwarded-For");
            if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {
                s = request.getHeader("Proxy-Client-IP");
            }
            if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {
                s = request.getHeader("WL-Proxy-Client-IP");
            }
            if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {
                s = request.getHeader("HTTP_CLIENT_IP");
            }
            if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {
                s = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {
                s = request.getRemoteAddr();
            }
            if ("127.0.0.1".equals(s) || "0:0:0:0:0:0:0:1".equals(s)) {
                try {
                    s = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException unknownhostexception) {
                }
            }
            return s;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取客户端操作系统
     *
     * @return
     */
    public static String getClientOs() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("user-agent");
        if (StringUtil.isNotEmpty(userAgent)) {
            int start = userAgent.indexOf("(");
            int end = userAgent.indexOf(")");

            return start > 0 && end > 0 ? userAgent.substring(start + 1, end) : userAgent;

        } else {
            return null;
        }
    }

    /**
     * 获取浏览器信息
     *
     * @return
     */
    public static String getClientBrowser() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            String userAgent = request.getHeader("user-agent");
            String user = request.getHeader("user-agent").toLowerCase();
            if (user.contains("edge")) {
                return (userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("msie")) {
                String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
                return substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
            } else if (user.contains("safari") && user.contains("version")) {
                return (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
                        + "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
            } else if (user.contains("opr") || user.contains("opera")) {
                if (user.contains("opera")) {
                    return (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
                            + "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
                } else if (user.contains("opr")) {
                    return ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace("OPR", "Opera");
                }
            } else if (user.contains("chrome")) {
                return (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
            } else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1) ||
                    (user.indexOf("mozilla/4.7") != -1) || (user.indexOf("mozilla/4.78") != -1) ||
                    (user.indexOf("mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1)) {
                return "Netscape-?";
            } else if (user.contains("firefox")) {
                return (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("rv")) {
                String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
                return "IE" + IEVersion.substring(0, IEVersion.length() - 1);
            } else {
                return "UnKnown, More-Info: " + userAgent;
            }
        } catch (Exception e) {
        }
        return null;
    }


    public static boolean isCN() {
        Locale locale = getLocale();
        return Objects.equals(locale, Locale.SIMPLIFIED_CHINESE);
    }

    public static boolean isUS() {
        Locale locale = getLocale();
        return Objects.equals(locale, Locale.US);
    }

    public static Locale getLocale() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return getLocaleResolver().resolveLocale(request);
        }
        return Locale.SIMPLIFIED_CHINESE;
    }


    public static SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public static void setSessionRegistry(SessionRegistry sessionRegistry) {
        WebUtil.sessionRegistry = sessionRegistry;
    }


    private static LocaleResolver getLocaleResolver() {
        return RequestContextUtils.getLocaleResolver(getRequest());
    }


}