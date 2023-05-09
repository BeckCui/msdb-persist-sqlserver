package com.dhl.fin.api.common.authentication;




import com.dhl.fin.api.common.dto.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Token service
 */
public interface TokenService {
    /**
     * 验证 token 是否有效。如果有效，则返回 userId
     */
    String verifyToken(String token) throws UnsupportedEncodingException;

    /**
     * 验证 request。如果有效，则返回 userId
     */
    String verifyRequest(HttpServletRequest request) throws UnsupportedEncodingException;

    /**
     * 生成新的 token
     */
    String generateToken(UserInfo user) throws Exception;


    /**
     * 验证 token 是否有效。如果有效，则返回 userId
     */
    String getUserIdFromToken(String token) throws UnsupportedEncodingException;
}
