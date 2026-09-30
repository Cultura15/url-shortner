package com.jesson.redirect_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundExceptionReturnsNotFound() {
        assertError(handler.handleNotFound(new UrlNotFoundException("missing")), HttpStatus.NOT_FOUND,
                "Not Found", "URL not found for short code: missing");
    }

    @Test
    void expiredExceptionReturnsGone() {
        assertError(handler.handleExpired(new UrlExpiredException("old")), HttpStatus.GONE,
                "Gone", "URL has expired for short code: old");
    }

    @Test
    void dataAccessExceptionReturnsServiceUnavailable() {
        assertError(handler.handleDataAccessException(new DataAccessResourceFailureException("offline")),
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "Could not reach the database. Please try again shortly.");
    }

    @Test
    void unexpectedExceptionReturnsInternalServerError() {
        assertError(handler.handleUnexpected(new RuntimeException("boom")), HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "Something went wrong. Please try again later.");
    }

    private void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String error, String message) {
        assertEquals(status, response.getStatusCode());
        assertEquals(status.value(), response.getBody().getStatus());
        assertEquals(error, response.getBody().getError());
        assertEquals(message, response.getBody().getMessage());
    }
}
