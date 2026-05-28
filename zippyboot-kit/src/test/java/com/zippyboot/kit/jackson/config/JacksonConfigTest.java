package com.zippyboot.kit.jackson.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    private final ObjectMapper objectMapper = createObjectMapper();

    @Test
    void shouldSerializeUnsafeNumbersAsStrings() throws Exception {
        NumberPayload payload = new NumberPayload(
                9007199254740991L,
                9007199254740992L,
                new BigInteger("9007199254740992"),
                new BigDecimal("123.45"),
                LocalDateTime.of(2026, 5, 28, 13, 45, 30)
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsBytes(payload));

        assertThat(json.get("safeLong").isNumber()).isTrue();
        assertThat(json.get("unsafeLong").isTextual()).isTrue();
        assertThat(json.get("bigInteger").isTextual()).isTrue();
        assertThat(json.get("bigDecimal").isTextual()).isTrue();
        assertThat(json.get("createdAt").textValue()).isEqualTo("2026-05-28 13:45:30");
    }

    private static ObjectMapper createObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().customizer().customize(builder);
        return builder.build();
    }

    private record NumberPayload(
            Long safeLong,
            Long unsafeLong,
            BigInteger bigInteger,
            BigDecimal bigDecimal,
            LocalDateTime createdAt
    ) {
    }
}
