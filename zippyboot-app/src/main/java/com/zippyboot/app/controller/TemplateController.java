package com.zippyboot.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zippyboot.api.dto.HealthDto;
import com.zippyboot.infra.es.ElasticsearchTemplate;
import com.zippyboot.infra.kafka.KafkaProducerTemplate;
import com.zippyboot.infra.redis.RedisTemplate;
import com.zippyboot.infra.satoken.service.SaTokenService;
import com.zippyboot.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/template")
public class TemplateController {

    private static final String DEMO_KAFKA_TOPIC = "zippyboot.demo.topic";

    private final ObjectProvider<RedisTemplate> redisTemplate;
    private final ObjectProvider<KafkaProducerTemplate> kafkaProducerTemplate;
    private final ObjectProvider<ElasticsearchTemplate> elasticsearchTemplate;
    private final ObjectProvider<SaTokenService> saTokenService;

    @GetMapping("/ping")
    public ApiResponse<HealthDto> ping() {
        return ApiResponse.ok(HealthDto.builder()
                .status("UP")
                .time(OffsetDateTime.now())
                .build());
    }

    @GetMapping("/sa-login")
    public ApiResponse<Map<String, Object>> saLogin() {
        StpUtil.login(10001L);
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", StpUtil.getTokenValue());
        payload.put("loginId", StpUtil.getLoginIdAsLong());
        return ApiResponse.ok(payload);
    }

    @GetMapping("/infra-check")
    public ApiResponse<Map<String, Object>> infraCheck() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("redis", redisTemplate.getIfAvailable() != null);
        payload.put("kafka", kafkaProducerTemplate.getIfAvailable() != null);
        payload.put("es", elasticsearchTemplate.getIfAvailable() != null);
        payload.put("satoken", saTokenService.getIfAvailable() != null);
        return ApiResponse.ok(payload);
    }

    @PostMapping("/kafka/send")
    public ApiResponse<Map<String, Object>> sendKafkaMessage(
            @RequestParam(defaultValue = DEMO_KAFKA_TOPIC) String topic,
            @RequestParam String message,
            @RequestParam(defaultValue = "demo-key") String key) {
        KafkaProducerTemplate producerTemplate = kafkaProducerTemplate.getIfAvailable();
        if (producerTemplate == null) {
            return ApiResponse.fail("KafkaProducerTemplate is not available. Check spring.kafka configuration.");
        }

        String traceId = UUID.randomUUID().toString();
        producerTemplate.send(MessageBuilder.withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("traceId", traceId)
                .build());

        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", topic);
        payload.put("key", key);
        payload.put("message", message);
        payload.put("traceId", traceId);
        return ApiResponse.ok(payload);
    }
}
