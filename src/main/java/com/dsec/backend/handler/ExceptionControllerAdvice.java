package com.dsec.backend.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.model.FieldErrorDTO;
import com.dsec.backend.model.GlobalErrorDTO;

@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(IllegalAccessException.class)
	public ResponseEntity<?> handleIllegalAccessException(IllegalAccessException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(EntityMissingException.class)
	public ResponseEntity<?> handleEntityMissingException(EntityMissingException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {

		BindingResult result = ex.getBindingResult();
		List<FieldErrorDTO> fieldErrors = result.getFieldErrors().stream()
				.map(f -> new FieldErrorDTO(f.getObjectName(), f.getField(), f.getCode(),
						f.getDefaultMessage()))
				.collect(Collectors.toList());

		List<GlobalErrorDTO> globalErrors = result.getGlobalErrors().stream().map(
				f -> new GlobalErrorDTO(f.getObjectName(), f.getCode(), f.getDefaultMessage()))
				.collect(Collectors.toList());

		return handleExceptionInternal(ex,
				createProps(ex, HttpStatus.BAD_REQUEST, fieldErrors, globalErrors),
				new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}


	private Map<String, Object> createProps(Exception e, HttpStatus status) {
		Map<String, Object> props = new HashMap<>();
		props.put("message", e.getMessage());
		props.put("status", status.getReasonPhrase());
		props.put("code", String.valueOf(status.value()));

		return props;
	}

	private Map<String, Object> createProps(Exception e, HttpStatus status,
			List<FieldErrorDTO> fieldErrorDTOs, List<GlobalErrorDTO> globalErrors) {
		Map<String, Object> props = createProps(e, status);
		props.put("fieldErors", fieldErrorDTOs);
		props.put("globalErrors", globalErrors);

		return props;
	}
}
