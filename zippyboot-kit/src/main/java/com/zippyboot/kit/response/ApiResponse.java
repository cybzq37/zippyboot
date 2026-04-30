package com.zippyboot.kit.response;

import java.time.Instant;

public class ApiResponse<T> {

    private String code;
    private String message;
    private Instant timestamp;
    private String path;
    private T data;

    public static <T> ApiResponse<T> success(String code, String message, String path, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        response.path = path;
        response.data = data;
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

    public T getData() {
        return data;
    }
}
