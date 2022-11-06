package com.dsec.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<?> handleDuplicateKeyException(DuplicateKeyException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.INTERNAL_SERVER_ERROR),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleDataIntegrityViolationException(
			DataIntegrityViolationException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<?> handlePropertyReferenceException(PropertyReferenceException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalAccessException.class)
	public ResponseEntity<?> handleIllegalAccessException(IllegalAccessException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	private Map<String, Object> createProps(Exception e, HttpStatus status) {
		Map<String, Object> props = new HashMap<>();
		props.put("message", e.getMessage());
		props.put("status", status.getReasonPhrase());
		props.put("code", String.valueOf(status.value()));

		return props;
	}
}
