package com.zippyboot.infra.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import com.zippyboot.infra.redis.RedisTemplate;
import com.zippyboot.infra.satoken.dao.RedisSaTokenDao;
import com.zippyboot.infra.satoken.service.SaPermissionImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 自动配置
 * <p>
 * 通过 {@code zippyboot.infra.satoken.enabled=true} 启用（默认开启）
 *
 * @author lichunqing
 */
@Configuration
@ConditionalOnClass(StpLogic.class)
@ConditionalOnProperty(prefix = "zippyboot.satoken", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SaTokenProperties.class)
public class SaTokenConfig {

    @Bean
    @ConditionalOnMissingBean(StpLogic.class)
    public StpLogic stpLogic(SaTokenProperties properties) {
        return new StpLogic(properties.getLoginType());
    }

    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface() {
        return new SaPermissionImpl();
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean(SaTokenDao.class)
    public SaTokenDao saTokenDao(RedisTemplate redisTemplate) {
        return new RedisSaTokenDao(redisTemplate);
    }
}
