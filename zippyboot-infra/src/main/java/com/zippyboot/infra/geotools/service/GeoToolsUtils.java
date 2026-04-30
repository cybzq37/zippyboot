package com.zippyboot.infra.geotools.service;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GeoToolsUtils {

    private static final String DEFAULT_TARGET_EPSG = "EPSG:3857";
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final GeometryJSON GEOMETRY_JSON = new GeometryJSON();
    private static final WKTReader WKT_READER = new WKTReader();
    private static final WKTWriter WKT_WRITER = new WKTWriter();

    private GeoToolsUtils() {
    }

    public static String toWkt(String epsgCode) throws Exception {
        CoordinateReferenceSystem crs = CRS.decode(epsgCode, true);
        return crs.toWKT();
    }

    public static String defaultTargetWkt() throws Exception {
        return toWkt(DEFAULT_TARGET_EPSG);
    }

    public static List<Map<String, Object>> readShpToList(String shpPath) throws IOException {
        File shpFile = new File(shpPath);
        if (!shpFile.exists()) {
            throw new IOException("Shapefile not found: " + shpPath);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("charset", StandardCharsets.UTF_8);

        DataStore dataStore = DataStoreFinder.getDataStore(params);
        if (dataStore == null) {
            throw new IOException("Unable to open shapefile datastore: " + shpPath);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String typeName = dataStore.getTypeNames()[0];
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
            SimpleFeatureCollection collection = (SimpleFeatureCollection) featureSource.getFeatures();

            try (SimpleFeatureIterator iterator = collection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", feature.getID());

                    for (var descriptor : feature.getFeatureType().getAttributeDescriptors()) {
                        String attrName = descriptor.getLocalName();
                        Object value = feature.getAttribute(attrName);
                        if (value instanceof Geometry geometry) {
                            row.put("geometry", geometry);
                            row.put("wkt", geometryToWkt(geometry));
                            row.put("geojson", geometryToGeoJson(geometry));
                            row.put("csv", geometryToCsv(geometry));
                        } else {
                            row.put(attrName, value);
                        }
                    }
                    result.add(row);
                }
            }
        } finally {
            dataStore.dispose();
        }
        return result;
    }

    public static String geometryToWkt(Geometry geometry) {
        return WKT_WRITER.write(geometry);
    }

    public static Geometry wktToGeometry(String wkt) throws Exception {
        return WKT_READER.read(wkt);
    }

    public static String geometryToGeoJson(Geometry geometry) throws IOException {
        StringWriter writer = new StringWriter();
        GEOMETRY_JSON.write(geometry, writer);
        return writer.toString();
    }

    public static Geometry geoJsonToGeometry(String geoJson) throws IOException {
        return GEOMETRY_JSON.read(new StringReader(geoJson));
    }

    public static String wktToGeoJson(String wkt) throws Exception {
        return geometryToGeoJson(wktToGeometry(wkt));
    }

    public static String geoJsonToWkt(String geoJson) throws Exception {
        return geometryToWkt(geoJsonToGeometry(geoJson));
    }

    public static String geometryToCsv(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        List<String> points = new ArrayList<>(coordinates.length);
        for (Coordinate coordinate : coordinates) {
            points.add(coordinate.getX() + "," + coordinate.getY());
        }
        return String.join(";", points);
    }

    public static Geometry csvToGeometry(String csv, String geometryType) {
        List<Coordinate> coordinates = parseCsvCoordinates(csv);
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("CSV coordinates cannot be empty");
        }

        String type = geometryType == null ? "POINT" : geometryType.toUpperCase(Locale.ROOT);
        return switch (type) {
            case "POINT" -> GEOMETRY_FACTORY.createPoint(coordinates.get(0));
            case "LINESTRING" -> GEOMETRY_FACTORY.createLineString(coordinates.toArray(new Coordinate[0]));
            case "POLYGON" -> createPolygon(coordinates);
            default -> throw new IllegalArgumentException("Unsupported geometry type: " + geometryType);
        };
    }

    public static String csvToWkt(String csv, String geometryType) {
        return geometryToWkt(csvToGeometry(csv, geometryType));
    }

    public static String csvToGeoJson(String csv, String geometryType) throws IOException {
        return geometryToGeoJson(csvToGeometry(csv, geometryType));
    }

    public static String wktToCsv(String wkt) throws Exception {
        return geometryToCsv(wktToGeometry(wkt));
    }

    public static String geoJsonToCsv(String geoJson) throws IOException {
        return geometryToCsv(geoJsonToGeometry(geoJson));
    }

    public static boolean intersects(Geometry source, Geometry target) {
        return source != null && target != null && source.intersects(target);
    }

    public static boolean intersectsByWkt(String sourceWkt, String targetWkt) throws Exception {
        return intersects(wktToGeometry(sourceWkt), wktToGeometry(targetWkt));
    }

    public static boolean pointIntersectsPoint(Geometry pointA, Geometry pointB) {
        requirePointGeometry(pointA, "pointA");
        requirePointGeometry(pointB, "pointB");
        return intersects(pointA, pointB);
    }

    public static boolean pointIntersectsLine(Geometry point, Geometry line) {
        requirePointGeometry(point, "point");
        requireLineGeometry(line, "line");
        return intersects(point, line);
    }

    public static boolean pointIntersectsPolygon(Geometry point, Geometry polygon) {
        requirePointGeometry(point, "point");
        requirePolygonGeometry(polygon, "polygon");
        return intersects(point, polygon);
    }

    public static boolean lineIntersectsLine(Geometry lineA, Geometry lineB) {
        requireLineGeometry(lineA, "lineA");
        requireLineGeometry(lineB, "lineB");
        return intersects(lineA, lineB);
    }

    public static boolean lineIntersectsPolygon(Geometry line, Geometry polygon) {
        requireLineGeometry(line, "line");
        requirePolygonGeometry(polygon, "polygon");
        return intersects(line, polygon);
    }

    public static boolean polygonIntersectsPolygon(Geometry polygonA, Geometry polygonB) {
        requirePolygonGeometry(polygonA, "polygonA");
        requirePolygonGeometry(polygonB, "polygonB");
        return intersects(polygonA, polygonB);
    }

    public static boolean pointIntersectsLineByWkt(String pointWkt, String lineWkt) throws Exception {
        return pointIntersectsLine(wktToGeometry(pointWkt), wktToGeometry(lineWkt));
    }

    public static boolean pointIntersectsPolygonByWkt(String pointWkt, String polygonWkt) throws Exception {
        return pointIntersectsPolygon(wktToGeometry(pointWkt), wktToGeometry(polygonWkt));
    }

    public static boolean lineIntersectsPolygonByWkt(String lineWkt, String polygonWkt) throws Exception {
        return lineIntersectsPolygon(wktToGeometry(lineWkt), wktToGeometry(polygonWkt));
    }

    private static List<Coordinate> parseCsvCoordinates(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(";"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(pair -> {
                    String[] values = pair.split(",");
                    if (values.length != 2) {
                        throw new IllegalArgumentException("Invalid coordinate: " + pair);
                    }
                    return new Coordinate(Double.parseDouble(values[0].trim()), Double.parseDouble(values[1].trim()));
                })
                .toList();
    }

    private static Geometry createPolygon(List<Coordinate> coordinates) {
        List<Coordinate> polygonCoords = new ArrayList<>(coordinates);
        if (!polygonCoords.get(0).equals2D(polygonCoords.get(polygonCoords.size() - 1))) {
            polygonCoords.add(new Coordinate(polygonCoords.get(0)));
        }
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(polygonCoords.toArray(new Coordinate[0]));
        return GEOMETRY_FACTORY.createPolygon(shell, null);
    }

    private static void requirePointGeometry(Geometry geometry, String paramName) {
        if (geometry == null || !("Point".equalsIgnoreCase(geometry.getGeometryType())
                || "MultiPoint".equalsIgnoreCase(geometry.getGeometryType()))) {
            throw new IllegalArgumentException(paramName + " must be Point or MultiPoint");
        }
    }

    private static void requireLineGeometry(Geometry geometry, String paramName) {
        if (geometry == null || !("LineString".equalsIgnoreCase(geometry.getGeometryType())
                || "MultiLineString".equalsIgnoreCase(geometry.getGeometryType()))) {
            throw new IllegalArgumentException(paramName + " must be LineString or MultiLineString");
        }
    }

    private static void requirePolygonGeometry(Geometry geometry, String paramName) {
        if (geometry == null || !("Polygon".equalsIgnoreCase(geometry.getGeometryType())
                || "MultiPolygon".equalsIgnoreCase(geometry.getGeometryType()))) {
            throw new IllegalArgumentException(paramName + " must be Polygon or MultiPolygon");
        }
    }
}
