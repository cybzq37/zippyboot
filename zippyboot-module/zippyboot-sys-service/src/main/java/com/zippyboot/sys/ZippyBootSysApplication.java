package com.zippyboot.sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zippyboot")
public class ZippyBootSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippyBootSysApplication.class, args);
    }
}
