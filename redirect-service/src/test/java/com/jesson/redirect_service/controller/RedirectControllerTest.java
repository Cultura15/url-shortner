package com.jesson.redirect_service.controller;

import com.jesson.redirect_service.service.RedirectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private RedirectService redirectService;

    @InjectMocks
    private RedirectController redirectController;

    @Test
    void redirectReturnsFoundWithOriginalUrlAsLocation() {
        when(redirectService.getOriginalUrl("abc123")).thenReturn("https://example.com/destination");

        ResponseEntity<Void> response = redirectController.redirect("abc123");

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(URI.create("https://example.com/destination"), response.getHeaders().getLocation());
        verify(redirectService).getOriginalUrl("abc123");
    }
}
