package com.dsec.backend.exception;

import java.io.Serial;

public class EntityAlreadyExistsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EntityAlreadyExistsException() {
        super("Entity already exists");
    }

}