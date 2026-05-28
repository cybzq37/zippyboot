package com.zippyboot.infra.mybatis.handler;

import com.zippyboot.kit.enums.IEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 枚举按值映射类型处理器。
 * <p>
 * 将数据库字段值与实现了 {@link IEnum} 接口的枚举相互映射，
 * 避免使用枚举序号（ordinal）存储。
 * <ul>
 *   <li>写入时：自动取 {@link IEnum#getCode()} 存入数据库</li>
 *   <li>读取时：遍历枚举常量，按 code 值匹配还原</li>
 * </ul>
 * 使用方式（在实体类字段上指定具体枚举类型）：
 * <pre>
 * &#64;TableField(typeHandler = EnumValueHandler.class)
 * private Status status;
 * </pre>
 * 或在 XML resultMap 中指定：
 * <pre>
 * &lt;result column="status" property="status"
 *         typeHandler="com.zippyboot.infra.mybatis.handler.EnumValueHandler.of(Status.class)"/&gt;
 * </pre>
 */
public class EnumValueHandler {

    /**
     * 创建指定枚举类型的类型安全处理器。
     */
    public static <E extends Enum<E> & IEnum<?>> BaseTypeHandler<E> of(Class<E> enumType) {
        return new TypedEnumValueHandler<>(enumType);
    }

    private static class TypedEnumValueHandler<E extends Enum<E> & IEnum<?>> extends BaseTypeHandler<E> {

        private final Class<E> enumType;

        TypedEnumValueHandler(Class<E> enumType) {
            this.enumType = enumType;
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
            return IEnum.ofCode(enumType, dbValue);
        }
    }
}
