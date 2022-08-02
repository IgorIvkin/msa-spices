package com.igorivkin.msaspices.lib.log;

import com.igorivkin.msaspices.lib.service.LogPreparationService;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public abstract class AbstractLogMethodAspect {

    protected static final String REQUEST_ID = "request-id";

    protected static final String IGNORED = "<ignored>";

    protected final LogPreparationService logPreparationService;

    protected Logger log;

    @Autowired
    public AbstractLogMethodAspect(LogPreparationService logPreparationService) {
        this.logPreparationService = logPreparationService;
    }

    /**
     * Will log the request if parameter contains in the allowed parameters or
     * if allowed parameters list is empty.
     *
     * @param method          annotated method
     * @param args            list of arguments of annotated method
     * @param parametersToLog list of argument names to log
     */
    protected void logNonReactiveRequest(MethodSignature method, Object[] args, String[] parametersToLog) {
        final StringBuilder logResult = new StringBuilder();
        logResult.append("\n>>> Request {}, method {}");

        final String[] parameterNames = method.getParameterNames();
        for (var i = 0; i < parameterNames.length; i++) {
            final String parameterName = parameterNames[i];
            if (useParameterToLog(parameterName, parametersToLog)) {
                final Object parameterValue = args[i];
                logResult.append("\n ")
                        .append(parameterName)
                        .append("=")
                        .append(logPreparationService.convertObjectToString(parameterValue));
            }
        }

        log.debug(logResult.toString(), MDC.get(REQUEST_ID), method.getName());
    }

    /**
     * Will log the request if parameter contains in the allowed parameters or
     * if allowed parameters list is empty.
     *
     * @param method          annotated method
     * @param args            list of arguments of annotated method
     * @param parametersToLog list of argument names to log
     */
    protected List<Object> logReactiveRequest(MethodSignature method, Object[] args, String[] parametersToLog) {
        final List<Object> instrumentedArgs = new ArrayList<>();
        final String logRequestHeader = "\n>>> Request {}, method {}";
        log.debug(logRequestHeader, MDC.get(REQUEST_ID), method.getName());

        final String[] parameterNames = method.getParameterNames();
        for (var i = 0; i < parameterNames.length; i++) {
            final String parameterName = parameterNames[i];
            final Object currentArgument = args[i];
            if (useParameterToLog(parameterName, parametersToLog)) {
                instrumentedArgs.add(logParameter(parameterName, currentArgument));
            } else {
                instrumentedArgs.add(currentArgument);
            }
        }

        return instrumentedArgs;
    }

    /**
     * Runs a special instrumentation for Mono/Flux parameters (will add one more listener on it).
     * Logs non-reactive parameter without ny changes.
     *
     * @param parameterName  name of current parameter
     * @param parameterValue value of current parameter
     * @return value of current parameter, will be modified for Mono/Flux
     */
    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    private Object logParameter(String parameterName, Object parameterValue) {
        if (parameterValue instanceof Mono) {
            final Mono<?> monoParameter = (Mono<?>) parameterValue;
            return monoParameter.doOnNext(p -> logNonReactiveParameter(parameterName, p));
        } else if (parameterValue instanceof Flux) {
            // TODO
            return parameterValue;
        } else {
            return logNonReactiveParameter(parameterName, parameterValue);
        }
    }

    /**
     * Logs non-reactive parameters so any parameter that is not instance of Flux or Mono.
     * Tracks also ID of request.
     *
     * @param parameterName  name of parameter
     * @param parameterValue value of parameter
     * @return value of parameter
     */
    private Object logNonReactiveParameter(String parameterName, Object parameterValue) {
        log.debug("\nRequest {}, {}={}",
                MDC.get(REQUEST_ID), parameterName, logPreparationService.convertObjectToString(parameterValue));
        return parameterValue;
    }

    /**
     * Parameter is used to log if there is no specified parameter names for logging
     * or if it's inside the list of allowed params.
     *
     * @param parameterName   current parameter name
     * @param parametersToLog list of parameter names to log
     * @return true if we need to log the parameter
     */
    protected boolean useParameterToLog(String parameterName, String[] parametersToLog) {
        return parametersToLog.length == 0 || arrayContains(parametersToLog, parameterName);
    }

    /**
     * Checks whether the given array of parameter names contains a given parameter name.
     *
     * @param parametersToLog list of parameter names to log
     * @param parameterName   parameter name to check in the list
     * @return true if list contains a given parameter name
     */
    protected boolean arrayContains(String[] parametersToLog, String parameterName) {
        for (String currentParameterName : parametersToLog) {
            if (Objects.equals(currentParameterName, parameterName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs the response of method which is annotated.
     * Logging is making by calling .toString().
     *
     * @param method      current method
     * @param response    a response of annotated method
     * @param logResponse do log the object of response
     */
    protected Object logNonReactiveResponse(MethodSignature method, Object response, boolean logResponse) {
        final String logResult = "\n<<< Response {}, method: {}"
                + "\n{}";

        final String logResponseResult;
        if (logResponse) {
            logResponseResult = logPreparationService.convertObjectToString(response);
        } else {
            logResponseResult = IGNORED;
        }

        log.debug(logResult, MDC.get(REQUEST_ID), method.getName(), logResponseResult);
        return response;
    }

    /**
     * Runs a special instrumentation for Mono/Flux response (add one more listener to it) and
     * logs any other type of response as it is.
     *
     * @param method      annotated method
     * @param response    result of annotated method
     * @param logResponse do log response?
     * @return object to log, will be modified for Mono/Flux
     */
    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    protected Object logReactiveResponse(MethodSignature method, Object response, boolean logResponse) {
        if (response instanceof Mono) {
            final Mono<?> monoResponse = (Mono<?>) response;
            return monoResponse.doOnNext(r -> logNonReactiveResponse(method, r, logResponse));
        } else if (response instanceof Flux) {
            if (logResponse) {
                Flux<?> fluxResponse = (Flux<?>) response;
                return fluxResponse.doOnNext(this::logReactiveFluxElement);
            } else {
                log.debug("\n<<< Response {}, method: {}\n" + IGNORED, MDC.get(REQUEST_ID), method.getName());
                return response;
            }
        } else {
            throw new IllegalArgumentException("Response is logging with reactive method but is not Mono or Flux");
        }
    }

    /**
     * Logs next element of Flux sequence.
     *
     * @param response element from Flux sequence
     */
    private void logReactiveFluxElement(Object response) {
        log.debug("\n<<< Response {}, next flux element = {}", MDC.get(REQUEST_ID),
                logPreparationService.convertObjectToString(response));
    }
}
