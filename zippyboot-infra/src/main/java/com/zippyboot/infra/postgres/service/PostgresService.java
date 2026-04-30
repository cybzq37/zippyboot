package com.zippyboot.infra.postgres.service;

import com.zippyboot.infra.postgres.config.PostgresConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "zippyboot.infra.postgres", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PostgresService {

    private final PostgresConfig config;

    public int defaultPageSize() {
        return config.getDefaultPageSize();
    }

    public String defaultSchema() {
        return config.getSchema();
    }
}
