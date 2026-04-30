package com.zippyboot.infra.satoken.service;

import com.zippyboot.infra.satoken.config.SaTokenConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "zippyboot.infra.satoken", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SaTokenService {

    private final SaTokenConfig config;

    public String loginType() {
        return config.getLoginType();
    }

    public String tokenPrefix() {
        return config.getTokenPrefix();
    }
}
