package com.zippy.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zippy")
public class ZippyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippyApplication.class, args);
    }
}
