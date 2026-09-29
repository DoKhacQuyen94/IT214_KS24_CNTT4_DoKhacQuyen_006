package org.example.menuservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MenuItemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuItemServiceApplication.class, args);
    }

}
