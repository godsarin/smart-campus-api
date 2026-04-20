package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        LOGGER.warning("LinkedResourceNotFoundException: " + exception.getMessage());

        ErrorResponse error = new ErrorResponse(
                422,
                "LINKED_RESOURCE_NOT_FOUND",
                exception.getMessage()
        );

        // 422 Unprocessable Entity
        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
