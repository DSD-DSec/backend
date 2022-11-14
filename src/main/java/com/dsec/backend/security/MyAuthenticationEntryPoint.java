package com.dsec.backend.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private BearerTokenAuthenticationEntryPoint bearerAuthEntryPoint;
    private String cookieName;

    public MyAuthenticationEntryPoint(String cookieName) {
        bearerAuthEntryPoint = new BearerTokenAuthenticationEntryPoint();
        this.cookieName = cookieName;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from(cookieName, "").httpOnly(true).path("/api")
                        .maxAge(0).build().toString());

        bearerAuthEntryPoint.commence(request, response, authException);
    }

}
