package com.dsec.backend.service;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.dsec.backend.entity.Role;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRole;
import com.dsec.backend.model.LoginDTO;
import com.dsec.backend.model.UserDTO;
import com.dsec.backend.model.UserRegisterDTO;
import com.dsec.backend.repository.RoleRepository;
import com.dsec.backend.repository.UserRepository;
import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.util.cookie.CookieUtil;
import exception.EntityMissingException;
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

	@Override
	public UserEntity register(UserRegisterDTO userRegisterDTO) {

		UserRole roleEntity = roleRepository.getByRoleNameEquals(Role.USER);
		if (roleEntity == null) {
			roleEntity = new UserRole();
			roleEntity.setRoleName(Role.USER);
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
		// TODO Auto-generated method stub

		// List<UserModel> model = userRepository.findByEmailEquals(principal.getSubject());
		// if (!model.get(0).getId().equals(idUser))
		// throw new IllegalAccessException("Invalid user deletion.");

		// UserModel user = fetch(idUser);
		// userRepository.delete(user);
		// return user;

		return null;
	}

	@Override
	public Page<UserEntity> findUsers(Pageable pageable, Specification<UserEntity> specification) {
		log.debug("Requesting users page {}", pageable.getPageNumber());

		return userRepository.findAll(specification, pageable);
	}

	@Override
	public UserEntity updateUser(long id, UserDTO userDTO, Jwt jwt) {
		// TODO Auto-generated method stub

		// try {
		// if (id == userDTO.id()) {
		// UserEntity userEntity = fetch(id);
		// Jwt user =
		// (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// JSONObject userData = new JSONObject((String) user.getClaim("object"));
		// System.out.println(userModel.getId());
		// if (userData.getJSONObject("userModel").getInt("id") == userModel.getId()) {
		// // TODO: missing validation
		// userEntity.setFirstName(userDTO.firstName());
		// userEntity.setLastName(userDTO.lastName());
		// userEntity.setPassword(passwordEncoder.encode(userDTO.password()));
		// userEntity.setEmail(userDTO.email());
		// userRepository.save(userModel);
		// return ResponseEntity.status(204).build();
		// }
		// }

		// return ResponseEntity.status(401).build();

		// } catch (Exception e) {
		// return ResponseEntity.notFound().build();
		// }

		return null;
	}



}
