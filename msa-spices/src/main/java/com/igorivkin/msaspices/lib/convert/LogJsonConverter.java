package com.igorivkin.msaspices.lib.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * JSON-converter. Uses dedicated object mapper.
 */
@Component
public class LogJsonConverter implements LogConverter {

    private final ObjectMapper objectMapper;

    @Autowired
    public LogJsonConverter(@Qualifier("masking-object-mapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts an object to JSON.
     *
     * @param obj object to logging
     * @return JSON-string representation of object to log
     */
    @Override
    public String convertObjectToString(Object obj) {
        if (obj != null) {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Cannot convert object to JSON, reason: " + ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public LogConversionType getConversionType() {
        return LogConversionType.JSON;
    }

}
