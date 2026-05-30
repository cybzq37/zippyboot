package com.zyn.sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zyn")
public class ZynSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZynSysApplication.class, args);
    }
}
