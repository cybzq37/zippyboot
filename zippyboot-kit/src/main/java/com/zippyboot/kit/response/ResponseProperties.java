package com.zippyboot.kit.response;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zippyboot.kit.response")
public class ResponseProperties {

    private boolean enabled = true;
    private String successCode = ApiResponse.DEFAULT_SUCCESS_CODE;
    private String successMessage = ApiResponse.DEFAULT_SUCCESS_MESSAGE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode == null || successCode.isBlank()
                ? ApiResponse.DEFAULT_SUCCESS_CODE
                : successCode;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage == null || successMessage.isBlank()
                ? ApiResponse.DEFAULT_SUCCESS_MESSAGE
                : successMessage;
    }
}
