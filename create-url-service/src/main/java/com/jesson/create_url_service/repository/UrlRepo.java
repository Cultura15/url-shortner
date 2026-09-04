package com.jesson.create_url_service.repository;

import com.jesson.create_url_service.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UrlRepo extends JpaRepository<UrlEntity, Long> {

    @Query(value = "SELECT nextval('url_mapping_id_seq')", nativeQuery = true)
    Long getNextId();
}
