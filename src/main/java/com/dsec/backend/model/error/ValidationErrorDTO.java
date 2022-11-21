package com.dsec.backend.model.error;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationErrorDTO extends ErrorDTO {
    private List<FieldErrorDTO> fieldErrors;
    private List<GlobalErrorDTO> globalErrors;

    public ValidationErrorDTO() {
    }

    public ValidationErrorDTO(String message, String status, String code, List<FieldErrorDTO> fieldErrors,
            List<GlobalErrorDTO> globalErrors) {
        super(message, status, code);
        this.fieldErrors = fieldErrors;
        this.globalErrors = globalErrors;
    }
}
