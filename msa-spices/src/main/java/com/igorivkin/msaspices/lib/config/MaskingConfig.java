package com.igorivkin.msaspices.lib.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class MaskingConfig {

    @Bean(name = "xml-object-mapper")
    public ObjectMapper xmlObjectMapper() {
        XmlMapper xmlMapper = new XmlMapper();

        // Set serialization options
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        return xmlMapper;
    }

    @Bean(name = "masking-object-mapper")
    public ObjectMapper maskingObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        objectMapper.registerModule(new JavaTimeModule());

        // Set serialization options
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        // Disable non-required features
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);

        return objectMapper;
    }
}
