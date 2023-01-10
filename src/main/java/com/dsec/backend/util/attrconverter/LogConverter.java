package com.dsec.backend.util.attrconverter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LogConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> String convertToDatabaseColumn(T logs) {

        String customerInfoJson = null;
        try {
            customerInfoJson = objectMapper.writeValueAsString(logs);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return customerInfoJson;
    }

    public <T> T convertToEntityAttribute(String logsJSON, Class<T> valueType) {

        T logs = null;
        try {
            logs = objectMapper.readValue(logsJSON,
                    valueType);
        } catch (final IOException | IllegalArgumentException e) {
            log.error("JSON reading error", e);
        }

        return logs;
    }

    public <T> T convertToEntityAttribute(String logsJSON, TypeReference<T> valueType) {

        T logs = null;
        try {
            logs = objectMapper.readValue(logsJSON,
                    valueType);
        } catch (final IOException | IllegalArgumentException e) {
            log.error("JSON reading error", e);
        }

        return logs;
    }
}