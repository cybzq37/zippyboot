package com.zippyboot.kit.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "zippyboot.kit.exception")
public class GlobalExceptionProperties {

    private boolean enabled = true;
    private boolean includeStackTrace = false;
    private boolean logWarnForBusiness = false;
}
