package com.zippyboot.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zippyboot.api.dto.HealthDto;
import com.zippyboot.infra.es.service.ElasticsearchService;
import com.zippyboot.infra.kafka.service.KafkaService;
import com.zippyboot.infra.redis.service.RedisService;
import com.zippyboot.infra.satoken.service.SaTokenService;
import com.zippyboot.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/template")
public class TemplateController {

    private final ObjectProvider<RedisService> redisService;
    private final ObjectProvider<KafkaService> kafkaService;
    private final ObjectProvider<ElasticsearchService> elasticsearchService;
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
        payload.put("redis", redisService.getIfAvailable() != null);
        payload.put("kafka", kafkaService.getIfAvailable() != null);
        payload.put("es", elasticsearchService.getIfAvailable() != null);
        payload.put("satoken", saTokenService.getIfAvailable() != null);
        return ApiResponse.ok(payload);
    }
}
