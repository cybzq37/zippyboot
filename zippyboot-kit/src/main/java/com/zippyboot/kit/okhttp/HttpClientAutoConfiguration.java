package com.zippyboot.kit.okhttp;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient(HttpClientProperties properties) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                .callTimeout(properties.getCallTimeoutSeconds(), TimeUnit.SECONDS);

        if (properties.isEnableLogInterceptor()) {
            builder.addInterceptor(new HttpLogInterceptor());
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClient httpClient(OkHttpClient okHttpClient, HttpClientProperties properties) {
        return new HttpClient(okHttpClient, properties.isThrowOnHttpError());
    }
}
