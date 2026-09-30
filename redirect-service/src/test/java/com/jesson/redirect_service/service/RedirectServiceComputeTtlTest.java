package com.jesson.redirect_service.service;

import com.jesson.redirect_service.repository.UrlRepo;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectServiceComputeTtlTest {

    private final RedirectService service = new RedirectService(
            org.mockito.Mockito.mock(UrlRepo.class),
            org.mockito.Mockito.mock(StringRedisTemplate.class)
    );

    @Test
    void capsTtlAtDefaultWhenExpiryIsFurtherThanDefault() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Instant expiresAt = now.plus(Duration.ofDays(10));

        assertEquals(Duration.ofHours(24), service.computeTtl(expiresAt, now));
    }

    @Test
    void returnsOneSecondWhenAlreadyExpired() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Instant expiresAt = now.minusSeconds(1);

        assertEquals(Duration.ofSeconds(1), service.computeTtl(expiresAt, now));
    }
}