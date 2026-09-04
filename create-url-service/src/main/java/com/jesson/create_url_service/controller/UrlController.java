package com.jesson.create_url_service.controller;

import com.jesson.create_url_service.entity.UrlEntity;
import com.jesson.create_url_service.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/create-url")
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<UrlEntity> createUrl(@RequestParam String longUrl, @RequestParam(required = false) Instant expiresAt) {
        UrlEntity urlEntity = urlService.createUrl(longUrl, expiresAt);
        return ResponseEntity.ok(urlEntity);
    }
}
