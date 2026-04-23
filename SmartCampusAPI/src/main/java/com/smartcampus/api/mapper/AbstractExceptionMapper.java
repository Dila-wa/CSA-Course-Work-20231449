package com.smartcampus.api.mapper;

import com.smartcampus.api.model.ApiError;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public abstract class AbstractExceptionMapper {

    @Context
    protected UriInfo uriInfo;

    protected Response buildResponse(int status, String error, String message) {
        ApiError apiError = new ApiError(
                System.currentTimeMillis(),
                status,
                error,
                message,
                uriInfo == null ? "" : uriInfo.getPath()
        );
        return Response.status(status).entity(apiError).build();
    }
}
