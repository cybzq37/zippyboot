package com.zippy.infra.geo.shp;

public record ShpWriteResult(
        String shpPath,
        ShpSchema schema,
        int featureCount
) {
}
