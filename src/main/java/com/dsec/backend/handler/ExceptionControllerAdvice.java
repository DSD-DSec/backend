package com.dsec.backend.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.model.error.ErrorDTO;
import com.dsec.backend.model.error.FieldErrorDTO;
import com.dsec.backend.model.error.GlobalErrorDTO;
import com.dsec.backend.model.error.ValidationErrorDTO;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(UsernameNotFoundException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ErrorDTO> handleUsernameNotFoundException(UsernameNotFoundException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ForbidenAccessException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorDTO> handleIllegalAccessException(ForbidenAccessException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.FORBIDDEN),
				HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ErrorDTO> handleAuthenticationException(AuthenticationException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.UNAUTHORIZED),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(EntityMissingException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ErrorDTO> handleEntityMissingException(EntityMissingException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorDTO> handleDataIntegrityException(DataIntegrityViolationException e) {
		e.printStackTrace();

		return new ResponseEntity<>(createProps(e, HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
	}

	
	@Override
	@ApiResponse(responseCode = "400", description  = "Bad request",
	content = { @Content(mediaType = "application/json",
	schema = @Schema(implementation = ValidationErrorDTO.class)) })
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

	private ErrorDTO createProps(Exception e, HttpStatus status) {
		ErrorDTO errorDTO = new ErrorDTO();
		errorDTO.setMessage(e.getMessage());
		errorDTO.setStatus(status.getReasonPhrase());
		errorDTO.setCode(String.valueOf(status.value()));

		return errorDTO;
	}

	private ValidationErrorDTO createProps(Exception e, HttpStatus status,
			List<FieldErrorDTO> fieldErrorDTOs, List<GlobalErrorDTO> globalErrors) {
		ErrorDTO errorDTO = createProps(e, status);

		ValidationErrorDTO props = new ValidationErrorDTO();
		props.setMessage(errorDTO.getMessage());
		props.setCode(errorDTO.getCode());
		props.setStatus(errorDTO.getStatus());
		props.setFieldErrors(fieldErrorDTOs);
		props.setGlobalErrors(globalErrors);

		return props;
	}
}
