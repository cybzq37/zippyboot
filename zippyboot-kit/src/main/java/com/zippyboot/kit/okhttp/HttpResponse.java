package com.zippyboot.kit.okhttp;

import okhttp3.Headers;

public final class HttpResponse {

    private final int statusCode;
    private final Headers headers;
    private final String body;
    private final String errorMessage;

    private HttpResponse(int statusCode, Headers headers, String body, String errorMessage) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.errorMessage = errorMessage;
    }

    public static HttpResponse success(int statusCode, Headers headers, String body) {
        return new HttpResponse(statusCode, headers, body, null);
    }

    public static HttpResponse failure(String errorMessage) {
        return new HttpResponse(-1, null, null, errorMessage);
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

    @Override
    public String toString() {
        if (errorMessage != null) {
            return "HttpResponse{error=" + errorMessage + "}";
        }
        return "HttpResponse{status=" + statusCode + ", body=" + (body != null ? body.length() + " chars" : "null") + "}";
    }
}
