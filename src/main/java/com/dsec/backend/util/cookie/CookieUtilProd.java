package com.dsec.backend.util.cookie;

import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.dsec.backend.config.ConfigProperties;
import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Profile("prod")
public class CookieUtilProd implements CookieUtil {

    private final JwtUtil jwtUtil;
    private final ConfigProperties configProperties;

    @Override
    public void createJwtCookie(HttpServletResponse response, UserPrincipal userPrincipal) {
        long exp = configProperties.getJwt().getExpiration();
        addCookie(response, configProperties.getJwt().getCookieName(), jwtUtil.getToken(userPrincipal, exp), exp);
    }

    @Override
    public void deleteJwtCookie(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, configProperties.getJwt().getCookieName());
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
        String backendDomain = configProperties.getBackend().getUrl().split("://")[1].split(":")[0];
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from(name, value)
                        .httpOnly(true).path("/api").domain(backendDomain)
                        .maxAge(maxAge).secure(true).sameSite("None").build().toString());
    }

    @Override
    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        String backendDomain = configProperties.getBackend().getUrl().split("://")[1].split(":")[0];
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    response.addHeader(HttpHeaders.SET_COOKIE,
                            ResponseCookie.from(name, "").httpOnly(true).path("/api")
                                    .domain(backendDomain)
                                    .maxAge(0).secure(true).sameSite("None").build().toString());
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
