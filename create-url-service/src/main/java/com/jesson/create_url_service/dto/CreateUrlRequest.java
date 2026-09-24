package com.jesson.create_url_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public class CreateUrlRequest   {

    @NotBlank(message = "Long URL cannot be blank")
    @URL(message = "Long URL must be valid url")
    @Pattern(regexp = "^https?://.*", message = "longUrl must start with http:// or https://")
    private String longUrl;

    @Future(message = "Expiration date must be in the future")
    private Instant expiresAt;

    public CreateUrlRequest(){};

    public String getLongUrl(){return longUrl;}
    public void setLongUrl(String longUrl){this.longUrl=longUrl;}
    public Instant getExpiresAt(){return expiresAt;}
    public void setExpiresAt(Instant expiresAt){this.expiresAt=expiresAt;}
}
