package com.example.webscraper.repository;

import com.example.webscraper.model.ScrapWebsite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapWebsiteRepository extends JpaRepository<ScrapWebsite, Integer> {
    @Query(value = "SELECT * FROM websites w WHERE w.website_name = :websiteName",
            nativeQuery = true)
    ScrapWebsite findScrapWebsiteByWebsiteName(String websiteName);
}
