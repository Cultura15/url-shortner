package com.jesson.create_url_service.controller;

import com.jesson.create_url_service.dto.CreateUrlRequest;
import com.jesson.create_url_service.dto.CreateUrlResponse;
import com.jesson.create_url_service.entity.UrlEntity;
import com.jesson.create_url_service.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/create-url")
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<CreateUrlResponse> createUrl(@RequestBody CreateUrlRequest request){
        UrlEntity urlEntity = urlService.createUrl(request.getLongUrl(), request.getExpiresAt());
        String shortUrl = "https://us.jessoncultura.info/" + urlEntity.getShortCode();

        CreateUrlResponse response = new CreateUrlResponse(shortUrl, urlEntity.getShortCode(), urlEntity.getLongUrl(), urlEntity.getExpiresAt());
        return ResponseEntity.created(URI.create(shortUrl)).body(response);
    }
}
