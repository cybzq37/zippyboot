package com.zippyboot.infra.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举按值映射类型处理器。
 * <p>
 * 将数据库字段值与实现了 {@link CodeEnum} 接口的枚举相互映射，
 * 避免使用枚举序号（ordinal）存储。
 * <p>
 * 写入时取 {@link CodeEnum#getCode()}，读取时遍历枚举常量匹配 code 值。
 * 未匹配到时返回 {@code null}。
 */
@MappedTypes({CodeEnum.class})
public class EnumValueHandler extends BaseTypeHandler<CodeEnum<?>> {

    private static final Map<Class<? extends CodeEnum>, Method> CODE_METHOD_CACHE = new ConcurrentHashMap<>();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CodeEnum<?> parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getCode());
    }

    @Override
    public CodeEnum<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return value == null ? null : resolveEnum(value);
    }

    @Override
    public CodeEnum<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return value == null ? null : resolveEnum(value);
    }

    @Override
    public CodeEnum<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return value == null ? null : resolveEnum(value);
    }

    private CodeEnum<?> resolveEnum(Object dbValue) {
        // 此处理器为通用注册，实际使用时需通过 MyBatis 的 typeHandler 指定枚举类型
        // 或使用 #of(Class) 工厂方法创建类型安全的处理器
        throw new UnsupportedOperationException(
                "Use EnumValueHandler.of(YourEnum.class) to create a typed handler");
    }

    /**
     * 创建指定枚举类型的类型安全处理器。
     * <pre>
     * // 在 resultMap 中使用
     * &lt;result column="status" property="status"
     *         typeHandler="com.zippyboot.infra.mybatis.handler.EnumValueHandler.of(Status.class)"/&gt;
     * </pre>
     */
    public static <E extends Enum<E> & CodeEnum<?>> BaseTypeHandler<E> of(Class<E> enumType) {
        return new TypedEnumValueHandler<>(enumType);
    }

    private static class TypedEnumValueHandler<E extends Enum<E> & CodeEnum<?>> extends BaseTypeHandler<E> {

        private final Class<E> enumType;
        private final E[] enumConstants;

        TypedEnumValueHandler(Class<E> enumType) {
            this.enumType = enumType;
            this.enumConstants = enumType.getEnumConstants();
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
            ps.setObject(i, parameter.getCode());
        }

        @Override
        public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
            Object value = rs.getObject(columnName);
            return value == null ? null : matchEnum(value);
        }

        @Override
        public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            Object value = rs.getObject(columnIndex);
            return value == null ? null : matchEnum(value);
        }

        @Override
        public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            Object value = cs.getObject(columnIndex);
            return value == null ? null : matchEnum(value);
        }

        private E matchEnum(Object dbValue) {
            String dbStr = String.valueOf(dbValue);
            for (E constant : enumConstants) {
                Object code = constant.getCode();
                if (code != null && String.valueOf(code).equals(dbStr)) {
                    return constant;
                }
            }
            return null;
        }
    }
}
