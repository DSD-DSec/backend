package com.dsec.backend.service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import exception.EntityMissingException;

@Service
public class UserServiceImpl implements UserService {
	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	private UserRepository userRepository;
	private RoleRepository roleRepository;

	private PasswordEncoder passwordEncoder;
	private JwtEncoder jtwEncoder;
	private AuthenticationManager manager;
	private ObjectMapper objectMapper;

	@Value("${jwt.expiration}")
	private long jwtExpiry;

	@Value("${jwt.cookie.name}")
	private String cookieName;

	@Autowired
	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, JwtEncoder jtwEncoder, AuthenticationManager manager,
			ObjectMapper objectMapper) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jtwEncoder = jtwEncoder;
		this.manager = manager;
		this.objectMapper = objectMapper;
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

		Instant now = Instant.now();

		String scope = auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));

		String object = null;
		try {
			object = objectMapper.writeValueAsString(user);
		} catch (JsonProcessingException e) {
			logger.error("UserPrincipal serialization error {}", e.getMessage());
		}

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("self")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(jwtExpiry))
				.subject(auth.getName())
				.claim("scope", scope)
				.claim("object", object)
				.build();

		String token = this.jtwEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		ResponseCookie cookie = ResponseCookie.from(cookieName, token).httpOnly(true).path("/api")
				.maxAge(jwtExpiry).build();

		response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		UserModel userModel = user.getUserModel();
		return new UserInfoDTO(userModel.getId(), user.getUsername(), userModel.getFirstName(),
				userModel.getLastName(), userModel.getUserRole());

	}

	@Override
	public ResponseEntity<?> getAllUsers() throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		int i = 0;
		String responseBody = new String();
		for (UserModel userModel : userRepository.findAll()) {
			String tmpString = "";
			if(i != 0)
				tmpString = ",";
			tmpString = tmpString + ("\""+i+"\":"+mapper.writeValueAsString(new UserInfoDTO(userModel.getId(), userModel.getEmail(), userModel.getFirstName(), userModel.getLastName(), userModel.getUserRole())));
			responseBody = responseBody + tmpString;
			i++;
		}

		if(responseBody.isEmpty())
			return ResponseEntity.noContent().build();

		return ResponseEntity.ok( responseBody);
	}


	@Override
	public UserInfoDTO getUser(Jwt user) {

		String username = user.getSubject();

		UserModel userModel = userRepository.findByEmailEquals(username).get(0);

		return new UserInfoDTO(userModel.getId(), userModel.getEmail(),
				userModel.getFirstName(), userModel.getLastName(), userModel.getUserRole());
	}

	@Override
	public ResponseEntity<?> editUser(int id, UserDTO userDTO)
	{
		try
		{
			if(id == userDTO.id())
			{
				UserModel userModel = userRepository.findById(id);
				Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				JSONObject userData = new JSONObject((String) user.getClaim("object"));
				System.out.println(userModel.getId());
				if(userData.getJSONObject("userModel").getInt("id") == userModel.getId())
				{
					// TODO: missing validation
					userModel.setFirstName(userDTO.firstName());
					userModel.setLastName(userDTO.lastName());
					userModel.setPassword(passwordEncoder.encode(userDTO.password()));
					userModel.setEmail(userDTO.email());
					userRepository.save(userModel);
					return ResponseEntity.status(204).build();
				}
			}

			return ResponseEntity.status(401).build();

		} catch(Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public UserModel deleteUserById(Integer idUser, Jwt principal) throws IllegalAccessException {
		List<UserModel> model = userRepository.findByEmailEquals(principal.getSubject());
		if (!model.get(0).getId().equals(idUser))
			throw new IllegalAccessException("Invalid user deletion.");

		UserModel user = fetch(idUser);
		userRepository.delete(user);
		return user;
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public UserModel fetch(Integer id) {
		return userRepository.findById(id).orElseThrow(() -> new EntityMissingException(UserModel.class, id));
	}

}
