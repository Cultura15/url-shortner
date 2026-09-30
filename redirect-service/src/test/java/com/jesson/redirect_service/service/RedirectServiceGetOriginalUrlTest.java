package com.jesson.redirect_service.service;

import com.jesson.redirect_service.entity.UrlEntity;
import com.jesson.redirect_service.exception.UrlExpiredException;
import com.jesson.redirect_service.exception.UrlNotFoundException;
import com.jesson.redirect_service.repository.UrlRepo;
import io.lettuce.core.RedisException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectServiceGetOriginalUrlTest {

    @Mock
    private UrlRepo urlRepo;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void cacheHitReturnsCachedUrlWithoutQueryingRepository() {
        RedirectService service = service();
        when(valueOperations.get("shortUrl:abc123")).thenReturn("https://cached.example");

        String result = service.getOriginalUrl("abc123");

        assertEquals("https://cached.example", result);
        verify(urlRepo, never()).findByShortCode(any());
    }

    @Test
    void cacheMissWithActiveUrlCachesUrlWithRemainingTtl() {
        RedirectService service = service();
        UrlEntity entity = url("https://destination.example", Instant.now().plus(Duration.ofHours(1)));
        when(valueOperations.get("shortUrl:abc123")).thenReturn(null);
        when(urlRepo.findByShortCode("abc123")).thenReturn(Optional.of(entity));

        String result = service.getOriginalUrl("abc123");

        assertEquals("https://destination.example", result);
        verify(valueOperations).set(eq("shortUrl:abc123"), eq("https://destination.example"), any(Duration.class));
    }

    @Test
    void cacheMissWithNoEntityCachesNotFoundMarkerAndThrows() {
        RedirectService service = service();
        when(valueOperations.get("shortUrl:missing")).thenReturn(null);
        when(urlRepo.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> service.getOriginalUrl("missing"));

        verify(valueOperations).set("shortUrl:missing", "NOT_FOUND", Duration.ofMinutes(5));
    }

    @Test
    void cacheMissWithExpiredEntityThrowsWithoutCaching() {
        RedirectService service = service();
        UrlEntity entity = org.mockito.Mockito.mock(UrlEntity.class);
        when(entity.getExpiresAt()).thenReturn(Instant.now().minusSeconds(1));

        when(valueOperations.get("shortUrl:expired")).thenReturn(null);
        when(urlRepo.findByShortCode("expired")).thenReturn(Optional.of(entity));

        assertThrows(UrlExpiredException.class, () -> service.getOriginalUrl("expired"));

        verify(valueOperations, never()).set(any(), any(), any(Duration.class));
    }

    @Test
    void redisReadFailureFallsBackToRepository() {
        RedirectService service = service();
        UrlEntity entity = url("https://database.example", null);
        when(valueOperations.get("shortUrl:abc123")).thenThrow(new RedisException("unavailable"));
        when(urlRepo.findByShortCode("abc123")).thenReturn(Optional.of(entity));

        String result = service.getOriginalUrl("abc123");

        assertEquals("https://database.example", result);
        verify(urlRepo).findByShortCode("abc123");
        verify(valueOperations).set("shortUrl:abc123", "https://database.example", Duration.ofHours(24));
    }

    private RedirectService service() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return new RedirectService(urlRepo, redisTemplate);
    }

    private UrlEntity url(String longUrl, Instant expiresAt) {
        UrlEntity entity = org.mockito.Mockito.mock(UrlEntity.class);
        when(entity.getLongUrl()).thenReturn(longUrl);
        when(entity.getExpiresAt()).thenReturn(expiresAt);
        return entity;
    }
}
