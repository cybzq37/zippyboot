package com.zippy.infra.web.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "zippy.global.web.exception")
public class GlobalExceptionProperties {

    private boolean enabled = true;
    private boolean includeStackTrace = false;
    private boolean logWarnForBusiness = false;
}
