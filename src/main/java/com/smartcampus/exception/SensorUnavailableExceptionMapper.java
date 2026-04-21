package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    private static final Logger LOGGER = Logger.getLogger(SensorUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        LOGGER.warning("SensorUnavailableException: " + exception.getMessage());

        ErrorResponse error = new ErrorResponse(
                403,
                "SENSOR_UNAVAILABLE",
                exception.getMessage()
        );

        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
