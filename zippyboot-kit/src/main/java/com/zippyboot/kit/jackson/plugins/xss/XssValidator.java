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

    public final static String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(RE_HTML_MARK);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !HTML_TAG_PATTERN.matcher(value).find();
    }

}
