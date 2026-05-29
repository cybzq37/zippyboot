package com.zippy.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zippy.netty")
public class ZippyNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippyNettyApplication.class, args);
    }
}
