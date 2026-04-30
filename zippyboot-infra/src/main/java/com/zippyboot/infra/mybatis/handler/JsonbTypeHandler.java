package com.zippyboot.infra.mybatis.handler;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Jackson 实现 JSONB 字段类型处理器
 */
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonbTypeHandler<T> extends BaseTypeHandler<T> {


    protected final Log log = LogFactory.getLog(this.getClass());

    protected final Class<?> type;

    /**
     * @since 3.5.6
     */
    protected Type genericType;

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public JsonbTypeHandler(Class<?> clazz) {
        this.type = clazz;
        if (log.isTraceEnabled()) {
            log.trace(this.getClass().getSimpleName() + "(" + type + ")");
        }
        Assert.notNull(type, "Type argument cannot be null");
    }

    // 自3.5.6版本开始支持泛型,需要加上此构造.
    public JsonbTypeHandler(Class<?> type, Field field) {
        this(type);
        this.genericType = field.getGenericType();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        PGobject jsonbObject = new PGobject();
        jsonbObject.setType("jsonb");
        jsonbObject.setValue(toJson(parameter));
        ps.setObject(i, jsonbObject);
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return isBlank(json) ? null : parse(json);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return isBlank(json) ? null : parse(json);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return isBlank(json) ? null : parse(json);
    }

    public Type getFieldType() {
        return this.genericType != null ? this.genericType : this.type;
    }

    public T parse(String json) {
        ObjectMapper objectMapper = getObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType javaType = typeFactory.constructType(getFieldType());
        try {
            return objectMapper.readValue(json, javaType);
        } catch (JacksonException e) {
            log.error("deserialize json: " + json + " to " + javaType + " error ", e);
            throw new RuntimeException(e);
        }
    }


    public String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("serialize " + obj + " to json error ", e);
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper should not be null");
        OBJECT_MAPPER = objectMapper;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
