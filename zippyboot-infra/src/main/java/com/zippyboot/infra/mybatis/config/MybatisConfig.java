package com.zippyboot.infra.mybatis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.mybatis")
public class MybatisConfig {

    private boolean enabled = true;
    private boolean mapUnderscoreToCamelCase = true;
    private boolean printSql = true;
}
