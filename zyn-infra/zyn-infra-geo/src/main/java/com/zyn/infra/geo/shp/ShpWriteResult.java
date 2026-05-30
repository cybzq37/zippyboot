package com.zyn.infra.geo.shp;

public record ShpWriteResult(
        String shpPath,
        ShpSchema schema,
        int featureCount
) {
}
