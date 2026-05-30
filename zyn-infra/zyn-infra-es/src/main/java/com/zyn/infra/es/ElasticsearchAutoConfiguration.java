package com.zyn.infra.es;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@AutoConfiguration(after = ElasticsearchDataAutoConfiguration.class)
@ConditionalOnClass(ElasticsearchOperations.class)
public class ElasticsearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EsClient esClient(ElasticsearchOperations operations) {
        return new EsClient(operations);
    }
}
