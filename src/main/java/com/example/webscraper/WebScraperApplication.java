package com.example.webscraper;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.annotation.PreDestroy;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
@CommonsLog
public class WebScraperApplication  {


    public static void main(String[] args) {
        SpringApplication.run(WebScraperApplication.class,args);
    }

    /**
     * Deconstructor.
     */
    @PreDestroy
    public void deconstructor() {
        log.info("Thanks !");
    }

}
