package com.zippyboot.infra.geo.shp;

import java.util.Map;

public record ShpFeatureData(
        String id,
        String typeName,
        Map<String, Object> attributes,
        ShpGeometryData geometry
) {
}
