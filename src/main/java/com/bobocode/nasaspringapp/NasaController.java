package com.bobocode.nasaspringapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller("/")
public class NasaController {

    @Value("${key}")
    private String key;

    @Cacheable("largest-picture")
    @GetMapping
    public ResponseEntity<?> get(@RequestParam int sol) throws JsonProcessingException {
        RestTemplate template = new RestTemplate();
        return Objects.requireNonNull(template.getForEntity(String.format("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=%d&api_key=%s", sol, key), JsonNode.class).getBody())
                .get("photos")
                .findValuesAsText("img_src")
                .parallelStream()
                .collect(Collectors.toMap(this::getRedirectedResponse, src -> src))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(o -> o.getKey().getHeaders().getContentLength()))
                .orElseThrow().getKey();
    }

    private ResponseEntity<byte[]> getRedirectedResponse(String src) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<byte[]> entry = template.getForEntity(src, byte[].class);
        while (entry.getStatusCode().is3xxRedirection()) {
            entry = template.getForEntity(Objects.requireNonNull(entry.getHeaders().getLocation()), byte[].class);
        }
        return entry;
    }

}
