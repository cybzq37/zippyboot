package com.zippyboot.infra.postgres.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.postgres")
public class PostgresConfig {

    private boolean enabled = true;
    private String schema = "public";
    private int defaultPageSize = 20;
}
