package com.zippyboot.kit.jackson.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.zippyboot.kit.enums.IEnum;

import java.io.IOException;
import java.util.Map;

/**
 * {@link IEnum} 通用反序列化器。
 * <p>
 * 支持两种 JSON 格式：
 * <ul>
 *   <li>对象格式：{@code {"code": 1, "desc": "男"}}</li>
 *   <li>code 值格式：{@code 1} 或 {@code "ACTIVE"}</li>
 * </ul>
 */
public class IEnumDeserializer extends JsonDeserializer<IEnum<?>> {

    @Override
    public IEnum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> targetType = ctxt.getContextualType().getRawClass();
        if (targetType == null || !IEnum.class.isAssignableFrom(targetType)) {
            throw JsonMappingException.from(p, "无法确定 IEnum 目标类型");
        }

        if (p.currentToken() == JsonToken.START_OBJECT) {
            Map<?, ?> map = p.readValueAs(Map.class);
            Object code = map.get("code");
            if (code == null) {
                throw JsonMappingException.from(p, "IEnum 对象缺少 code 字段");
            }
            return ofCode(targetType, code);
        }

        return ofCode(targetType, p.getValueAsString());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static IEnum<?> ofCode(Class<?> enumType, Object code) {
        return (IEnum<?>) IEnum.ofCode((Class) enumType, code);
    }
}
