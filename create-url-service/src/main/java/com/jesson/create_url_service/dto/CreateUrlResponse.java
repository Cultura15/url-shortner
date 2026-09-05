package com.jesson.create_url_service.dto;

import java.time.Instant;

public class CreateUrlResponse {
    private final String shortUrl;
    private final String shortCode;
    private final String originalUrl;
    private final Instant expiresAt;

    public CreateUrlResponse(String shortUrl, String shortCode, String originalUrl, Instant expiresAt) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
    }

    public String getShortUrl(){return shortUrl;}
    public String getShortCode(){return shortCode;}
    public String getOriginalUrl(){return originalUrl;}
    public Instant getExpiresAt(){return expiresAt;}
}
