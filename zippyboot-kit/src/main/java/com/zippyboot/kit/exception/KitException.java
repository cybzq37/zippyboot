package com.zippyboot.kit.exception;

public class KitException extends RuntimeException {

    private final String code;

    public KitException(String message) {
        this("KIT-500", message);
    }

    public KitException(String code, String message) {
        super(message);
        this.code = code;
    }

    public KitException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
