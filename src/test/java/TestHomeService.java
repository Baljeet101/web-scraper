import com.example.webscraper.service.HomeService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URISyntaxException;

@CommonsLog
public class TestHomeService {
    HomeService homeService = new HomeService();

    @Value("${json.mandatory.fields}")
    String keys;

//    @DisplayName("Test loading a JSON file")
//    @Test
//    public void testJsonFilesEmpty() throws IOException, URISyntaxException {
//        Assertions.assertTrue(homeService.readJsonFile()
//                .getAsJsonArray()
//                .forEach(e ->
//                        System.out.println(Arrays.stream(keys.split(","))
//                                .allMatch(y ->
//                                        e.getAsJsonObject().has(y)))));
//    }

    @DisplayName("Test the amount of json files not zero")
    @Test
    public void testJsonFilesSize() throws IOException, URISyntaxException {
        Assertions.assertNotEquals(homeService.readJsonFiles().size(),0,"Atleast one json file should be present");
    }

    @DisplayName("Test the rest api /api/getData")
    @Test
    public void testGetData() throws IOException, URISyntaxException {
//        JsonArray array = homeService.readJsonFiles();
//        log.debug("All JSON files : \n "+array.toString());
//        for (JsonElement jsonElement:
//                array) {
//            if(homeService.validate(jsonElement)){
//                JsonElement cloneJson = homeService.process(jsonElement);
//                System.out.println(cloneJson.getAsString());
//            }
//        }
    }


    /**
     * Example:
     * browser:
     * @param
     * @return ResponseEntity
     */
    @Test
    @GetMapping("/fetch")
    public void fetch(){
        String result = "";
        String url = "https://www.techmeme.com/events";
        try{
            JsonArray j = new JsonArray();
            Document d =  Jsoup.connect(url).get();
            d.select("#events > div").forEach( e-> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("eventname", String.valueOf(e.children().select("div > a > div:nth-child(2)").text()));
                jsonObject.addProperty("date", String.valueOf(e.children().select("div > a > div:nth-child(1)").text()));
                jsonObject.addProperty("location",String.valueOf(e.children().select("div > a > div:nth-child(3)").text()));
                j.add(jsonObject);
//                System.out.println(e.toString());
            });
            log.info(j.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
