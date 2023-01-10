package com.dsec.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.repository.UserRepository;
import com.dsec.backend.security.UserPrincipal;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

	private final UserRepository userRepository;

	public UniqueEmailValidator(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {

		Object principal = SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		if (principal instanceof Jwt) {
			UserEntity entity = UserPrincipal.fromClaims(((Jwt) principal).getClaims()).getUserEntity();
			if (email.equals(entity.getEmail())) { // on user update
				return true;
			}
		}

		return email != null && !this.userRepository.existsByEmail(email);
	}

}
