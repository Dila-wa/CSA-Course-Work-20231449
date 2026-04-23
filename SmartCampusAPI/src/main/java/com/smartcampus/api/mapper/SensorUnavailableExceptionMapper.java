package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.SensorUnavailableException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper extends AbstractExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public javax.ws.rs.core.Response toResponse(SensorUnavailableException exception) {
        return buildResponse(403, "Forbidden", exception.getMessage());
    }
}
