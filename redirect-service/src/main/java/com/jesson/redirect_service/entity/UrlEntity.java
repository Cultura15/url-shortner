package com.jesson.redirect_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "url_mapping")
public class UrlEntity {

    @Id
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public UrlEntity(){}

    public Long getId(){return id;}
    public String getShortCode(){return shortCode;}
    public String getLongUrl(){return longUrl;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getExpiresAt(){return expiresAt;}
}
