package com.smartcampus.api;

import com.smartcampus.api.filter.ApiLoggingFilter;
import com.smartcampus.api.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.api.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.api.mapper.SensorUnavailableExceptionMapper;
import com.smartcampus.api.mapper.ThrowableMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
        packages("com.smartcampus.api.resource");
        register(JacksonFeature.class);
        register(ApiLoggingFilter.class);
        register(RoomNotEmptyExceptionMapper.class);
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(ThrowableMapper.class);
    }
}
