package com.zyn.infra.discovery;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * HttpExchange 客户端构建工具。
 * <p>
 * 用于在 @Configuration 类中快速创建 HttpExchange 客户端 Bean：
 * <pre>
 * &#64;Bean
 * public RemoteUserService remoteUserService(DiscoveryProperties props) {
 *     return ServiceProxyBuilder.build(RemoteUserService.class, props.getRequiredUrl("sys"));
 * }
 * </pre>
 */
public final class ServiceProxyBuilder {

    private ServiceProxyBuilder() {
    }

    public static <T> T build(Class<T> serviceType, String baseUrl) {
        RestClient client = RestClient.builder().baseUrl(baseUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(client))
                .build();
        return factory.createClient(serviceType);
    }
}
