package com.zippy.app.config;

import com.zippy.api.sys.client.RemoteUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${sys.service.url:http://localhost:8081}")
    private String sysServiceUrl;

    @Bean
    RemoteUserService remoteUserService() {
        RestClient client = RestClient.builder().baseUrl(sysServiceUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(client))
                .build();
        return factory.createClient(RemoteUserService.class);
    }
}
