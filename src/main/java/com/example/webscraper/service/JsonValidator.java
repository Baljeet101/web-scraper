package com.example.webscraper.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@CommonsLog
public class JsonValidator {
    @Value("${json.mandatory.fields}")
    private String jsonFields;


    public boolean isValid(String json) throws JsonParseException,IOException {
        AtomicBoolean retValue = new AtomicBoolean(true);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(json);
        JsonNode jsonObj = mapper.readTree(parser);
//        Arrays.stream(jsonFields.split(",")).allMatch(jsonObj::has);
        String[] mandatoryFields = jsonFields.split(",");
        Arrays.stream(mandatoryFields).forEach(
                e -> {
                    if (retValue.get()){
                        if (!jsonObj.has(e.trim())) {
                            log.warn("JSON file doesn't have all mandatory tags("+jsonFields+") : \n" + jsonObj.toString() + "\n");
                            retValue.set(false);
                        }
                    }
                }
        );
        log.debug(jsonObj.toString());
        return retValue.get();
    }
}
