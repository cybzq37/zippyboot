package com.zippyboot.infra.satoken.service;

import com.zippyboot.infra.satoken.config.SaTokenConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaTokenService {

    private final SaTokenConfig config;

    public String loginType() {
        return config.getLoginType();
    }

    public String tokenPrefix() {
        return config.getTokenPrefix();
    }
}
