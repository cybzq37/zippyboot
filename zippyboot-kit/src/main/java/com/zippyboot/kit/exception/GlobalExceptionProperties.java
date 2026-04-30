package com.zippyboot.kit.exception;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zippyboot.kit.exception")
public class GlobalExceptionProperties {

    private boolean enabled = true;
    private boolean includeStackTrace = false;
    private boolean logWarnForBusiness = false;
    private boolean wrapSuccessResponse = true;
    private String successCode = "KIT-200";
    private String successMessage = "OK";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
    }

    public boolean isLogWarnForBusiness() {
        return logWarnForBusiness;
    }

    public void setLogWarnForBusiness(boolean logWarnForBusiness) {
        this.logWarnForBusiness = logWarnForBusiness;
    }

    public boolean isWrapSuccessResponse() {
        return wrapSuccessResponse;
    }

    public void setWrapSuccessResponse(boolean wrapSuccessResponse) {
        this.wrapSuccessResponse = wrapSuccessResponse;
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
