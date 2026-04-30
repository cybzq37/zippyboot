package com.zippyboot.kit.exception;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private String code;
    private String message;
    private Instant timestamp;
    private String path;
    private List<String> details;

    public static ErrorResponse of(String code, String message, String path, List<String> details) {
        ErrorResponse response = new ErrorResponse();
        response.code = code;
        response.message = message;
        response.path = path;
        response.details = details;
        response.timestamp = Instant.now();
        return response;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public List<String> getDetails() {
        return details;
    }
}
