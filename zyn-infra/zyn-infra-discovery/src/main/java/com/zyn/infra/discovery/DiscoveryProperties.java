package com.zyn.infra.discovery;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务发现配置，读取 zyn.discovery.services 下的服务地址。
 * <p>
 * 配置示例：
 * <pre>
 * zyn:
 *   discovery:
 *     services:
 *       sys: http://zyn-sys:8081
 *       biz: http://zyn-biz:8082
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "zyn.discovery")
public class DiscoveryProperties {

    /**
     * 服务地址映射，key 为服务名，value 为 baseUrl。
     */
    private Map<String, String> services = new HashMap<>();

    /**
     * 获取指定服务的 baseUrl，不存在时抛异常。
     */
    public String getRequiredUrl(String serviceName) {
        String url = services.get(serviceName);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "Service URL not configured: zyn.discovery.services." + serviceName);
        }
        return url;
    }
}
