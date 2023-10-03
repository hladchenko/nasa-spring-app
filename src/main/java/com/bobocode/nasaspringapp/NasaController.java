package com.bobocode.nasaspringapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URI;


@Controller("/")
public class NasaController {

    @Value("${key}")
    private String key;


    @Cacheable("picture")
    @GetMapping

    public ResponseEntity<byte[]> get(@RequestParam int sol) throws JsonProcessingException {

        long max = 0;
        byte[] item;
        URI itemSrc = null;

        RestTemplate template = new RestTemplate();

        ResponseEntity<String> response = template.getForEntity(String.format("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=%d&api_key=%s", sol, key), String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode photos = root.path("photos");
        for (JsonNode node : photos) {

            URI source = URI.create(node.path("img_src").textValue());
            HttpHeaders headers = template.headForHeaders(source);

            if (headers.getLocation() != null) {
                source = headers.getLocation();
                headers = template.headForHeaders(source);
            }

            long length = headers.getContentLength();

            if (length > max) {
                max = length;
                itemSrc = source;
            }
        }

        if (itemSrc != null) {
            item = template.getForObject(itemSrc, byte[].class);
        } else {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(item);
    }
}
