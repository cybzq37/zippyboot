package com.zyn.infra.discovery;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 服务发现自动配置。
 * 引入 zyn-infra-discovery 依赖后自动生效。
 * 服务地址配置由 zyn-conf 模块提供。
 */
@AutoConfiguration
@ConditionalOnClass(HttpExchange.class)
@ConditionalOnProperty(prefix = "zyn.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(ServiceClientRegistrar.class)
public class DiscoveryAutoConfiguration {
}
