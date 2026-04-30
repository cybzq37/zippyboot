package com.zippyboot.infra.satoken.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.satoken")
public class SaTokenConfig {

    private boolean enabled = true;
    private String loginType = "login";
    private String tokenPrefix = "Bearer";
}
