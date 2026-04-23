package com.smartcampus.api.mapper;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ThrowableMapper extends AbstractExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public javax.ws.rs.core.Response toResponse(Throwable exception) {
        return buildResponse(500, "Internal Server Error", "An unexpected error occurred.");
    }
}
