package com.dsec.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.dsec.backend.model.UserRegisterDTO;

public class MatchPasswordValidator implements ConstraintValidator<MatchPassword, Object> {

	@Override
	public void initialize(MatchPassword constraintAnnotation) {}

	@Override
	public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
		UserRegisterDTO user = (UserRegisterDTO) obj;

		return user.getPassword().equals(user.getSecondPassword());
	}
}
