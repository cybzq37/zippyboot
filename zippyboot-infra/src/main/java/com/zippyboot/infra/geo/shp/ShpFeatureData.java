package com.zippyboot.infra.geo.shp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ShpFeatureData(
        String id,
        Map<String, Object> attributes,
        ShpGeometryData geometry
) {
    public ShpFeatureData {
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }
}
