package com.dsec.backend.service;

import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import com.dsec.backend.DTO.LoginInfoDTO;
import com.dsec.backend.DTO.UserDTO;
import com.dsec.backend.DTO.UserInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserService {

	URI register(UserDTO userDTO);

	UserInfoDTO login(LoginInfoDTO loginInfoDTO, HttpServletResponse response);

	UserInfoDTO getUser(Jwt user);

	ResponseEntity<?> editUser(int id, UserDTO userDTO);

	ResponseEntity<?> getAllUsers() throws JsonProcessingException;
}
