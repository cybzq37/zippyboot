package com.zippyboot.infra.mybatis.handler;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON/JSONB → {@link ObjectNode} 类型处理器。
 * <p>
 * 返回可变的 {@link ObjectNode}，支持链式读取和直接修改，
 * 适用于无需映射为具体 Java 对象、以树模型读写 JSON 的场景。
 * 写入 PostgreSQL 时使用 {@code jsonb} 类型。
 * <p>
 * 通过 {@link #setObjectMapper(ObjectMapper)} 可注入 Spring 容器中的 ObjectMapper，
 * 由 {@link com.zippyboot.infra.mybatis.config.MybatisAutoConfiguration} 自动完成。
 */
@MappedTypes({ObjectNode.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ObjectNodeHandler extends BaseTypeHandler<ObjectNode> {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ObjectNode parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
            ps.setObject(i, pgObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize ObjectNode", e);
        }
    }

    @Override
    public ObjectNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toObjectNode(rs.getString(columnName));
    }

    @Override
    public ObjectNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObjectNode(rs.getString(columnIndex));
    }

    @Override
    public ObjectNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObjectNode(cs.getString(columnIndex));
    }

    private ObjectNode toObjectNode(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            if (node.isObject()) {
                return (ObjectNode) node;
            }
            throw new SQLException("Expected JSON object, but got: " + node.getNodeType());
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON: " + json, e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper should not be null");
        OBJECT_MAPPER = objectMapper;
    }
}
