package com.dsec.backend.controller;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.hateoas.UserAssembler;
import com.dsec.backend.model.user.LoginDTO;
import com.dsec.backend.model.user.UserRegisterDTO;
import com.dsec.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {
	private final UserService userService;
	private final UserAssembler userAssembler;

	@PostMapping("/register")
	public ResponseEntity<UserEntity> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
		log.debug("Register request from email: {}", userRegisterDTO.getEmail());

		UserEntity entity = userService.register(userRegisterDTO);

		URI uri = UriComponentsBuilder.fromPath("api/users/{id}")
				.buildAndExpand(entity.getId()).toUri();

		return ResponseEntity.created(uri).body(userAssembler.toModel(entity));
	}

	@PostMapping("/login")
	public ResponseEntity<UserEntity> login(@Valid @RequestBody LoginDTO loginDTO,
			HttpServletResponse response) {
		log.debug("Login request from email: {}", loginDTO.getEmail());

		UserEntity entity = userService.login(loginDTO, response);

		return ResponseEntity.ok(userAssembler.toModel(entity));
	}

}
