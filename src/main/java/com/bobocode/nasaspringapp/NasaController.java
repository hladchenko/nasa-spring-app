package com.bobocode.nasaspringapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@RestController("/")
public class NasaController {

    @Value("${key}")
    private String key;


    @GetMapping
    public JsonNode get(@RequestParam int sol) throws JsonProcessingException {

        long max = 0;
        JsonNode item = null;

        RestTemplate template = new RestTemplate();

        ResponseEntity<String> response = template.getForEntity(String.format("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=%d&api_key=%s", sol, key), String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode photos = root.path("photos");
        for (JsonNode node : photos) {

            String source = node.path("img_src").textValue();

            ResponseEntity<String> res = template.getForEntity(source, String.class);
            long length = res.getHeaders().getContentLength();

            if (res.getStatusCode().is3xxRedirection()) {
                length = template.headForHeaders(Objects.requireNonNull(res.getHeaders().getLocation())).getContentLength();
            }

            if (length > max) {
                max = length;
                item = node;
            }

        }

        return item;
    }
}
