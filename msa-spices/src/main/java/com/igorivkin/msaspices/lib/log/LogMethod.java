package com.igorivkin.msaspices.lib.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogMethod {

    /**
     * List of parameter names to log. By default, all the params are logged.
     *
     * @return list of parameter names to log
     */
    String[] parameters() default {};

    /**
     * Do log the response? By default, it will log responses.
     *
     * @return do log the response
     */
    boolean logResponse() default true;
}
