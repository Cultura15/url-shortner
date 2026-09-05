package com.jesson.create_url_service.dto;

import java.time.Instant;

public class CreateUrlRequest   {
    private String longUrl;
    private Instant expiresAt;

    public CreateUrlRequest(){};

    public String getLongUrl(){return longUrl;}
    public void setLongUrl(String longUrl){this.longUrl=longUrl;}
    public Instant getExpiresAt(){return expiresAt;}
    public void setExpiresAt(Instant expiresAt){this.expiresAt=expiresAt;}
}
