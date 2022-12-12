package com.dsec.backend.util;

import java.io.IOException;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.dsec.backend.model.GitleaksDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class GitleaksListConverter implements AttributeConverter<List<GitleaksDTO>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<GitleaksDTO> logs) {

        String customerInfoJson = null;
        try {
            customerInfoJson = objectMapper.writeValueAsString(logs);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return customerInfoJson;
    }

    @Override
    public List<GitleaksDTO> convertToEntityAttribute(String logsJSON) {

        List<GitleaksDTO> logs = null;
        try {
            logs = objectMapper.readValue(logsJSON,
                    new TypeReference<List<GitleaksDTO>>() {
                    });
        } catch (final IOException e) {
            log.error("JSON reading error", e);
        }

        return logs;
    }
}