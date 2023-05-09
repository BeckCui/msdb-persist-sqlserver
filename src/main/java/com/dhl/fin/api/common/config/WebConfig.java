package com.dhl.fin.api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Application configuration defined in file: application.properties
 */
@Component
public class WebConfig {


    @Value("${spring.profiles.active}")
    public String profilesActive;

    @Value("${local.ldap.host}")
    public String ldapHost;

    @Value("${local.ldap.post}")
    public String ldapPost;

    @Value("${local.rpmp.appId}")
    public String appId;

    @Value("${local.rpmp.sessionExpiredDay}")
    public int sessionExpiredDay = 7;

    @Value("${local.rpmp.allowAnonymousApiList}")
    public String[] allowAnonymousApiList = new String[0];

    @Value("${local.rpmp.internalAuth.enable}")
    public boolean enableInternalAuth;

    @Value("${local.rpmp.internalAuth.apiList}")
    public String[] internalAuthApiList = new String[0];

    @Value("${local.rpmp.appCredential.apiList}")
    public String[] appCredentialApiList = new String[0];

    /**
     * App credential 签名过期时间 (单位：分钟)
     */
    @Value("${local.rpmp.appCredential.signatureExpiredMs}")
    public int signatureExpiredMs = 60000;

    @Value("${local.rpmp.token.key}")
    public String tokenKey;

    @Value("${local.rpmp.token.headerName}")
    public String tokenHeaderName;

    @Value("${local.rpmp.token.algorithmId}")
    public String tokenAlgorithmId;

    @Value("${local.rpmp.token.expiredAfterHours}")
    public int userTokenExpiredAfterHours = 24 * 7;

    @Value("${local.rpmp.xappAuth.apiList}")
    public String[] xappAuthApiList = new String[0];




}
