package com.zippyboot.infra.geo.shp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ShpSchema(
        String typeName,
        List<ShpFieldMeta> fields
) {
    public ShpSchema {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    public Map<String, String> fieldNameMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (ShpFieldMeta field : fields) {
            mapping.put(field.originalName(), field.normalizedName());
        }
        return Collections.unmodifiableMap(mapping);
    }
}
