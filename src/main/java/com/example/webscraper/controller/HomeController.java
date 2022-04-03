package com.example.webscraper.controller;

import com.example.webscraper.model.ScrapWebsite;
import com.example.webscraper.service.HomeService;
import com.example.webscraper.service.Scraper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
@CommonsLog
public class HomeController {
    @Autowired
    HomeService homeService;

    @Autowired
    Scraper scraper;

    /**
     * curl --location --request GET 'http://localhost:8080/api/getData' \
     * --header 'Authorization: Basic c2E6cGFzc3dvcmQ='
     * @param
     * @return ResponseEntity
     */
    @GetMapping(value = "/getData", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private ResponseEntity<Object> getData() {
        ResponseEntity<Object> result = null;
        JsonArray jsonArray = null;
        try {
            JsonArray array = homeService.readJsonFiles();
            log.debug("All JSON files : \n " + array.toString());
            jsonArray = new JsonArray();
            for (JsonElement jsonElement :
                    array) {
                if (homeService.validate(jsonElement)) {
                    JsonElement cloneJson = homeService.process(jsonElement);
                    log.info("CLONE JSON BEFORE SCRAPPING : \n" + cloneJson.toString());
                    scraper.scrape(cloneJson);

                    log.info("CLONE JSON AFTER SCRAPPING : \n" + fetchAllData().toString());

                } else {
                    log.warn("The JSON seems to be invalid : " +
                            jsonElement.getAsString());
                }
            }
            result = ResponseEntity
                    .status(HttpStatus.OK)
                    .body(fetchAllData().toString());
        } catch (Exception e) {
            log.warn("Exception in /getData end point",e.fillInStackTrace());

            result = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("INTERNAL_SERVER_ERROR");
        }
        return result;
    }

    /**
     * curl --location --request GET 'http://localhost:8080/api/fetchAll' \
     * --header 'Authorization: Basic c2E6cGFzc3dvcmQ='
     * http://localhost:8080/api/fetchAll
     * @return String
     */
    @GetMapping(value = "/fetchAll", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private String fetchAllData(){
        try {
            return homeService.fetchAll();

        }catch (Exception e) {
            log.warn("Exception in /fetchAll end point",e.fillInStackTrace());
            return "INTERNAL_SERVER_ERROR";
        }
    }
}
