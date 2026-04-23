package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.RoomNotEmptyException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper extends AbstractExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public javax.ws.rs.core.Response toResponse(RoomNotEmptyException exception) {
        return buildResponse(409, "Conflict", exception.getMessage());
    }
}
