package com.jesson.create_url_service.service;

import com.jesson.create_url_service.entity.UrlEntity;
import com.jesson.create_url_service.repository.UrlRepo;
import com.jesson.create_url_service.utils.Base62;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UrlService {
    private final UrlRepo urlRepo;

    public UrlService(UrlRepo urlRepo) {
        this.urlRepo = urlRepo;
    }

    public UrlEntity createUrl(String longUrl, Instant expiresAt) {
        Long id = urlRepo.getNextId();
        String shortCode = Base62.encodeBase62(id);
        UrlEntity url = new UrlEntity(id, shortCode, longUrl, Instant.now(), expiresAt);
        return urlRepo.save(url);
    }
}
