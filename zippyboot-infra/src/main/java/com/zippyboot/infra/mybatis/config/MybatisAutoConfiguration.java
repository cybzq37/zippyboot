package com.zippyboot.infra.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zippyboot.infra.mybatis.handler.JsonTypeHandler;
import com.zippyboot.infra.mybatis.handler.JsonbTypeHandler;
import com.zippyboot.infra.mybatis.handler.MapJsonHandler;
import com.zippyboot.infra.mybatis.handler.ObjectNodeHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 自动配置。
 * <p>
 * 提供分页拦截器和 ObjectMapper 注入（同步 Spring Jackson 配置到 TypeHandler）。
 * <p>
 * 需在 application.yml 中配置 handler 扫描路径：
 * <pre>
 * mybatis-plus:
 *   type-handlers-package: com.zippyboot.infra.mybatis.handler
 * </pre>
 */
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    @Bean
    public TypeHandlerObjectMapperInjector typeHandlerObjectMapperInjector(ObjectMapper objectMapper) {
        JsonTypeHandler.setObjectMapper(objectMapper);
        JsonbTypeHandler.setObjectMapper(objectMapper);
        ObjectNodeHandler.setObjectMapper(objectMapper);
        MapJsonHandler.setObjectMapper(objectMapper);
        return new TypeHandlerObjectMapperInjector();
    }

    /** 标记 Bean，表示 ObjectMapper 已注入到 TypeHandler。 */
    public static class TypeHandlerObjectMapperInjector {
    }
}
