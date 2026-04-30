package com.zippyboot.kit.jackson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.zippyboot.kit.jackson.config.SpringBeanHolder;
import com.zippyboot.kit.jackson.plugins.sensitive.Sensitive;
import com.zippyboot.kit.jackson.plugins.sensitive.SensitiveService;
import com.zippyboot.kit.jackson.plugins.sensitive.SensitiveStrategy;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏json序列化工具
 *
 * @author Yjoioooo
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final SensitiveStrategy strategy;

    public SensitiveJsonSerializer() {
        this(null);
    }

    public SensitiveJsonSerializer(SensitiveStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        SensitiveService sensitiveService = SpringBeanHolder.getBean(SensitiveService.class);
        if (sensitiveService == null || !sensitiveService.isSensitive() || strategy == null || value == null) {
            gen.writeString(value);
            return;
        }

        gen.writeString(strategy.desensitizer().apply(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return prov.findNullValueSerializer(null);
        }

        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            return new SensitiveJsonSerializer(annotation.strategy());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
