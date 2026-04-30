package com.zippyboot.kit.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zippyboot.kit.exception.ErrorResponse;
import com.zippyboot.kit.exception.GlobalExceptionProperties;
import com.zippyboot.kit.exception.KitException;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestControllerAdvice
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final GlobalExceptionProperties properties;
    private final ObjectMapper objectMapper;

    public GlobalResponseBodyAdvice(GlobalExceptionProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!properties.isWrapSuccessResponse()) {
            return false;
        }
        if (returnType.hasMethodAnnotation(IgnoreResponseWrap.class)) {
            return false;
        }
        Class<?> declaringClass = returnType.getContainingClass();
        return !declaringClass.isAnnotationPresent(IgnoreResponseWrap.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) {
            return wrap(null, request);
        }
        if (body instanceof ApiResponse<?> || body instanceof ErrorResponse) {
            return body;
        }
        if (body instanceof ResponseEntity<?> || body instanceof Resource || body instanceof byte[] || body instanceof StreamingResponseBody) {
            return body;
        }

        Object wrapped = wrap(body, request);
        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(wrapped);
            } catch (JsonProcessingException e) {
                throw new KitException("KIT-500", "Serialize wrapped string response failed", e);
            }
        }
        return wrapped;
    }

    private ApiResponse<Object> wrap(Object body, ServerHttpRequest request) {
        return ApiResponse.success(
                properties.getSuccessCode(),
                properties.getSuccessMessage(),
                extractPath(request),
                body
        );
    }

    private String extractPath(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest().getRequestURI();
        }
        return request.getURI() == null ? "" : request.getURI().getPath();
    }
}
