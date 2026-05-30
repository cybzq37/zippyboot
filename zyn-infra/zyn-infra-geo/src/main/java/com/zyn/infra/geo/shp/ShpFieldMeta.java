package com.zyn.infra.geo.shp;

public record ShpFieldMeta(
        String originalName,
        String normalizedName,
        Class<?> valueType
) {
    public ShpFieldMeta {
        valueType = valueType == null ? Object.class : valueType;
    }
}
