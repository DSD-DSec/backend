package com.dsec.backend.util.cookie;

import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dsec.backend.security.UserPrincipal;

public interface CookieUtil {

    void createJwtCookie(HttpServletResponse response, UserPrincipal userPrincipal);

    void deleteJwtCookie(HttpServletRequest request, HttpServletResponse respons);

    Optional<Cookie> getCookie(HttpServletRequest request, String name);

    void addCookie(HttpServletResponse response, String name, String value, long maxAge);

    void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name);

    String serialize(Object object);

    <T> T deserialize(Cookie cookie, Class<T> cls);
}
