package com.example.fuelticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FuelTicketApplication {
    public static void main(String[] args) {
        SpringApplication.run(FuelTicketApplication.class, args);
    }
}
