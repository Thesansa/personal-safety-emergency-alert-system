package com.semali.sosbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SosBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SosBackendApplication.class, args);


    }
}
