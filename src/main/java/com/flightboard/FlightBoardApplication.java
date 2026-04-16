package com.flightboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightBoardApplication.class, args);
    }
}