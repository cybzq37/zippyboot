package com.zippyboot.infra.geo.shp;

import com.zippyboot.infra.geo.GeoFormatUtils;
import com.zippyboot.infra.geo.GeometryType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

final class ShpGeometryResolver {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private ShpGeometryResolver() {
    }

    static ResolvedGeometry resolveDefinition(List<ShpFeatureData> features, GeometryType configuredType)
            throws Exception {
        if (configuredType != null) {
            return new ResolvedGeometry(configuredType, geometryBinding(configuredType));
        }

        for (ShpFeatureData feature : features) {
            Geometry geometry = resolve(feature.geometry(), null);
            if (geometry != null) {
                GeometryType geometryType = geometryTypeOf(geometry);
                return new ResolvedGeometry(geometryType, geometryBinding(geometryType));
            }
        }
        throw new IllegalArgumentException("geometryType must be provided when features do not contain geometry");
    }

    static Integer resolveSrid(List<ShpFeatureData> features, Integer configuredSrid) throws Exception {
        if (configuredSrid != null) {
            return configuredSrid;
        }
        for (ShpFeatureData feature : features) {
            Geometry geometry = resolve(feature.geometry(), null);
            if (geometry != null && geometry.getSRID() > 0) {
                return geometry.getSRID();
            }
        }
        return null;
    }

    static Geometry resolve(ShpGeometryData geometryData, GeometryType expectedType) throws Exception {
        if (geometryData == null) {
            return null;
        }

        Geometry geometry = geometryData.geometry();
        if (geometry == null && geometryData.wkt() != null && !geometryData.wkt().isBlank()) {
            geometry = GeoFormatUtils.wktToGeometry(geometryData.wkt());
        }
        if (geometry == null && geometryData.geoJson() != null && !geometryData.geoJson().isBlank()) {
            geometry = GeoFormatUtils.geoJsonToGeometry(geometryData.geoJson());
        }
        if (geometry == null && geometryData.csv() != null && !geometryData.csv().isBlank()) {
            if (expectedType == null) {
                throw new IllegalArgumentException("geometryType is required when geometry is provided only as csv");
            }
            geometry = GeoFormatUtils.csvToGeometry(geometryData.csv(), expectedType);
        }
        if (geometry == null) {
            return null;
        }
        if (expectedType == null) {
            return geometry;
        }
        return convertGeometryType(geometry, expectedType);
    }

    private static Geometry convertGeometryType(Geometry geometry, GeometryType expectedType) {
        return switch (expectedType) {
            case POINT -> {
                if (!(geometry instanceof Point)) {
                    throw new IllegalArgumentException("geometry must be Point");
                }
                yield (Geometry) geometry.copy();
            }
            case LINESTRING -> GeoFormatUtils.toLineString(geometry);
            case MULTILINESTRING -> {
                if (geometry instanceof MultiLineString multiLineString) {
                    yield (Geometry) multiLineString.copy();
                }
                if (geometry instanceof LineString lineString) {
                    yield GEOMETRY_FACTORY.createMultiLineString(new LineString[]{(LineString) lineString.copy()});
                }
                throw new IllegalArgumentException("geometry must be LineString or MultiLineString");
            }
            case POLYGON -> {
                if (!(geometry instanceof Polygon)) {
                    throw new IllegalArgumentException("geometry must be Polygon");
                }
                yield (Geometry) geometry.copy();
            }
        };
    }

    private static GeometryType geometryTypeOf(Geometry geometry) {
        if (geometry instanceof Point) {
            return GeometryType.POINT;
        }
        if (geometry instanceof LineString) {
            return GeometryType.LINESTRING;
        }
        if (geometry instanceof MultiLineString) {
            return GeometryType.MULTILINESTRING;
        }
        if (geometry instanceof Polygon) {
            return GeometryType.POLYGON;
        }
        throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
    }

    private static Class<? extends Geometry> geometryBinding(GeometryType geometryType) {
        return switch (geometryType) {
            case POINT -> Point.class;
            case LINESTRING -> LineString.class;
            case MULTILINESTRING -> MultiLineString.class;
            case POLYGON -> Polygon.class;
        };
    }

    record ResolvedGeometry(
            GeometryType geometryType,
            Class<? extends Geometry> binding
    ) {
    }
}
