package com.dsec.backend.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.dsec.backend.util.cookie.CookieUtil;

public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final BearerTokenAuthenticationEntryPoint bearerAuthEntryPoint;
    private final CookieUtil cookieUtil;

    public MyAuthenticationEntryPoint(CookieUtil cookieUtil) {
        bearerAuthEntryPoint = new BearerTokenAuthenticationEntryPoint();
        this.cookieUtil = cookieUtil;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) {

        cookieUtil.deleteJwtCookie(request, response);

        bearerAuthEntryPoint.commence(request, response, authException);
    }

}
