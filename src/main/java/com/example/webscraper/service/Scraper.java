package com.example.webscraper.service;

import com.example.webscraper.model.EventInfo;
import com.example.webscraper.model.ScrapWebsite;
import com.example.webscraper.repository.EventInfoRepository;
import com.example.webscraper.repository.ScrapWebsiteRepository;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;



@Service
@CommonsLog
public class Scraper {
    @Autowired
    ScrapWebsiteRepository scrapWebsiteRepository;

    @Autowired
    EventInfoRepository eventInfoRepository;

    @Value("${json.mandatory.fields}")
    private String jsonFields;

    /**
     *
     * @param jsonElement
     * @return
     * @throws IOException
     */
    public void scrape(JsonElement jsonElement) throws IOException {
        AtomicReference<JsonObject> jsonObject = new AtomicReference<>(new JsonObject());
        JsonArray jsonArray = new JsonArray();
        if(jsonElement != null){
            Document doc = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(jsonElement.getAsString());
            JsonNode jsonObj = mapper.readTree(parser);
            String[] mandatoryFields = jsonFields.split(",");
            final  String url = jsonObj.has("url") ? jsonObj.get("url").asText().trim() : null;
            doc = Jsoup.connect(url).get();
            if(doc != null ){
                ScrapWebsite scrapWebsite = new ScrapWebsite();
                String mainElement = jsonObj.has("mainelement") ? jsonObj.get("mainelement").asText().trim() : null;
                assert mainElement != null;
                String websiteName = jsonObj.has("websitename") ? jsonObj.get("websitename").asText().trim() : null;
                assert websiteName != null;
                if(websiteName.trim().length() > 0 && scrapWebsiteRepository.findScrapWebsiteByWebsiteName(websiteName.trim()) != null){
                    log.info("There is already data present for this website : "+websiteName);
                    log.info("First Delete existing data for this website : "+websiteName);
                    return;
                }
                List<EventInfo> list = new ArrayList<>();
                final int[] eventId = {1};

                AtomicReference<SimpleDateFormat> formatter = new AtomicReference<>();
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                log.debug("Checking enginge = "+engine);
                doc.select(mainElement).forEach( e -> {
                    EventInfo eventInfo = new EventInfo();
                    eventInfo.setEventName(String.valueOf(e.children().select(jsonObj.has("eventname") && jsonObj.get("eventname").asText().length()>1 ? jsonObj.get("eventname").asText().trim() : "").text()));
                    eventInfo.setEventLocation(String.valueOf(e.children().select(jsonObj.has("location") && jsonObj.get("location").asText().length()>1 ? jsonObj.get("location").asText().trim() : "").text()));

                    // fetching the selector for end date
                    String endDate = jsonObj.has("enddate") && jsonObj.get("enddate").asText().length()>1 ? jsonObj.get("enddate").asText().trim() : "";
                    log.debug("Selector for end Date for event Name  "+eventInfo.getEventName()+" is -> "+endDate);
                    // fetching the selector for start date
                    String startDate = jsonObj.has("startdate") && jsonObj.get("startdate").asText().length()>1 ? jsonObj.get("startdate").asText().trim() : "";
                    log.debug("Selector for start Date for event Name :: "+eventInfo.getEventName()+" is -> "+startDate);

                    try {
                        // scraping the end date
                        endDate = String.valueOf(e.children().select(endDate).text());
                        if(endDate == null)throw new ParseException("Set Default date",1);
                        final String patternEtDt = jsonObj.has("enddateformat") && jsonObj.get("enddateformat").asText().length()>1 ? jsonObj.get("enddateformat").asText().trim() : null;

                        if(patternEtDt != null){
                            if(patternEtDt.trim().contains("function")){
                                Bindings  obj = (Bindings )engine.eval(patternEtDt);
                                Invocable invocable = (Invocable) engine;
                                Object funcResult = invocable.invokeFunction("handle", endDate);
                                JsonElement json = new Gson().toJsonTree(funcResult);
                                log.debug("javascript function written for end date : \n"+json);

                                formatter.set(new SimpleDateFormat(String.valueOf(json.getAsJsonObject().get("format")), Locale.ENGLISH));
                                eventInfo.setEndDate(formatter.get().parse(String.valueOf(json.getAsJsonObject().get("valDate"))));
                            }
                            else{
                                formatter.set(new SimpleDateFormat(patternEtDt));
                                eventInfo.setEndDate(formatter.get().parse(endDate));
                            }

                        }

                    } catch (ParseException ex) {
                        ex.printStackTrace();
                        try {
                            log.info("Setting the end date 31/01/2001   for event name = " + eventInfo.getEventName() + " as default 31 Jan 2001");
//                            endDate = formatter.get().parse("31/01/2001");
                            eventInfo.setEndDate(formatter.get().parse("31/01/2001"));
                        } catch (ParseException exc) {
                            log.debug("Problems setting default end date for row with event name = " + eventInfo.getEventName());
                            exc.printStackTrace();
                        }
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.debug("Problems runnning the javascript for end date for event name = " + eventInfo.getEventName());
                        ex.printStackTrace();
                    }

                    try {
                        // scraping the start date
                        startDate = String.valueOf(e.children().select(startDate).text());
                        if(startDate == null)throw new ParseException("Set Default startDate date",1);
                        final String patternStDt = jsonObj.has("startdateformat") && jsonObj.get("startdateformat").asText().length()>1 ? jsonObj.get("startdateformat").asText().trim() : null;

                        if(patternStDt != null){
                            if(patternStDt.trim().contains("function")){
                                Bindings  obj = (Bindings )engine.eval(patternStDt);
                                Invocable invocable = (Invocable) engine;
                                Object funcResult = invocable.invokeFunction("handle", startDate);
                                JsonElement json = new Gson().toJsonTree(funcResult);
                                log.debug("javascript function written for start date : \n"+json);
                                formatter.set(new SimpleDateFormat(String.valueOf(json.getAsJsonObject().get("format")), Locale.ENGLISH));
                                eventInfo.setStartDate(formatter.get().parse(String.valueOf(json.getAsJsonObject().get("valDate"))));
                            }
                            else{
                                formatter.set(new SimpleDateFormat(patternStDt));
                                eventInfo.setStartDate(formatter.get().parse(startDate));
                            }

                        }

                    } catch (ParseException ex) {
                        ex.printStackTrace();
                        try {
                            log.info("Setting the start date 31/01/2001   for event name = " + eventInfo.getEventName() + " as default 31 Jan 2001");
//                            endDate = formatter.get().parse("31/01/2001");
                            eventInfo.setStartDate(formatter.get().parse("31/01/2001"));
                        } catch (ParseException exc) {
                            log.debug("Problems setting default start date for row with event name = " + eventInfo.getEventName());
                            exc.printStackTrace();
                        }
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.debug("Problems runnning the javascript for start date for event name = " + eventInfo.getEventName());
                        ex.printStackTrace();
                    }

//                    Date startDate;
//                    try {
//                        startDate = formatter.get().parse(String.valueOf(e.children().select(jsonObj.has("startdate") && jsonObj.get("startdate").asText().length()>1  ? jsonObj.get("startdate").asText().trim() : "").text()));
//                        eventInfo.setStartDate(startDate);
//                    } catch (ParseException ex) {
//                        ex.printStackTrace();
//                        try {
//                            log.info("Setting the start date for event name = " + eventInfo.getEventName() + " as default 31 Jan 2001");
//                            startDate = formatter.get().parse("31/01/2001");
//                            eventInfo.setStartDate(startDate);
//                        } catch (ParseException exc) {
//                            log.debug("Problems setting default start date for row with event name = " + eventInfo.getEventName());
//                            exc.printStackTrace();
//                        }
//                    }
                    eventInfoRepository.save(eventInfo);
                    list.add(eventInfo);
                });
                scrapWebsite.setUrl(url);
                scrapWebsite.setWebsiteName(websiteName);
                scrapWebsite.setEventList(list);
                scrapWebsiteRepository.save(scrapWebsite);
            }
        }
    }
}
