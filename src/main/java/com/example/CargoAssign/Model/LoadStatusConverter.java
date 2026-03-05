package com.example.CargoAssign.Model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LoadStatusConverter implements AttributeConverter<LoadStatus, String> {

    @Override
    public String convertToDatabaseColumn(LoadStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public LoadStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        String normalized = dbData.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return LoadStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // Legacy/invalid status value in DB. Keep entity readable and validate in service.
            return null;
        }
    }
}
