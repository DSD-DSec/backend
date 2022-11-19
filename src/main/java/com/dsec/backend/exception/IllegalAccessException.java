package com.dsec.backend.exception;

/**
 * Exception thrown when there is something wrong with access rights for example
 * user is trying to delete request that is not created by him.
 */
public class IllegalAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalAccessException() { super(); }

	public IllegalAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalAccessException(String message, Throwable cause) { super(message, cause); }

	public IllegalAccessException(String message) { super(message); }

	public IllegalAccessException(Throwable cause) { super(cause); }

}
