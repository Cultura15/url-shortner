package com.jesson.create_url_service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUrlRequestValidationTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void blankLongUrlIsRejected() {
        assertFalse(validator.validate(request(" ", Instant.now().plusSeconds(60))).isEmpty());
    }

    @Test
    void nonUrlLongUrlIsRejected() {
        assertFalse(validator.validate(request("not a url", Instant.now().plusSeconds(60))).isEmpty());
    }

    @Test
    void javascriptSchemeIsRejected() {
        assertFalse(validator.validate(request("javascript:alert(1)", Instant.now().plusSeconds(60))).isEmpty());
    }

    @Test
    void pastExpiryIsRejected() {
        assertFalse(validator.validate(request("https://example.com", Instant.now().minusSeconds(60))).isEmpty());
    }

    @Test
    void validRequestHasNoViolations() {
        assertTrue(validator.validate(request("https://example.com", Instant.now().plusSeconds(60))).isEmpty());
    }

    private CreateUrlRequest request(String longUrl, Instant expiresAt) {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl(longUrl);
        request.setExpiresAt(expiresAt);
        return request;
    }
}