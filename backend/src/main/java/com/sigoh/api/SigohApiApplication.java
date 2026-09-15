package com.sigoh.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // habilita a rotina de snapshot diario de ocupacao
public class SigohApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SigohApiApplication.class, args);
    }
}
