package com.zippyboot.infra.es;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@AutoConfiguration
@ConditionalOnBean(ElasticsearchOperations.class)
public class ElasticsearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchTemplate elasticsearchTemplate(ElasticsearchOperations operations) {
        return new ElasticsearchTemplate(operations);
    }
}
