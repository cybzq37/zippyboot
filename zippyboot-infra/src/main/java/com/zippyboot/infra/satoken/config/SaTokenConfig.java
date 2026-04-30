package com.zippyboot.infra.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import com.zippyboot.infra.satoken.service.PlusSaTokenDao;
import com.zippyboot.infra.satoken.service.SaPermissionImpl;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "zippyboot.infra.satoken")
public class SaTokenConfig {

    private String loginType = "login";
    private String tokenPrefix = "Bearer";

    @Bean
    @ConditionalOnMissingBean(name = "stpLogic")
    public StpLogic getStpLogicJwt() {
        return new StpLogic(loginType);
    }

    /**
     * 权限接口实现(使用bean注入方便用户替换)
     */
    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface() {
        return new SaPermissionImpl();
    }

    /**
     * 自定义dao层存储
     */
    @Bean
    @ConditionalOnMissingBean(SaTokenDao.class)
    public SaTokenDao saTokenDao(RedisTemplate<String, Object> redisTemplate) {
        return new PlusSaTokenDao(redisTemplate);
    }
}
