package com.jesson.redirect_service.service;

import com.jesson.redirect_service.entity.UrlEntity;
import com.jesson.redirect_service.exception.UrlExpiredException;
import com.jesson.redirect_service.exception.UrlNotFoundException;
import com.jesson.redirect_service.repository.UrlRepo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class RedirectService {
    private static final String CACHE_KEY_PREFIX = "shortUrl:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final UrlRepo urlRepo;
    private final StringRedisTemplate redisTemplate;

    public RedirectService(UrlRepo urlRepo, StringRedisTemplate redisTemplate) {
        this.urlRepo = urlRepo;
        this.redisTemplate = redisTemplate;
    }

    public String getOriginalUrl(String shortCode) {
        String cacheKey = CACHE_KEY_PREFIX + shortCode;

        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        UrlEntity url = urlRepo.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())) {
            throw new UrlExpiredException(shortCode);
        }

        redisTemplate.opsForValue().set(cacheKey, url.getLongUrl(), computeTtl(url.getExpiresAt(), Instant.now()));
        return url.getLongUrl();
    }

    private Duration computeTtl(Instant expiresAt, Instant now) {
        if (expiresAt == null) {
            return DEFAULT_TTL;
        }
        Duration timeUntilExpiry = Duration.between(now, expiresAt);
        return timeUntilExpiry.compareTo(DEFAULT_TTL) < 0 ? timeUntilExpiry : DEFAULT_TTL;
    }
}
