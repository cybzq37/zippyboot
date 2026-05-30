package com.zyn.infra.discovery;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

/**
 * 服务发现自动配置。
 * <p>
 * 引入 {@code zyn-infra-discovery} 依赖后自动生效：
 * <ul>
 *     <li>读取 {@code zyn.discovery.services} 配置的服务地址</li>
 *     <li>扫描所有 {@link ServiceClient @ServiceClient} 标记的接口</li>
 *     <li>自动创建 HttpExchange 代理 Bean</li>
 * </ul>
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(DiscoveryProperties.class)
@ConditionalOnClass(HttpExchange.class)
@ConditionalOnProperty(prefix = "zyn.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(ServiceClientRegistrar.class)
@RequiredArgsConstructor
public class DiscoveryAutoConfiguration {

    private final DiscoveryProperties discoveryProperties;

    @PostConstruct
    public void init() {
        DiscoveryPropertiesHolder.set(discoveryProperties);
        Map<String, String> services = discoveryProperties.getServices();
        log.info("Service discovery initialized with {} service(s): {}", services.size(), services.keySet());
    }
}
