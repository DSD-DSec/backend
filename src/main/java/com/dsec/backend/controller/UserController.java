package com.dsec.backend.controller;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dsec.backend.DTO.UserInfoDTO;
import com.dsec.backend.service.UserService;

@RestController
@RequestMapping(value = "/users", produces = "application/hal+json")
public class UserController {

    private final UserService userService;

    private String cookieName;

    @Autowired
    public UserController(UserService userService, @Value("${jwt.cookie.name}") String cookieName) {
        this.userService = userService;
        this.cookieName = cookieName;
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        response.setHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from(cookieName, "").maxAge(0).build().toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public UserInfoDTO getMe() throws IllegalAccessException {
        Object user = SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (user instanceof String) {
            throw new IllegalAccessException();
        }

        return userService.getUser((Jwt) user);
    }

}
