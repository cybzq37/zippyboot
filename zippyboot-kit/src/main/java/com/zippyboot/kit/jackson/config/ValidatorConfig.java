package com.zippyboot.kit.jackson.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 校验框架配置类
 *
 * @author lichunqing
 */
@AutoConfiguration
public class ValidatorConfig {

    /**
     * 配置校验框架 快速返回模式
     */
    @Bean
    public ValidationConfigurationCustomizer validationConfigurationCustomizer() {
        return configuration -> configuration.addProperty("hibernate.validator.fail_fast", "true");
    }
}
