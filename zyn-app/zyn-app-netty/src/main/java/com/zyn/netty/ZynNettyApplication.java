package com.zyn.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zyn.netty")
public class ZynNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZynNettyApplication.class, args);
    }
}
