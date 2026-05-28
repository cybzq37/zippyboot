package com.zippyboot.kit.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zippyboot.kit.exception.BaseException;
import com.zippyboot.kit.exception.GlobalExceptionProperties;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalResponseBodyAdviceTest {

    @Test
    void shouldRaiseBaseExceptionWhenStringWrappingSerializationFails() throws NoSuchMethodException {
        GlobalExceptionProperties properties = new GlobalExceptionProperties();
        GlobalResponseBodyAdvice advice = new GlobalResponseBodyAdvice(properties, failingObjectMapper());
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("text"), -1);

        assertThatThrownBy(() -> advice.beforeBodyWrite(
                "payload",
                returnType,
                MediaType.TEXT_PLAIN,
                StringHttpMessageConverter.class,
                new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/demo")),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        ))
                .isInstanceOf(BaseException.class)
                .satisfies(throwable -> {
                    BaseException exception = (BaseException) throwable;
                    assertThat(exception.getStatus().value()).isEqualTo(500);
                    assertThat(exception.getCode()).isEqualTo(BaseException.INTERNAL_ERROR_CODE);
                });
    }

    private static ObjectMapper failingObjectMapper() {
        return new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws JsonProcessingException {
                throw new JsonProcessingException("boom") {
                };
            }
        };
    }

    static class TestController {

        public String text() {
            return "ok";
        }
    }
}
