package com.jesson.redirect_service.service;

import com.jesson.redirect_service.entity.UrlEntity;
import com.jesson.redirect_service.exception.UrlNotFoundException;
import com.jesson.redirect_service.repository.UrlRepo;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RedirectService {
    private final UrlRepo urlRepo;

    public RedirectService(UrlRepo urlRepo){
        this.urlRepo = urlRepo;;
    }

    public String getOriginalUrl(String shortCode){
        UrlEntity url = urlRepo.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())){
            throw new UrlNotFoundException(shortCode);
        }

        return url.getLongUrl();
    }
}
