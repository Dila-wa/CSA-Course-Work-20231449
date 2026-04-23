package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper extends AbstractExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public javax.ws.rs.core.Response toResponse(LinkedResourceNotFoundException exception) {
        return buildResponse(422, "Unprocessable Entity", exception.getMessage());
    }
}
