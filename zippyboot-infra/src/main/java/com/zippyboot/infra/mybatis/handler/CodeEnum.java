package com.zippyboot.infra.mybatis.handler;

/**
 * 枚举值映射接口。
 * <p>
 * 实现此接口的枚举可由 {@link EnumValueHandler} 按 {@link #getCode()} 值
 * 与数据库字段相互映射，而非使用枚举序号。
 * <pre>
 * public enum Status implements CodeEnum&lt;Integer&gt; {
 *     DISABLED(0), ENABLED(1);
 *     private final Integer code;
 *     Status(Integer code) { this.code = code; }
 *     public Integer getCode() { return code; }
 * }
 * </pre>
 *
 * @param <T> 数据库字段类型（通常为 String 或 Integer）
 */
public interface CodeEnum<T> {

    T getCode();
}
