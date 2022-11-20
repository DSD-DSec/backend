package com.dsec.backend.service;

import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.model.user.LoginDTO;
import com.dsec.backend.model.user.UserRegisterDTO;
import com.dsec.backend.model.user.UserUpdateDTO;

public interface UserService {

	UserEntity register(UserRegisterDTO userRegisterDTO);

	UserEntity login(LoginDTO loginDTO, HttpServletResponse response);

	Page<UserEntity> findUsers(Pageable pageable, Specification<UserEntity> specification);

	UserEntity updateUser(long id, UserUpdateDTO userUpdateDTO, Jwt jwt);

	UserEntity deleteUser(long id, Jwt jwt);

	UserEntity fetch(long id);

}
