package com.dsec.backend.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.hateoas.UserAssembler;
import com.dsec.backend.model.EmptyDTO;
import com.dsec.backend.model.user.UserDTO;
import com.dsec.backend.model.user.UserUpdateDTO;
import com.dsec.backend.service.UserService;
import com.dsec.backend.specification.UserSpecification;
import com.dsec.backend.util.cookie.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/users",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_FORMS_JSON_VALUE})
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final UserAssembler userAssembler;
    private final UserSpecification userSpecification;
    private final PagedResourcesAssembler<UserEntity> pagedResourcesAssembler;

    @PostMapping("/logout")
    public ResponseEntity<EmptyDTO> logout(HttpServletResponse response,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("Logout user {}", jwt.getSubject());

        cookieUtil.deleteJwtCookie(response);

        return ResponseEntity.ok(new EmptyDTO());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal Jwt jwt) {
        UserDTO userDTO = userAssembler.toModel(userService.fetch(jwt.<Long>getClaim("id")));

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable("id") long id) {
        UserDTO userDTO = userAssembler.toModel(userService.fetch(id));

        return ResponseEntity.ok(userDTO);
    }

    /**
     * Searches for users.
     * 
     * Enables lazy loading (similar to listing pages of a book - display first 10 pages at first
     * request, then display next 10 pages at next request etc.).
     *
     * @param pageable Represents a page of users.
     * @param firstName Represents a firstName that should be searched for.
     * @param lastName Represents a lastName that should be searched for.
     * @param email Represents a email that should be searched for.
     * @param generalSearch Space delimited strings that will be searched for in each of firstName,
     *        lastName and email categories.
     * @return A page of users.
     */
    @GetMapping("")
    public ResponseEntity<PagedModel<UserDTO>> getUsers(@PageableDefault Pageable pageable,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "generalSearch", required = false) String generalSearch,
            @AuthenticationPrincipal Jwt jwt) {

        Specification<UserEntity> specs =
                userSpecification.geSpecification(firstName, lastName, email, generalSearch);

        Page<UserEntity> pageUser = userService.findUsers(pageable, specs);

        PagedModel<UserDTO> pageModel =
                pagedResourcesAssembler.toModel(pageUser, userAssembler);

        return ResponseEntity.ok(pageModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable("id") long id,
            @AuthenticationPrincipal Jwt jwt) {
        UserEntity user = userService.deleteUser(id, jwt);

        UserDTO userDTO = userAssembler.toModel(user);
        return ResponseEntity.ok(userDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") long id,
            @Valid UserUpdateDTO userUpdateDTO, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity
                .ok(userAssembler.toModel(userService.updateUser(id, userUpdateDTO, jwt)));
    }

}
