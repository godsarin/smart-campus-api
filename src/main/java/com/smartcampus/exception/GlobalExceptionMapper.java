package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace server-side (safe — not returned to client)
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper: " + exception.getMessage(), exception);

        ErrorResponse error = new ErrorResponse(
                500,
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please contact the system administrator. "
                        + "Reference time: " + System.currentTimeMillis()
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
