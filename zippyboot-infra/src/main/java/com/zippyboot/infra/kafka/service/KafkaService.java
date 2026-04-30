package com.zippyboot.infra.kafka.service;

import com.zippyboot.infra.kafka.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(KafkaTemplate.class)
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaConfig config;

    public void send(String message) {
        kafkaTemplate.send(config.getDefaultTopic(), message);
    }

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
