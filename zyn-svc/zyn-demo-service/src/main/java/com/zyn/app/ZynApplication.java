package com.zyn.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zyn")
public class ZynApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZynApplication.class, args);
    }
}
