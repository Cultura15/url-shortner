package com.jesson.create_url_service.service;

import com.jesson.create_url_service.entity.UrlEntity;
import com.jesson.create_url_service.repository.UrlRepo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceCreateUrlTest {

    @Mock
    private UrlRepo urlRepo;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UrlService urlService;

    @Test
    void createUrl_generatesBase62CodeAndPersistsCompleteEntity() {
        Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
        when(urlRepo.getNextId()).thenReturn(62L);
        ReflectionTestUtils.setField(urlService, "entityManager", entityManager);

        UrlEntity result = urlService.createUrl("https://example.com/long-url", expiresAt);

        ArgumentCaptor<UrlEntity> urlCaptor = ArgumentCaptor.forClass(UrlEntity.class);
        verify(urlRepo).getNextId();
        verify(entityManager).persist(urlCaptor.capture());

        UrlEntity persisted = urlCaptor.getValue();
        assertSame(persisted, result);
        assertEquals(62L, persisted.getId());
        assertEquals("10", persisted.getShortCode());
        assertEquals("https://example.com/long-url", persisted.getLongUrl());
        assertEquals(expiresAt, persisted.getExpiresAt());
    }
}
