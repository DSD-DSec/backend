package com.dsec.backend.service;

import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import com.dsec.backend.DTO.LoginInfoDTO;
import com.dsec.backend.DTO.UserDTO;
import com.dsec.backend.DTO.UserInfoDTO;

public interface IUserService {

	URI register(UserDTO userDTO);

	UserInfoDTO login(LoginInfoDTO loginInfoDTO, HttpServletResponse response);

	UserInfoDTO getUser(Jwt user);
}
