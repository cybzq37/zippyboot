package com.zippyboot.infra.geo.shp;

import com.zippyboot.infra.geo.GeoFormatUtils;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShpReader {

    private ShpReader() {
    }

    public static List<ShpFeatureData> read(String shpPath) throws IOException {
        return read(shpPath, ShpReadOptions.defaults());
    }

    public static List<ShpFeatureData> read(String shpPath, ShpReadOptions options) throws IOException {
        File shpFile = new File(shpPath);
        if (!shpFile.exists()) {
            throw new IOException("Shapefile not found: " + shpPath);
        }

        ShpReadOptions readOptions = options == null ? ShpReadOptions.defaults() : options;
        Map<String, Object> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("charset", readOptions.charset());

        DataStore dataStore = DataStoreFinder.getDataStore(params);
        if (dataStore == null) {
            throw new IOException("Unable to open shapefile datastore: " + shpPath);
        }

        List<ShpFeatureData> result = new ArrayList<>();
        try {
            String typeName = resolveTypeName(dataStore, readOptions.typeName());
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
            SimpleFeatureCollection collection = (SimpleFeatureCollection) featureSource.getFeatures();

            try (SimpleFeatureIterator iterator = collection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Map<String, Object> attributes = new LinkedHashMap<>();
                    ShpGeometryData geometryData = null;

                    for (var descriptor : feature.getFeatureType().getAttributeDescriptors()) {
                        String attrName = descriptor.getLocalName();
                        Object value = feature.getAttribute(attrName);
                        if (value instanceof Geometry geometry) {
                            geometryData = buildGeometryData(geometry, readOptions);
                        } else {
                            attributes.put(attrName, value);
                        }
                    }
                    result.add(new ShpFeatureData(
                            feature.getID(),
                            typeName,
                            Collections.unmodifiableMap(new LinkedHashMap<>(attributes)),
                            geometryData
                    ));
                }
            }
        } finally {
            dataStore.dispose();
        }
        return result;
    }

    private static ShpGeometryData buildGeometryData(Geometry geometry, ShpReadOptions options) throws IOException {
        Geometry rawGeometry = options.includeGeometry() ? geometry : null;
        String wkt = options.includeWkt() ? GeoFormatUtils.geometryToWkt(geometry) : null;
        String geoJson = options.includeGeoJson() ? GeoFormatUtils.geometryToGeoJson(geometry) : null;
        String csv = options.includeCsv() ? GeoFormatUtils.geometryToCsv(geometry) : null;
        return new ShpGeometryData(rawGeometry, wkt, geoJson, csv);
    }

    private static String resolveTypeName(DataStore dataStore, String requestedTypeName) throws IOException {
        if (requestedTypeName == null || requestedTypeName.isBlank()) {
            return dataStore.getTypeNames()[0];
        }

        for (String typeName : dataStore.getTypeNames()) {
            if (typeName.equals(requestedTypeName)) {
                return typeName;
            }
        }
        throw new IOException("Shapefile type name not found: " + requestedTypeName);
    }
}
