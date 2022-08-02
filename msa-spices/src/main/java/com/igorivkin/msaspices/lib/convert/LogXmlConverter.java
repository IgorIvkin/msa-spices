package com.igorivkin.msaspices.lib.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LogXmlConverter implements LogConverter {

    private final ObjectMapper objectMapper;

    @Autowired
    public LogXmlConverter(@Qualifier("xml-object-mapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LogConversionType getConversionType() {
        return LogConversionType.XML;
    }

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
}
