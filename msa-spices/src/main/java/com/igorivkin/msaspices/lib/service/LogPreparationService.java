package com.igorivkin.msaspices.lib.service;

import com.igorivkin.msaspices.lib.convert.LogConversionType;
import com.igorivkin.msaspices.lib.convert.LogConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the functionality to prepare the objects for logging.
 * All the converters defined by their conversion type (for example JSON). Every converter should implement
 * an interface LogConverter and be valid @Component. This way we are able to find a suitable converter
 * using conversion type.
 */
@Service
public class LogPreparationService {

    private final Map<LogConversionType, LogConverter> converters;

    @Autowired
    public LogPreparationService(List<LogConverter> converterList) {
        converters = new HashMap<>();
        for (LogConverter converter : converterList) {
            converters.put(converter.getConversionType(), converter);
        }
    }

    /**
     * Converts an object to string
     *
     * @param obj object to logging
     * @return string representation of object to log
     */
    public String convertObjectToString(Object obj, LogConversionType conversionType) {
        LogConverter logConverter = converters.get(conversionType);
        if (logConverter == null) {
            throw new IllegalStateException("Cannot find converter for conversion type: " + conversionType);
        }
        return logConverter.convertObjectToString(obj);
    }

    /**
     * Converts an object to string. Uses JSON by default.
     *
     * @param obj object to logging
     * @return string representation of object to log
     */
    public String convertObjectToString(Object obj) {
        return convertObjectToString(obj, LogConversionType.JSON);
    }
}

