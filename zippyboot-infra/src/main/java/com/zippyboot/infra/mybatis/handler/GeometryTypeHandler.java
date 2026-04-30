package com.navinfo.common.core.mybatis.handler;

import com.navinfo.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@MappedTypes(value = {String.class})
public class GeometryTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        PGgeometry pGgeometry = new PGgeometry(parameter);
        ps.setObject(i, pGgeometry);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (StringUtils.isEmpty(rs.getString(columnName))){
            return null;
        }
        PGgeometry pGgeometry = new PGgeometry(rs.getString(columnName));
        if (pGgeometry == null) {
            return null;
        }
        return pGgeometry.toString();
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (StringUtils.isEmpty(rs.getString(columnIndex))){
            return null;
        }
        PGgeometry pGgeometry = new PGgeometry(rs.getString(columnIndex));
        if (pGgeometry == null) {
            return null;
        }
        return pGgeometry.toString();
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (StringUtils.isEmpty(cs.getString(columnIndex))){
            return null;
        }
        PGgeometry pGgeometry = new PGgeometry(cs.getString(columnIndex));
        if (pGgeometry == null) {
            return null;
        }
        return pGgeometry.toString();
    }
}
