package com.zippyboot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zippyboot")
public class ZippyBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippyBootApplication.class, args);
    }
}
