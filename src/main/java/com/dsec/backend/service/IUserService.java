package com.dsec.backend.service;

import java.net.URI;

import com.dsec.backend.DTO.LoginInfoDTO;
import com.dsec.backend.DTO.UserDTO;
import com.dsec.backend.DTO.UserInfoDTO;

public interface IUserService {

	URI register(UserDTO userDTO);

	UserInfoDTO login(LoginInfoDTO loginInfoDTO);
}
