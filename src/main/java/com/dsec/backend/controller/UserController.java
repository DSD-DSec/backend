package com.dsec.backend.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.hateoas.UserAssembler;
import com.dsec.backend.model.EmptyDTO;
import com.dsec.backend.model.user.UserUpdateDTO;
import com.dsec.backend.service.user.UserService;
import com.dsec.backend.repository.specification.UserSpecification;
import com.dsec.backend.util.cookie.CookieUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/users")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final UserAssembler userAssembler;
    private final UserSpecification userSpecification;
    private final PagedResourcesAssembler<UserEntity> pagedResourcesAssembler;

    @PostMapping("/logout")
    public ResponseEntity<EmptyDTO> logout(HttpServletRequest request, HttpServletResponse response,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("Logout user {}", jwt.getSubject());

        cookieUtil.deleteJwtCookie(request, response);

        return ResponseEntity.ok(new EmptyDTO());
    }

    @GetMapping("/me")
    public ResponseEntity<UserEntity> getMe(@AuthenticationPrincipal Jwt jwt) {
        UserEntity entity = userAssembler.toModel(userService.fetch(Long.parseLong(jwt.getClaim("id"))));

        return ResponseEntity.ok(entity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getById(@PathVariable("id") long id) {
        UserEntity entity = userAssembler.toModel(userService.fetch(id));

        return ResponseEntity.ok(entity);
    }

    /**
     * Searches for users.
     * <p>
     * Enables lazy loading (similar to listing pages of a book - display first 10
     * pages at first
     * request, then display next 10 pages at next request etc.).
     *
     * @param pageable      Represents a page of users.
     * @param firstName     Represents a firstName that should be searched for.
     * @param lastName      Represents a lastName that should be searched for.
     * @param email         Represents a email that should be searched for.
     * @param generalSearch Space delimited strings that will be searched for in
     *                      each of firstName,
     *                      lastName and email categories.
     * @return A page of users.
     */
    @GetMapping("")
    public ResponseEntity<PagedModel<UserEntity>> getUsers(@PageableDefault Pageable pageable,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "generalSearch", required = false) String generalSearch,
            @AuthenticationPrincipal Jwt jwt) {

        Specification<UserEntity> specs = userSpecification.geSpecification(firstName, lastName, email, generalSearch);

        Page<UserEntity> pageUser = userService.findUsers(pageable, specs);

        PagedModel<UserEntity> pageModel = pagedResourcesAssembler.toModel(pageUser, userAssembler);

        return ResponseEntity.ok(pageModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserEntity> deleteUser(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) {
        UserEntity user = userService.deleteUser(id, jwt);

        user = userAssembler.toModel(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UserUpdateDTO userUpdateDTO) {

        return ResponseEntity
                .ok(userAssembler.toModel(userService.updateUser(id, userUpdateDTO, jwt)));
    }

    @GetMapping("/{id}/repos")
    public ResponseEntity<List<Repo>> getRepos(@PathVariable("id") long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUsersRepos(id, jwt));
    }

    @GetMapping("/token")
    public ResponseEntity<String> getOAuthtoken(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getOAuthtoken(jwt));
    }

}
