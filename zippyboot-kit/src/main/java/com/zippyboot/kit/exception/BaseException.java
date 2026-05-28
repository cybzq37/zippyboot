package com.zippyboot.kit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;

public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public static final String BAD_REQUEST_CODE = "KIT-400";
    public static final String INTERNAL_ERROR_CODE = "KIT-500";
    private static final String DEFAULT_BAD_REQUEST_MESSAGE = "Bad request";
    private static final String DEFAULT_INTERNAL_ERROR_MESSAGE = "Internal server error";

    private final String code;
    private final HttpStatusCode status;
    private final List<String> details;

    public BaseException(String message) {
        this(HttpStatus.BAD_REQUEST, BAD_REQUEST_CODE, message);
    }

    public BaseException(String code, String message) {
        this(HttpStatus.BAD_REQUEST, code, message);
    }

    public BaseException(String code, String message, Throwable cause) {
        this(HttpStatus.BAD_REQUEST, code, message, cause);
    }

    public BaseException(HttpStatusCode status, String code, String message) {
        this(status, code, message, null, List.of());
    }

    public BaseException(HttpStatusCode status, String code, String message, List<String> details) {
        this(status, code, message, null, details);
    }

    public BaseException(HttpStatusCode status, String code, String message, Throwable cause) {
        this(status, code, message, cause, List.of());
    }

    public BaseException(HttpStatusCode status, String code, String message, Throwable cause, List<String> details) {
        super(normalizeMessage(message, status), cause);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
        this.code = normalizeCode(code, this.status);
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public static BaseException badRequest(String message) {
        return new BaseException(message);
    }

    public static BaseException badRequest(String code, String message) {
        return new BaseException(code, message);
    }

    public static BaseException badRequest(String code, String message, List<String> details) {
        return new BaseException(HttpStatus.BAD_REQUEST, code, message, details);
    }

    public static BaseException internalError(String message, Throwable cause) {
        return new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_CODE, message, cause);
    }

    public static BaseException internalError(String code, String message, Throwable cause) {
        return new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, code, message, cause);
    }

    public static BaseException internalError(String code, String message, Throwable cause, List<String> details) {
        return new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, code, message, cause, details);
    }

    public String getCode() {
        return code;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public List<String> getDetails() {
        return details;
    }

    private static String normalizeCode(String code, HttpStatusCode status) {
        if (code != null && !code.isBlank()) {
            return code;
        }
        return status.is5xxServerError() ? INTERNAL_ERROR_CODE : BAD_REQUEST_CODE;
    }

    private static String normalizeMessage(String message, HttpStatusCode status) {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return status != null && status.is5xxServerError()
                ? DEFAULT_INTERNAL_ERROR_MESSAGE
                : DEFAULT_BAD_REQUEST_MESSAGE;
    }
}
