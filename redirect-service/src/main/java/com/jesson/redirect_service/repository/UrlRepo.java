package com.jesson.redirect_service.repository;

import com.jesson.redirect_service.entity.UrlEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UrlRepo extends Repository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortCode(String shortCode);
}
