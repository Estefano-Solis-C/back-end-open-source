package com.codexateam.platform.iam.domain.model.valueobjects;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to persist EmailAddress Value Object as String.
 */
@Converter(autoApply = true)
public class EmailAddressAttributeConverter implements AttributeConverter<EmailAddress, String> {
    @Override
    public String convertToDatabaseColumn(EmailAddress attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public EmailAddress convertToEntityAttribute(String dbData) {
        return dbData != null ? new EmailAddress(dbData) : null;
    }
}

