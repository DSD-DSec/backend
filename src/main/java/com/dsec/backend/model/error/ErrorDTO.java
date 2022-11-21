package com.dsec.backend.model.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDTO {
    private String message;
    private String status;
    private String code;

    public ErrorDTO() {
    }

    public ErrorDTO(String message, String status, String code) {
        this.message = message;
        this.status = status;
        this.code = code;
    }
}
