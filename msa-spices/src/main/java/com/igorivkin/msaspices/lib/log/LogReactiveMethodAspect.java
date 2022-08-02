package com.igorivkin.msaspices.lib.log;

import com.igorivkin.msaspices.lib.service.LogPreparationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * This aspect maintains logging of reactive methods (i.e. methods that accept or respond by Mono/Flux).
 * I should give a note that this class is not tested yet and has an experimental status.
 */
@Aspect
@Configuration
@EnableAspectJAutoProxy
public class LogReactiveMethodAspect extends AbstractLogMethodAspect {

    @Autowired
    public LogReactiveMethodAspect(LogPreparationService logPreparationService) {
        super(logPreparationService);
        log = LoggerFactory.getLogger(LogReactiveMethodAspect.class);
    }

    /**
     * Logs request and response of annotated method. Request params which is allowed to log
     * is possible to define in the "parameters"-param of annotation. It is possible to skip
     * logging of response with parameter "logResponse" (will log by default).
     *
     * @param jp            point where the aspect is working now
     * @param logAnnotation current annotation
     * @return result of working of annotated method
     * @throws Throwable possible when working with Reflection API
     */
    @Around("@annotation(logAnnotation)")
    public Object processLogReactiveMethod(ProceedingJoinPoint jp, LogReactiveMethod logAnnotation) throws Throwable {

        MDC.put(REQUEST_ID, UUID.randomUUID().toString());

        // Initialize common params of method calling and settings of annotation
        var args = jp.getArgs();
        var method = ((MethodSignature) jp.getSignature());
        var parametersToLog = logAnnotation.parameters();
        var logResponse = logAnnotation.logResponse();

        // Log request of the method, use the only params that are allowed to log
        final List<Object> instrumentedArgs = logReactiveRequest(method, args, parametersToLog);

        // Execute method and return its result. Log the response in the case
        // if it's allowed to log
        final Object result = jp.proceed(instrumentedArgs.toArray());
        return logResponse(method, result, logResponse);
    }

    /**
     * Logs the response of method which is annotated.
     * Logging is making by calling .toString().
     *
     * @param method      current method
     * @param response    a response of annotated method
     * @param logResponse do log the object of response
     */
    private Object logResponse(MethodSignature method, Object response, boolean logResponse) {
        if (response instanceof Mono || response instanceof Flux) {
            return logReactiveResponse(method, response, logResponse);
        } else {
            return logNonReactiveResponse(method, response, logResponse);
        }
    }
}
