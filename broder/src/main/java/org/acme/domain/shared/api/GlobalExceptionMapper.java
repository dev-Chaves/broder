package org.acme.domain.shared.api;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.hibernate.exception.GenericJDBCException;

import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (isUniqueConstraintViolation(exception) || exception instanceof EntityExistsException) {
            LOG.warnf(exception, "Conflict while processing request: %s", exception.getMessage());
            return build(Response.Status.CONFLICT, "Resource already exists");
        }

        if (exception instanceof ConstraintViolationException) {
            LOG.warnf(exception, "Validation error while processing request: %s", exception.getMessage());
            return build(Response.Status.BAD_REQUEST, "Validation failed");
        }

        if (exception instanceof IllegalArgumentException || exception instanceof BadRequestException) {
            LOG.warnf(exception, "Bad request: %s", exception.getMessage());
            return build(Response.Status.BAD_REQUEST, defaultMessage(exception, "Invalid request"));
        }

        if (exception instanceof NoResultException || exception instanceof NotFoundException) {
            LOG.warnf(exception, "Resource not found: %s", exception.getMessage());
            return build(Response.Status.NOT_FOUND, "Resource not found");
        }

        if (exception instanceof NotAllowedException) {
            LOG.warnf(exception, "Method not allowed: %s", exception.getMessage());
            return build(Response.Status.METHOD_NOT_ALLOWED, "Method not allowed");
        }

        if (exception instanceof NotSupportedException) {
            LOG.warnf(exception, "Unsupported media type: %s", exception.getMessage());
            return build(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type");
        }

        if (exception instanceof WebApplicationException webApplicationException) {
            var status = Response.Status.fromStatusCode(webApplicationException.getResponse().getStatus());
            if (status == null) {
                LOG.errorf(exception, "Unhandled web application exception: %s", exception.getMessage());
                return build(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected internal error");
            }

            LOG.warnf(exception, "Web application exception: %s", exception.getMessage());
            return build(status, defaultMessage(exception, status.getReasonPhrase()));
        }

        LOG.error("Unhandled exception while processing request", exception);
        return build(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected internal error");
    }

    private Response build(Response.Status status, String message) {
        return Response.status(status)
                .entity(Map.of(
                        "status", status.getStatusCode(),
                        "error", status.getReasonPhrase(),
                        "message", message
                ))
                .build();
    }

    private String defaultMessage(Throwable exception, String fallback) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return fallback;
        }

        return exception.getMessage();
    }

    private boolean isUniqueConstraintViolation(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException) {
                return true;
            }

            if (current instanceof GenericJDBCException
                    && current.getMessage() != null
                    && current.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                return true;
            }

            if (current.getMessage() != null
                    && current.getMessage().contains("UNIQUE constraint failed")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
