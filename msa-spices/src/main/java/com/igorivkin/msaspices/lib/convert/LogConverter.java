package com.igorivkin.msaspices.lib.convert;

/**
 * Marking interface for conversion classes. These components are used to convert
 * object values to string representation.
 */
public interface LogConverter {

    LogConversionType getConversionType();

    String convertObjectToString(Object obj);
}
