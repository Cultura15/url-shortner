package com.jesson.redirect_service.exception;

public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException(String shortCode) {
        super("URL not found for short code: " + shortCode);
    }
}
