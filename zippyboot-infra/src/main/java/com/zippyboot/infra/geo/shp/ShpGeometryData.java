package com.zippyboot.infra.geo.shp;

import org.locationtech.jts.geom.Geometry;

public record ShpGeometryData(
        Geometry geometry,
        String wkt,
        String geoJson,
        String csv
) {
}
