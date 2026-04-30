package com.zippyboot.kit.jackson.plugins.sensitive;

import java.util.function.Function;

/**
 * 脱敏策略
 *
 * @author Yjoioooo
 */
public enum SensitiveStrategy {

    /**
     * 身份证脱敏
     */
    ID_CARD(s -> maskKeep(s, 3, 4)),

    /**
     * 手机号脱敏
     */
    PHONE(s -> maskKeep(s, 3, 4)),

    /**
     * 地址脱敏
     */
    ADDRESS(s -> maskKeep(s, 0, 8)),

    /**
     * 邮箱脱敏
     */
    EMAIL(SensitiveStrategy::maskEmail),

    /**
     * 银行卡
     */
    BANK_CARD(s -> maskKeep(s, 6, 4));

    //可自行添加其他脱敏策略

    private final Function<String, String> desensitizer;

    SensitiveStrategy(Function<String, String> desensitizer) {
        this.desensitizer = desensitizer;
    }

    public Function<String, String> desensitizer() {
        return desensitizer;
    }

    private static String maskEmail(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        int atIndex = value.indexOf('@');
        if (atIndex <= 1) {
            return maskKeep(value, 1, 0);
        }

        String prefix = value.substring(0, atIndex);
        String domain = value.substring(atIndex);
        return maskKeep(prefix, 1, 0) + domain;
    }

    private static String maskKeep(String value, int keepStart, int keepEnd) {
        if (value == null || value.isBlank()) {
            return value;
        }

        int length = value.length();
        int safeKeepStart = Math.max(0, Math.min(keepStart, length));
        int safeKeepEnd = Math.max(0, Math.min(keepEnd, length - safeKeepStart));
        int maskLen = length - safeKeepStart - safeKeepEnd;
        if (maskLen <= 0) {
            return value;
        }

        StringBuilder sb = new StringBuilder(length);
        sb.append(value, 0, safeKeepStart);
        sb.append("*".repeat(maskLen));
        sb.append(value, length - safeKeepEnd, length);
        return sb.toString();
    }
}
