package com.zippyboot.infra.es.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.es")
public class ElasticsearchConfig {

    private boolean enabled = true;
    private String indexPrefix = "zippy_";
    private int shards = 1;
    private int replicas = 0;
}
