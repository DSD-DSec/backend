package com.dsec.backend.model.error;

public record FieldErrorDTO(String field, String fieldValue, String code, String message) {
}
