package com.example.webscraper.service;

import com.example.webscraper.model.ScrapWebsite;
import com.example.webscraper.repository.ScrapWebsiteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class HomeService {
    @Autowired
    JsonValidator jsonValidator;

    @Autowired
    ScrapWebsiteRepository scrapWebsiteRepository;
    /**
     *
     * @return Json Array containing all the json files
     * @throws IOException
     * @throws URISyntaxException
     */
    public JsonArray readJsonFiles() throws IOException, URISyntaxException {
        String folderName = "scrapers";
        JsonArray jsonArray = new JsonArray();
        for (File f : getJsonFiles(folderName)) {
            jsonArray.add(String.valueOf(new ObjectMapper().readTree(f)));
        }
        return jsonArray;
//        return .new ObjectMapper().readTree(new ClassPathResource("webconfig.json").getFile());
    }

    /**
     *
     * @param input
     * @return whether a valid json or not
     * @throws IOException
     */
    public Boolean validate(JsonElement input) throws IOException {
        return input != null && jsonValidator.isValid(input.getAsString());
    }

    /**
     *
     * @param input
     * @return deep copies the json and fetches the css selectors
     * @throws IOException
     */
    public JsonElement process(JsonElement input) throws IOException{
        if(input != null && validate(input)){
            return input.deepCopy();
        }
        return null;
    }

    /**
     * 
     * @param folder
     * @return List of files in scrapers folder
     * @throws URISyntaxException
     * @throws IOException
     */
    private List<File> getJsonFiles (String folder) throws URISyntaxException, IOException {
        if (folder != null) {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource(folder);
            assert resource != null;
            List<File> collect = Files.walk(Paths.get(resource.toURI()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            return collect;
        }
        return null;
    }

//    private void fullFlowService() throws IOException, URISyntaxException {
//        JsonArray array = readJsonFiles();
//        log.debug("All JSON files : \n "+array.toString());
//        for (JsonElement jsonElement:
//                array) {
//            if(validate(jsonElement)){
//                JsonElement cloneJson = process(jsonElement);
//                System.out.println(cloneJson.getAsString());
//            }
//        }
//    }

    public String fetchAll(){
        try {
            log.info("In fetchAll method");
            List<ScrapWebsite> list =  scrapWebsiteRepository.findAll();
            log.info("List of all scrapWebsite :: \n"+ list.stream().map(ScrapWebsite::toString).collect(Collectors.toList()));
            Gson gson = new Gson();
           return gson.toJson(list);

        }catch (Exception e) {
            e.printStackTrace();
            return "INTERNAL_SERVER_ERROR";
        }
    }
}
