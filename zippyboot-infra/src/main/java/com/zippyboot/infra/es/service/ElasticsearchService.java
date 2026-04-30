package com.zippyboot.infra.es.service;

import com.zippyboot.infra.es.config.ElasticsearchConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchOperations.class)
@ConditionalOnProperty(prefix = "zippyboot.infra.es", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchService {

    private final ElasticsearchConfig config;

    public String withPrefix(String indexName) {
        return config.getIndexPrefix() + indexName;
    }

    public int shards() {
        return config.getShards();
    }

    public int replicas() {
        return config.getReplicas();
    }
}
