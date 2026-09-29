package org.example.foodorderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FoodOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodOrderServiceApplication.class, args);
    }

}
