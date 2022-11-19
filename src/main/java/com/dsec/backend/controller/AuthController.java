package com.dsec.backend.controller;

import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.hateoas.UserAssembler;
import com.dsec.backend.model.LoginDTO;
import com.dsec.backend.model.UserDTO;
import com.dsec.backend.model.UserRegisterDTO;
import com.dsec.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/auth",
		produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE})
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {
	private final UserService userService;
	private final UserAssembler userAssembler;

	@PostMapping("/register")
	ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
		log.debug("Register request from email: {}", userRegisterDTO.getEmail());

		UserEntity entity = userService.register(userRegisterDTO);

		URI uri = ServletUriComponentsBuilder.fromPath("api/users/{id}")
				.buildAndExpand(entity.getId()).toUri();

		return ResponseEntity.created(uri).body(userAssembler.toModel(entity));
	}

	@PostMapping("/login")
	public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginDTO loginDTO,
			HttpServletResponse response) {
		log.debug("Login request from email: {}", loginDTO.getEmail());

		UserEntity entity = userService.login(loginDTO, response);

		return ResponseEntity.ok(userAssembler.toModel(entity));
	}

}
