package com.jesson.create_url_service.service;

import com.jesson.create_url_service.entity.UrlEntity;
import com.jesson.create_url_service.repository.UrlRepo;
import com.jesson.create_url_service.utils.Base62;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UrlService {
    Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepo urlRepo;

    @PersistenceContext
    EntityManager entityManager;

    public UrlService(UrlRepo urlRepo) {
        this.urlRepo = urlRepo;
    }

    @Transactional
    public UrlEntity createUrl(String longUrl, Instant expiresAt) {
        logger.info("Starting url creation request");

        Long id = urlRepo.getNextId();
        logger.debug("Generated new ID: {}", id);

        String shortCode = Base62.encodeBase62(id);
        logger.debug("Generated short code: {}", shortCode);

        UrlEntity url = new UrlEntity(id, shortCode, longUrl, Instant.now(), expiresAt);
        entityManager.persist(url);

        logger.info("Url creation request completed successfully");
        return url;
    }
}
