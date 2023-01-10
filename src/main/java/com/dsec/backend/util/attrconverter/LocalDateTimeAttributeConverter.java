package com.dsec.backend.util.attrconverter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime locDateTime) {
        return locDateTime == null ? null : Timestamp.valueOf(locDateTime);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp sqlTimestamp) {
        return sqlTimestamp == null ? null
                : sqlTimestamp.toInstant().atZone(ZoneId.of("Europe/Zagreb")).toLocalDateTime();
    }

    public static LocalDateTime now() {
        return Instant.now().atZone(ZoneId.of("Europe/Zagreb")).toLocalDateTime();
    }
}