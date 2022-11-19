package com.dsec.backend.exception;

/**
 * The Class IllegalActionException models exception that is thrown when user is
 * authorized but is trying to execute some illegal action such as delete
 * request that must not be deleted
 */
public class IllegalActionException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new illegal action exception.
	 *
	 * @param message the message
	 */
	public IllegalActionException(String message) { super(message); }

	/**
	 * Instantiates a new illegal action exception.
	 *
	 * @param cause the cause
	 */
	public IllegalActionException(Throwable cause) { super(cause); }

	/**
	 * Instantiates a new illegal action exception.
	 *
	 * @param message the message
	 * @param cause   the cause
	 */
	public IllegalActionException(String message, Throwable cause) { super(message, cause); }

	/**
	 * Instantiates a new illegal action exception.
	 *
	 * @param message            the message
	 * @param cause              the cause
	 * @param enableSuppression  the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public IllegalActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
