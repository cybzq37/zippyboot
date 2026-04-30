package com.zippyboot.infra.redis.service;

import com.zippyboot.infra.redis.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "zippyboot.infra.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private final RedisConfig config;

    public void put(String key, String value) {
        String fullKey = config.getKeyPrefix() + key;
        redisTemplate.opsForValue().set(fullKey, value, Duration.ofSeconds(config.getDefaultTtlSeconds()));
    }

    public Optional<String> get(String key) {
        String fullKey = config.getKeyPrefix() + key;
        return Optional.ofNullable(redisTemplate.opsForValue().get(fullKey));
    }
}
