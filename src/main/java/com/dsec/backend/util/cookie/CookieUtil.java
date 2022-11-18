package com.dsec.backend.util.cookie;

import javax.servlet.http.HttpServletResponse;
import com.dsec.backend.security.UserPrincipal;

public interface CookieUtil {

    void createJwtCookie(HttpServletResponse response, UserPrincipal userPrincipal);

    void deleteJwtCookie(HttpServletResponse response);

}
