package com.igorivkin.msaspices.lib.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorivkin.msaspices.lib.config.MaskingConfig;
import com.igorivkin.msaspices.lib.convert.LogJsonConverter;
import lombok.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
@DisplayName("Unit-tests of log preparation service")
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {MaskingConfig.class, LogJsonConverter.class})
public class LogPreparationServiceTests {

    private static final String STRING_VALUE_TEST = "Test";

    @SpyBean
    private LogPreparationService logPreparationService;

    @SpyBean(name = "masking-object-mapper")
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Serialization for logs - success")
    public void prepareObjectToLog_success() {
        String stringValue = logPreparationService.convertObjectToString(STRING_VALUE_TEST);
        String longValue = logPreparationService.convertObjectToString(1L);
        String objectValue = logPreparationService.convertObjectToString(ObjectToPrepare.builder()
                .age(25)
                .name(STRING_VALUE_TEST)
                .build());

        assertEquals("\"" + STRING_VALUE_TEST + "\"", stringValue);
        assertEquals("1", longValue);
        assertEquals("{\"name\":\"Test\",\"age\":25}", objectValue);
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectToPrepare {
        private String name;
        private Integer age;
    }
}
