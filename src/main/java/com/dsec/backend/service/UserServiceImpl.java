package com.dsec.backend.service;

import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.dsec.backend.DTO.LoginInfoDTO;
import com.dsec.backend.DTO.UserDTO;
import com.dsec.backend.DTO.UserInfoDTO;
import com.dsec.backend.model.Role;
import com.dsec.backend.model.UserModel;
import com.dsec.backend.model.UserRole;
import com.dsec.backend.repository.RoleRepository;
import com.dsec.backend.repository.UserRepository;
import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.util.cookie.CookieUtil;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;
	private RoleRepository roleRepository;

	private PasswordEncoder passwordEncoder;
	private AuthenticationManager manager;
	private CookieUtil cookieUtil;

	@Autowired
	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, AuthenticationManager manager,
			CookieUtil cookieUtil) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.manager = manager;
		this.cookieUtil = cookieUtil;
	}

	@Override
	public URI register(UserDTO userDTO) {

		UserRole roleEntity = roleRepository.getByRoleNameEquals(Role.USER);
		if (roleEntity == null) {
			roleEntity = new UserRole(Role.USER);
			roleEntity = roleRepository.save(roleEntity);
		}

		UserModel userModel = new UserModel(userDTO.firstName(), userDTO.lastName(),
				userDTO.email(), passwordEncoder.encode(userDTO.password()), roleEntity);

		userModel = userRepository.save(userModel);

		return ServletUriComponentsBuilder.fromPath("api/users/{id}")
				.buildAndExpand(userModel.getId()).toUri();
	}

	@Override
	public UserInfoDTO login(LoginInfoDTO dto, HttpServletResponse response) {

		Authentication auth = manager
				.authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

		UserPrincipal user = (UserPrincipal) auth.getPrincipal();

		cookieUtil.createJwtCookie(response, user);

		UserModel userModel = user.getUserModel();
		return new UserInfoDTO(userModel.getId(), user.getUsername(), userModel.getFirstName(),
				userModel.getLastName(), userModel.getUserRole());
	}

	@Override
	public UserInfoDTO getUser(Jwt user) {

		String username = user.getSubject();

		UserModel userModel = userRepository.findByEmailEquals(username).get(0);

		return new UserInfoDTO(userModel.getId(), userModel.getEmail(),
				userModel.getFirstName(), userModel.getLastName(), userModel.getUserRole());
	}

}
