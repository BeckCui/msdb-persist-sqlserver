package com.dhl.fin.api.common.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dhl.fin.api.common.config.WebConfig;
import com.dhl.fin.api.common.dto.ApiResponseStatus;
import com.dhl.fin.api.common.dto.UserInfo;
import com.dhl.fin.api.common.exception.UserFriendlyException;
import com.dhl.fin.api.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Token service
 */
@Service
public class TokenServiceImpl implements TokenService {
    private WebConfig webConfig;


    @Autowired
    public TokenServiceImpl(WebConfig webConfig) {
        this.webConfig = webConfig;
    }

    @Override
    public String verifyToken(String token) throws UnsupportedEncodingException {
        DecodedJWT decodedJWT = verifyTokenInternal(token);
        return decodedJWT.getHeaderClaim(TokenClaimNames.UUID).asString();
    }


    @Override
    public String verifyRequest(HttpServletRequest request) throws UnsupportedEncodingException {
        String token = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().endsWith("token")).map(Cookie::getValue).findFirst().orElse(null);
        if (StringUtil.isEmpty(token)) {
            return null;
        }

        DecodedJWT decodedJWT = verifyTokenInternal(token);
        setRequestAtb(decodedJWT, request);

        return this.verifyToken(token);
    }

    private void setRequestAtb(DecodedJWT decodedJWT, HttpServletRequest request) {
        UserInfo userInfo = UserInfo.builder()
                .userName(decodedJWT.getHeaderClaim(TokenClaimNames.NAME).asString())
                .uuid(decodedJWT.getHeaderClaim(TokenClaimNames.UUID).asString())
                .adminType(decodedJWT.getHeaderClaim(TokenClaimNames.ADMIN_TYPE).asString())
                .email(decodedJWT.getHeaderClaim(TokenClaimNames.MAIL).asString())
                .build();
        request.setAttribute("loginUser", userInfo);
    }


    @Override
    public String generateToken(UserInfo user) throws Exception {
        Algorithm algorithm = getTokenAlgorithm();
        Date expiredDate = getDefaultExpiredDate();
        Map<String, Object> headerClaims = buildTokenClaims(user);

        String token = JWT.create()
                .withHeader(headerClaims)
                .withExpiresAt(expiredDate)
                .sign(algorithm);

        return token;
    }

    @Override
    public String getUserIdFromToken(String token) throws UnsupportedEncodingException {
        DecodedJWT decodedJWT = verifyTokenInternal(token);
        return decodedJWT.getHeaderClaim(TokenClaimNames.UUID).asString();
    }

    private Date getDefaultExpiredDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, webConfig.userTokenExpiredAfterHours);

        return cal.getTime();
    }

    private DecodedJWT verifyTokenInternal(String token) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(token) || token.trim().length() == 0) {
            throw new UserFriendlyException("dhl.token.tokenRequired", true, ApiResponseStatus.INVALID_TOKEN);
        }

        DecodedJWT jwt = JWT.decode(token);
        String algorithmName = jwt.getAlgorithm();

        if (StringUtils.isEmpty(algorithmName)) {
            throw new UserFriendlyException("dhl.token.tokenRequired", true, ApiResponseStatus.INVALID_TOKEN);
        }

        algorithmName = algorithmName.toUpperCase();
        Algorithm algorithm;

        switch (algorithmName) {
            case "HS256":
                algorithm = Algorithm.HMAC256(webConfig.tokenKey);
                break;
            case "HS384":
                algorithm = Algorithm.HMAC384(webConfig.tokenKey);
                break;
            case "HS512":
                algorithm = Algorithm.HMAC512(webConfig.tokenKey);
                break;
            default:
                algorithm = Algorithm.HMAC256(webConfig.tokenKey);
                break;
        }

        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        DecodedJWT result = verifier.verify(token);

        // 验证 token 是否过期
        Date expiredAt = result.getExpiresAt();
        if (expiredAt == null) {
            throw new UserFriendlyException("dhl.token.tokenInvalid", true,
                    ApiResponseStatus.INVALID_TOKEN);
        }

        if (expiredAt.before(new Date())) {
            throw new UserFriendlyException("dhl.token.tokenExpired", true,
                    ApiResponseStatus.TOKEN_EXPIRED);
        }

        String userId = result.getHeaderClaim(TokenClaimNames.UUID).asString();
        if (StringUtils.isBlank(userId)) {
            throw new UserFriendlyException("dhl.token.tokenInvalid", true,
                    ApiResponseStatus.INVALID_TOKEN);
        }

        return result;
    }


    private Map<String, Object> buildTokenClaims(UserInfo user) {
        if (StringUtils.isBlank(user.getUuid())) {
            throw new IllegalArgumentException("uuid");
        }
        Map<String, Object> result = new HashMap<>();
        result.put(TokenClaimNames.UUID, user.getUuid());
        result.put(TokenClaimNames.NAME, user.getUserName());
        result.put(TokenClaimNames.MAIL, user.getEmail());
        result.put(TokenClaimNames.ADMIN_TYPE, user.getAdminType());
        return result;
    }

    private Algorithm getTokenAlgorithm() throws UnsupportedEncodingException {
        return Algorithm.HMAC256(webConfig.tokenKey);
    }


}
