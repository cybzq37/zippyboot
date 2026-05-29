package com.zippy.infra.mybatis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis 扩展配置属性
 *
 * @author lichunqing
 */
@Data
@ConfigurationProperties(prefix = "zippy.mybatis")
public class MybatisProperties {

    /**
     * 是否启用 MyBatis 扩展配置（默认 true）
     */
    private boolean enabled = true;
}
