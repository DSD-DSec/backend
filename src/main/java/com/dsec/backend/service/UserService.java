package com.dsec.backend.service;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
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

@Service
public class UserService implements IUserService {

	private UserRepository userRepository;
	private RoleRepository roleRepository;

	private PasswordEncoder passwordEncoder;
	private JwtEncoder jtwEncoder;
	private AuthenticationManager manager;

	@Value("${jwt.expiration}")
	private long jwtExpiry;

	@Autowired
	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
			JwtEncoder jtwEncoder, AuthenticationManager manager) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jtwEncoder = jtwEncoder;
		this.manager = manager;
	}

	@Override
	public URI register(UserDTO userDTO) {

		UserRole roleEntity = roleRepository.getByRoleNameEquals(Role.USER);
		if (roleEntity == null) {
			roleEntity = new UserRole(Role.USER);
			roleEntity = roleRepository.save(roleEntity);
		}

		UserModel userModel = new UserModel(userDTO.firstName(), userDTO.lastName(), userDTO.email(),
				passwordEncoder.encode(userDTO.password()), roleEntity);

		userModel = userRepository.save(userModel);

		return ServletUriComponentsBuilder.fromPath("api/users/{id}").buildAndExpand(userModel.getId()).toUri();

	}

	@Override
	public UserInfoDTO login(LoginInfoDTO dto) {

		Authentication auth = manager
				.authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

		UserPrincipal user = (UserPrincipal) auth.getPrincipal();

		Instant now = Instant.now();

		// @formatter:off
		String scope = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("self").issuedAt(now).expiresAt(now.plusSeconds(jwtExpiry))
				.subject(auth.getName()).claim("scope", scope).build();
		// @formatter:on

		String token = this.jtwEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		UserModel userModel = user.getUserModel();
		return new UserInfoDTO(userModel.getId(), user.getUsername(), userModel.getFirstName(),
				userModel.getFirstName(), token);

	}

}
