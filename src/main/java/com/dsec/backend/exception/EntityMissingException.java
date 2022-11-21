package com.dsec.backend.exception;

import java.io.Serial;

public class EntityMissingException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public EntityMissingException(Class<?> cls, Object ref) {
		super("Entity with reference " + ref + " of " + cls + " not found.");
	}

}
