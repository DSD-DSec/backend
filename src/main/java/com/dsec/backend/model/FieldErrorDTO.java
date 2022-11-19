package com.dsec.backend.model;

public record FieldErrorDTO(String field, String fieldValue, String code, String message) {
}
