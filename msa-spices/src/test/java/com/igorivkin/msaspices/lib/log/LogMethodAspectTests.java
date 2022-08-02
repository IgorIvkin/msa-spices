package com.igorivkin.msaspices.lib.log;

import com.igorivkin.msaspices.lib.config.MaskingConfig;
import com.igorivkin.msaspices.lib.convert.LogJsonConverter;
import com.igorivkin.msaspices.lib.model.LogMethodTestRequest;
import com.igorivkin.msaspices.lib.model.LogMethodTestResponse;
import com.igorivkin.msaspices.lib.service.LogPreparationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@JsonTest
@DisplayName("Unit-tests of aspect of method logging")
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {MaskingConfig.class, LogJsonConverter.class})
public class LogMethodAspectTests {

    private static final String REQUEST_NAME = "Igor";

    private static final Long REQUEST_SALARY = 10000L;

    private static final String REQUEST_OUTPUT =
            " param2={\"name\":\"Igor\",\"salary\":10000}";

    private static final String RESPONSE_OUTPUT = "{\"id\":1,\"name\":\"Igor\"}";

    private static final String RESPONSE_OUTPUT_IGNORED = "<ignored>";

    @SpyBean
    private LogPreparationService logPreparationService;

    private final Logger log = Mockito.spy(LoggerFactory.getLogger(LogMethodAspect.class));

    @Test
    @DisplayName("Logging method - basic case")
    public void logRestEndpoint_success() {
        ArgumentCaptor<String> requestStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> responseStringCaptor = ArgumentCaptor.forClass(String.class);
        TestLogRestService testLogRestService = getTestLogRestService();

        testLogRestService.testMethod(2L, getTestLogRequest());

        verify(logPreparationService, times(3)).convertObjectToString(any());
        verify(log, times(1)).debug(requestStringCaptor.capture(), anyString(), anyString());
        verify(log, times(1)).debug(anyString(), anyString(), anyString(),
                responseStringCaptor.capture());

        String requestValue = requestStringCaptor.getValue();
        assertThat(requestValue).contains(REQUEST_OUTPUT);
        String responseValue = responseStringCaptor.getValue();
        assertThat(responseValue).contains(RESPONSE_OUTPUT);
    }

    @Test
    @DisplayName("Logging method - do not log response")
    public void logRestEndpoint_doNotLogResponse() {
        ArgumentCaptor<String> requestStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> responseStringCaptor = ArgumentCaptor.forClass(String.class);
        TestLogRestService testLogRestService = getTestLogRestService();

        testLogRestService.testMethod2(2L, getTestLogRequest());

        verify(logPreparationService, times(2)).convertObjectToString(any());
        verify(log, times(1)).debug(requestStringCaptor.capture(), anyString(), anyString());
        verify(log, times(1)).debug(anyString(), anyString(), anyString(),
                responseStringCaptor.capture());

        String requestValue = requestStringCaptor.getValue();
        assertThat(requestValue).contains(REQUEST_OUTPUT);
        String responseValue = responseStringCaptor.getValue();
        assertThat(responseValue).doesNotContain(RESPONSE_OUTPUT);
        assertThat(responseValue).contains(RESPONSE_OUTPUT_IGNORED);
    }

    @Test
    @DisplayName("Logging method - do not log response and some parameters")
    public void logRestEndpoint_doNotLogResponseAndSomeParams() {
        ArgumentCaptor<String> requestStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> responseStringCaptor = ArgumentCaptor.forClass(String.class);
        TestLogRestService testLogRestService = getTestLogRestService();

        testLogRestService.testMethod3(2L, getTestLogRequest());

        verify(logPreparationService, times(1)).convertObjectToString(any());
        verify(log, times(1)).debug(requestStringCaptor.capture(), anyString(), anyString());
        verify(log, times(1)).debug(anyString(), anyString(), anyString(),
                responseStringCaptor.capture());

        String requestValue = requestStringCaptor.getValue();
        assertThat(requestValue).doesNotContain(REQUEST_OUTPUT);
        String responseValue = responseStringCaptor.getValue();
        assertThat(responseValue).doesNotContain(RESPONSE_OUTPUT);
        assertThat(responseValue).contains(RESPONSE_OUTPUT_IGNORED);
    }

    private TestLogRestService getTestLogRestService() {
        TestLogRestService testLogRestService = new TestLogRestService();
        AspectJProxyFactory factory = new AspectJProxyFactory(testLogRestService);
        LogMethodAspect aspect = new LogMethodAspect(logPreparationService);
        ReflectionTestUtils.setField(aspect, "log", log);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    private LogMethodTestRequest getTestLogRequest() {
        return LogMethodTestRequest.builder()
                .name(REQUEST_NAME)
                .salary(REQUEST_SALARY)
                .build();
    }

    /**
     * Class emulating the service containing annotated methods.
     */
    public static class TestLogRestService {

        @LogMethod
        public LogMethodTestResponse testMethod(Long param1, LogMethodTestRequest param2) {
            return LogMethodTestResponse.builder()
                    .id(1L)
                    .name(param2.getName())
                    .build();
        }

        @LogMethod(logResponse = false)
        public LogMethodTestResponse testMethod2(Long param1, LogMethodTestRequest param2) {
            return testMethod(param1, param2);
        }

        @LogMethod(logResponse = false, parameters = {"param1"})
        public LogMethodTestResponse testMethod3(Long param1, LogMethodTestRequest param2) {
            return testMethod(param1, param2);
        }
    }
}
