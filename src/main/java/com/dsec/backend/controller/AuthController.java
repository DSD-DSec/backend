package com.dsec.backend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsec.backend.DTO.LoginInfoDTO;
import com.dsec.backend.DTO.UserDTO;
import com.dsec.backend.DTO.UserInfoDTO;
import com.dsec.backend.service.IUserService;
import com.jayway.jsonpath.internal.Utils;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final IUserService userService;

	@Autowired
	public AuthController(IUserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	ResponseEntity<?> register(@RequestBody UserDTO userDTO) {

		// TODO auto validation?
		List<String> validStrings = List.of(userDTO.firstName(), userDTO.lastName(), userDTO.email(),
				userDTO.password());
		boolean isAnyEmpty = validStrings.stream().map(s -> Utils.isEmpty(s)).reduce((a, b) -> a || b).get();

		if (isAnyEmpty) {
			logger.debug(userDTO.toString());

			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.created(userService.register(userDTO)).build();
	}

	@PostMapping("/login")
	public ResponseEntity<UserInfoDTO> token(@RequestBody LoginInfoDTO dto) {

		return ResponseEntity.ok(userService.login(dto));
	}

}
