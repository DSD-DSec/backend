package com.dsec.backend.service.user;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.Role;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRepo;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.model.user.LoginDTO;
import com.dsec.backend.model.user.UserRegisterDTO;
import com.dsec.backend.model.user.UserUpdateDTO;
import com.dsec.backend.repository.RoleRepository;
import com.dsec.backend.repository.UserRepository;
import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.util.EncryptionUtil;
import com.dsec.backend.util.JwtUtil;
import com.dsec.backend.util.cookie.CookieUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager manager;
	private final CookieUtil cookieUtil;
	private final EncryptionUtil encryptionUtil;
	private final JwtUtil jwtUtil;

	@Value("${encryption.key}")
	private String encryptionKey;

	@Override
	public UserEntity register(UserRegisterDTO userRegisterDTO) {

		UserRole roleEntity = roleRepository.getByRoleNameEquals(Role.USER);
		if (roleEntity == null) {
			roleEntity = UserRole.builder().roleName(Role.USER).build();
			roleEntity = roleRepository.save(roleEntity);
		}

		UserEntity userModel = new UserEntity(userRegisterDTO, roleEntity, passwordEncoder);

		return userRepository.save(userModel);
	}

	@Override
	public UserEntity login(LoginDTO loginDTO, HttpServletResponse response) {

		Authentication auth = manager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),
						loginDTO.getPassword()));

		UserPrincipal user = (UserPrincipal) auth.getPrincipal();

		cookieUtil.createJwtCookie(response, user);

		return user.getUserEntity();
	}

	@Override
	public UserEntity fetch(long id) {

		return userRepository.findById(id)
				.orElseThrow(() -> new EntityMissingException(UserEntity.class, id));
	}

	@Override
	public UserEntity deleteUser(long id, Jwt jwt) {
		UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

		if (!userJwt.getId().equals(id))
			throw new ForbidenAccessException("Invalid user deletion.");

		userRepository.deleteById(id);

		return userJwt;
	}

	@Override
	public Page<UserEntity> findUsers(Pageable pageable, Specification<UserEntity> specification) {
		log.debug("Requesting users page {}", pageable.getPageNumber());

		return userRepository.findAll(specification, pageable);
	}

	@Override
	public UserEntity updateUser(long id, UserUpdateDTO userUpdateDTO, Jwt jwt) {
		UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

		if (!userJwt.getId().equals(id))
			throw new ForbidenAccessException("Invalid user deletion.");

		UserEntity userEntity = fetch(id);

		userEntity.setEmail(userUpdateDTO.getEmail());
		userEntity.setFirstName(userUpdateDTO.getFirstName());
		userEntity.setLastName(userUpdateDTO.getLastName());

		return userRepository.save(userEntity);
	}

	@Override
	public void saveToken(Long id, String token) {

		UserEntity user = fetch(id);

		try {
			user.setToken(encryptionUtil.encrypt(token.getBytes(), encryptionKey));
			userRepository.save(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getToken(Jwt jwt) {
		UserEntity user = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

		return getToken(user);
	}

	@Override
	public String getToken(UserEntity user) {
		user = fetch(user.getId());

		try {
			return encryptionUtil.decrypt(user.getToken(), encryptionKey);
		} catch (Exception e) {
			throw new ForbidenAccessException(e);
		}
	}

	@Override
	public List<Repo> getUsersRepos(long id, Jwt jwt) {
		return fetch(id).getUserRepos().stream().map(UserRepo::getRepo).collect(Collectors.toList());
	}

	@Override
	public String getOAuthtoken(Jwt jwt) {
		return jwtUtil.getOAuthToken(UserPrincipal.fromClaims(jwt.getClaims()), 180L);
	}

}
