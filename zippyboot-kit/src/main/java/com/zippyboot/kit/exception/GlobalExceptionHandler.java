package com.zippyboot.kit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final GlobalExceptionProperties properties;

    public GlobalExceptionHandler(GlobalExceptionProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("Application exception, code={}, msg={}", ex.getCode(), ex.getMessage(), ex);
        } else if (properties.isLogWarnForBusiness()) {
            log.warn("Business exception, code={}, msg={}", ex.getCode(), ex.getMessage());
        }
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, ex.getDetails());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, BaseException.BAD_REQUEST_CODE, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, BaseException.BAD_REQUEST_CODE, "Validation failed", request, collectErrorDetails(ex.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, BaseException.BAD_REQUEST_CODE, "Bind failed", request, collectErrorDetails(ex.getBindingResult()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, WebRequest request) {
        List<String> details = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, BaseException.BAD_REQUEST_CODE, "Validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .toList();
        return build(HttpStatus.BAD_REQUEST, BaseException.BAD_REQUEST_CODE, "Constraint violation", request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (properties.isIncludeStackTrace()) {
            log.error("Unhandled exception", ex);
        } else {
            log.error("Unhandled exception: {}", ex.toString());
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, BaseException.INTERNAL_ERROR_CODE, "Internal server error", request, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatusCode status, String code, String message, WebRequest request, List<String> details) {
        String path = extractPath(request);
        ErrorResponse body = ErrorResponse.of(code, message, path, details);
        return ResponseEntity.status(status).body(body);
    }

    private String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        String desc = request.getDescription(false);
        if (desc == null) {
            return "";
        }
        return desc.startsWith("uri=") ? desc.substring(4) : desc;
    }

    private List<String> collectErrorDetails(Errors errors) {
        return errors.getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
