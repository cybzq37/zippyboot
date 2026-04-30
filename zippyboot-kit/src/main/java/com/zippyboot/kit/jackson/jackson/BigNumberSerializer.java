package com.zippyboot.kit.jackson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 超出 JS 最大最小值 处理
 *
 * @author lichunqing
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {

    /**
     * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    /**
     * 提供实例
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (isSafeNumber(value)) {
            super.serialize(value, gen, provider);
        } else {
            gen.writeString(value.toString());
        }
    }

    private boolean isSafeNumber(Number value) {
        if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            BigInteger max = BigInteger.valueOf(MAX_SAFE_INTEGER);
            BigInteger min = BigInteger.valueOf(MIN_SAFE_INTEGER);
            return bigInteger.compareTo(min) >= 0 && bigInteger.compareTo(max) <= 0;
        }

        long val = value.longValue();
        return val >= MIN_SAFE_INTEGER && val <= MAX_SAFE_INTEGER;
    }
}
