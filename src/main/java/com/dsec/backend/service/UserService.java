package com.dsec.backend.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;

import com.dsec.backend.entity.Repo;
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

	void saveToken(Long id, String token);

	String getToken(Jwt jwt);

	String getToken(UserEntity user);

	List<Repo> getUsersRepos(long id, Jwt jwt);

	String getOAuthtoken(Jwt jwt);
}
