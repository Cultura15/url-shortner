package com.jesson.create_url_service.entity;

import jakarta.persistence.*;
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

    public UrlEntity() {}

    public UrlEntity(Long id, String shortCode, String longUrl, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getShortCode(){return shortCode;}
    public void setShortCode(String shortCode){this.shortCode = shortCode;}
    public String getLongUrl(){return longUrl;}
    public void setLongUrl(String longUrl){this.longUrl = longUrl;}
    public Instant getCreatedAt(){return createdAt;}
    public void setCreatedAt(Instant createdAt){this.createdAt = createdAt;}
    public Instant getExpiresAt(){return expiresAt;}
    public void setExpiresAt(Instant expiresAt){this.expiresAt = expiresAt;}
}
