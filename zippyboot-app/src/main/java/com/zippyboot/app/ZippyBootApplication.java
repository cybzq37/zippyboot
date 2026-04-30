package com.zippyboot.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zippyboot.infra.postgres.mapper")
@SpringBootApplication(scanBasePackages = "com.zippyboot")
public class ZippyBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippyBootApplication.class, args);
    }
}
