package com.zippyboot.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zippyboot.netty")
public class ZippyBootNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippyBootNettyApplication.class, args);
    }
}
