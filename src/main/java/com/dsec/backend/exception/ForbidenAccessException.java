package com.dsec.backend.exception;

import java.io.Serial;

/**
 * Exception thrown when there is something wrong with access rights for example user is trying to
 * delete request that is not created by him.
 */
public class ForbidenAccessException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ForbidenAccessException() {
		super();
	}

	public ForbidenAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ForbidenAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ForbidenAccessException(String message) {
		super(message);
	}

	public ForbidenAccessException(Throwable cause) {
		super(cause);
	}

}
