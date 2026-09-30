package com.jesson.create_url_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolationReturnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("duplicate"));
        assertError(response, HttpStatus.CONFLICT, "Conflict", "The request could not be completed due to a data conflict.");
    }

    @Test
    void dataAccessFailureReturnsServiceUnavailable() {
        ResponseEntity<ErrorResponse> response = handler.handleDataAccessException(new DataAccessResourceFailureException("offline"));
        assertError(response, HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "Could not reach the database. Please try again shortly.");
    }

    @Test
    void unexpectedExceptionReturnsInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"));
        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something went wrong. Please try again later.");
    }

    @Test
    void validationExceptionReturnsCombinedFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "longUrl", "Long URL cannot be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertError(response, HttpStatus.BAD_REQUEST, "Bad Request", "longUrl: Long URL cannot be blank");
    }

    private void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String error, String message) {
        assertEquals(status, response.getStatusCode());
        assertEquals(status.value(), response.getBody().getStatus());
        assertEquals(error, response.getBody().getError());
        assertEquals(message, response.getBody().getMessage());
    }
}
