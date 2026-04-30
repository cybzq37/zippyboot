package com.zippyboot.kit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final GlobalExceptionProperties properties;

    public GlobalExceptionHandler(GlobalExceptionProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler(KitException.class)
    public ResponseEntity<ErrorResponse> handleKitException(KitException ex, WebRequest request) {
        if (properties.isLogWarnForBusiness()) {
            log.warn("Business exception, code={}, msg={}", ex.getCode(), ex.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request, Collections.emptyList());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "KIT-400", ex.getMessage(), request, Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        FieldError fieldError = (FieldError) error;
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "KIT-400", "Validation failed", request, details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        FieldError fieldError = (FieldError) error;
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "KIT-400", "Bind failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "KIT-400", "Constraint violation", request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (properties.isIncludeStackTrace()) {
            log.error("Unhandled exception", ex);
        } else {
            log.error("Unhandled exception: {}", ex.getMessage());
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "KIT-500", "Internal server error", request, Collections.emptyList());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, WebRequest request, List<String> details) {
        String path = extractPath(request);
        ErrorResponse body = ErrorResponse.of(code, message, path, details);
        return ResponseEntity.status(status).body(body);
    }

    private String extractPath(WebRequest request) {
        String desc = request.getDescription(false);
        if (desc == null) {
            return "";
        }
        return desc.startsWith("uri=") ? desc.substring(4) : desc;
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
