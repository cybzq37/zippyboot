package com.zippyboot.infra.geo;

import java.util.Locale;

public enum GeometryType {
    POINT,
    LINESTRING,
    MULTILINESTRING,
    POLYGON;

    public static GeometryType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("geometryType must not be blank");
        }
        return GeometryType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
