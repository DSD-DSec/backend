package com.dsec.backend.util.cookie;

import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.util.JwtUtil;

@Component
@Profile({"dev", "test"})
public class CookieUtilDev implements CookieUtil {

    private final JwtUtil jwtUtil;
    private final long jwtExpiry;
    private final String cookieName;

    public CookieUtilDev(JwtUtil jwtUtil, @Value("${jwt.expiration}") long jwtExpiry,
            @Value("${jwt.cookie.name}") String cookieName) {
        this.jwtUtil = jwtUtil;
        this.jwtExpiry = jwtExpiry;
        this.cookieName = cookieName;
    }

    @Override
    public void createJwtCookie(HttpServletResponse response, UserPrincipal userPrincipal) {
        addCookie(response, cookieName, jwtUtil.getToken(userPrincipal, jwtExpiry), jwtExpiry);
    }

    @Override
    public void deleteJwtCookie(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, cookieName);
    }

    @Override
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from(name, value)
                        .httpOnly(true).path("/api")
                        .maxAge(maxAge).build().toString());
    }

    @Override
    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    response.addHeader(HttpHeaders.SET_COOKIE,
                            ResponseCookie.from(name, "").httpOnly(true).path("/api")
                                    .maxAge(0).build().toString());
                }
            }
        }
    }

    @Override
    public String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    @Override
    public <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue())));
    }

}
