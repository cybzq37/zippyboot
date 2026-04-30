package com.zippyboot.infra.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.kafka")
public class KafkaConfig {

    private boolean enabled = true;
    private String defaultTopic = "zippyboot-topic";
    private String consumerGroup = "zippyboot-group";
}
