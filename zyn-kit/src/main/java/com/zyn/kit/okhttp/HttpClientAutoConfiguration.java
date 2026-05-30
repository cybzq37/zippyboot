package com.zyn.kit.okhttp;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient(HttpClientProperties properties,
                                     ObjectProvider<okhttp3.Interceptor> customInterceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                .callTimeout(properties.getCallTimeoutSeconds(), TimeUnit.SECONDS);

        // 重试拦截器（最先执行）
        HttpClientProperties.Retry retry = properties.getRetry();
        if (retry.getMaxAttempts() > 1) {
            builder.addInterceptor(new RetryInterceptor(
                    retry.getMaxAttempts(),
                    retry.getBackoffMs(),
                    retry.getRetryOnStatusCodes()
            ));
        }

        // 日志拦截器
        if (properties.isEnableLogInterceptor()) {
            builder.addInterceptor(new LoggingInterceptor(
                    properties.getLogMaxBodyBytes(),
                    properties.getLogMaxBodyChars()
            ));
        }

        // 自定义拦截器（其他模块定义为 @Bean 即可自动注入）
        customInterceptors.orderedStream().forEach(builder::addInterceptor);

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClient httpClient(OkHttpClient okHttpClient, HttpClientProperties properties) {
        return new HttpClient(okHttpClient, properties.isThrowOnHttpError());
    }
}
