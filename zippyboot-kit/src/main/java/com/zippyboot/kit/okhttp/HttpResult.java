package com.zippyboot.kit.okhttp;

import okhttp3.Headers;

public final class HttpResult {

    private final int statusCode;
    private final Headers headers;
    private final String body;
    private final String errorMessage;

    private HttpResult(int statusCode, Headers headers, String body, String errorMessage) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.errorMessage = errorMessage;
    }

    public static HttpResult success(int statusCode, Headers headers, String body) {
        return new HttpResult(statusCode, headers, body, null);
    }

    public static HttpResult failure(String errorMessage) {
        return new HttpResult(-1, null, null, errorMessage);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccessful() {
        return errorMessage == null && statusCode >= 200 && statusCode < 300;
    }
}
