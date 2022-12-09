package com.dsec.backend.exception;

import java.io.Serial;

/**
 * Exception thrown when there is something wrong with access rights for example user is trying to
 * delete request that is not created by him.
 */
public class UnauthorizedAccessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;


}
