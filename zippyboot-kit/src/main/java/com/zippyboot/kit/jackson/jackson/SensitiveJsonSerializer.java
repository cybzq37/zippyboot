package com.zippyboot.kit.jackson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.zippyboot.kit.jackson.plugins.sensitive.Sensitive;
import com.zippyboot.kit.jackson.plugins.sensitive.SensitiveService;
import com.zippyboot.kit.jackson.plugins.sensitive.SensitiveStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * 数据脱敏json序列化工具
 *
 * @author Yjoioooo
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final SensitiveStrategy strategy;
    private final SensitiveService sensitiveService;

    public SensitiveJsonSerializer() {
        this(null, null);
    }

    private SensitiveJsonSerializer(SensitiveStrategy strategy, SensitiveService sensitiveService) {
        this.strategy = strategy;
        this.sensitiveService = sensitiveService;
    }

    @Autowired(required = false)
    public SensitiveJsonSerializer(SensitiveService sensitiveService) {
        this(null, sensitiveService);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (strategy == null || (sensitiveService != null && !sensitiveService.isSensitive())) {
            gen.writeString(value);
            return;
        }

        gen.writeString(strategy.desensitizer().apply(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }

        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(Sensitive.class);
        }
        if (annotation != null && String.class.equals(property.getType().getRawClass())) {
            return new SensitiveJsonSerializer(annotation.strategy(), sensitiveService);
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
