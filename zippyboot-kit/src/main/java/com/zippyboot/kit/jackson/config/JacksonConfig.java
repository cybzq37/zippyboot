package com.zippyboot.kit.jackson.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zippyboot.kit.jackson.module.IEnumModule;
import com.zippyboot.kit.jackson.serializer.BigNumberSerializer;
import com.zippyboot.kit.jackson.serializer.SensitiveServiceHolder;
import com.zippyboot.kit.jackson.plugins.sensitive.SensitiveService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DEFAULT_DATE_TIME_FORMATTER));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DEFAULT_DATE_TIME_FORMATTER));
            javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

            builder.modules(javaTimeModule, new IEnumModule());
            builder.featuresToDisable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        };
    }

    @Bean
    @ConditionalOnMissingBean(SensitiveService.class)
    public SensitiveService sensitiveService() {
        return () -> true;
    }

    @Bean
    public SensitiveServiceHolder sensitiveServiceHolder(SensitiveService sensitiveService) {
        return new SensitiveServiceHolder(sensitiveService);
    }
}
