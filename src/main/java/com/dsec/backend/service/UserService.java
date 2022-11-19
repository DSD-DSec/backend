package com.dsec.backend.service;

import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.model.LoginDTO;
import com.dsec.backend.model.UserDTO;
import com.dsec.backend.model.UserRegisterDTO;

public interface UserService {

	UserEntity register(UserRegisterDTO userRegisterDTO);

	UserEntity login(LoginDTO loginDTO, HttpServletResponse response);

	Page<UserEntity> findUsers(Pageable pageable, Specification<UserEntity> specification);

	UserEntity updateUser(long id, UserDTO userDTO, Jwt jwt);

	UserEntity deleteUser(long id, Jwt jwt) throws IllegalAccessException;

	UserEntity fetch(long id);

}
