package com.bobocode.nasaspringapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class NasaSpringAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(NasaSpringAppApplication.class, args);
    }

}
