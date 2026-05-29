package com.zippy.sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zippy")
public class ZippySysApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippySysApplication.class, args);
    }
}
