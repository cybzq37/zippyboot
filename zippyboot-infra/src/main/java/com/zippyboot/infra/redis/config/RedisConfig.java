package com.zippyboot.infra.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.redis")
public class RedisConfig {

    private String keyPrefix = "zippy:";
    private long defaultTtlSeconds = 3600;
}
