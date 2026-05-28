package com.zippyboot.kit.jackson.plugins.xss;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 自定义xss校验注解实现
 *
 * @author lichunqing
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<[^>]+>)|(javascript:)|((?:on[a-z]+)\\s*=)"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !XSS_PATTERN.matcher(value).find();
    }

}
