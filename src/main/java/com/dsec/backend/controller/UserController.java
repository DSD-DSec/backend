package com.dsec.backend.controller;

import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dsec.backend.DTO.EmptyDTO;
import com.dsec.backend.DTO.UserHALDTO;
import com.dsec.backend.DTO.UserInfoDTO;
import com.dsec.backend.model.UserModel;
import com.dsec.backend.service.UserService;
import com.dsec.backend.util.cookie.CookieUtil;

@RestController
@RequestMapping(value = "/users", produces = "application/hal+json")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserService userService;

    private CookieUtil cookieUtil;

    @Autowired
    public UserController(UserService userService, @Value("${jwt.cookie.name}") String cookieName) {
        this.userService = userService;
    }

    @PostMapping("/logout")
    ResponseEntity<EmptyDTO> logout(HttpServletResponse response) {
        cookieUtil.deleteJwtCookie(response);

        return ResponseEntity.ok(new EmptyDTO());
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getMe(@AuthenticationPrincipal Object user)
            throws IllegalAccessException {

        if (user instanceof String) {
            throw new IllegalAccessException();
        }

        return ResponseEntity.ok(userService.getUser((Jwt) user));
    }


    @DeleteMapping(value = "/{id}", produces = { "application/json; charset=UTF-8" })
    public ResponseEntity<?> deleteUser(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal Jwt principal) throws IllegalAccessException {
        try {
            UserModel model = userService.deleteUserById(id, principal);
            UserHALDTO user = new UserHALDTO(model.getId(), model.getFirstName(), model.getLastName(), model.getEmail(), model.getPassword());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            throw e;
        }
    }

}
