package com.example.webscraper.repository;

import com.example.webscraper.model.EventInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventInfoRepository extends JpaRepository<EventInfo, Integer> {
}
