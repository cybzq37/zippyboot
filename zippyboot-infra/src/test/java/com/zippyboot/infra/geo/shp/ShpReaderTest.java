package com.zippyboot.infra.geo.shp;

import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShpReaderTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @TempDir
    Path tempDir;

    @Test
    void shouldReadShapefileIntoStructuredResult() throws Exception {
        Path shpPath = createPointShapefile();

        List<ShpFeatureData> features = ShpReader.read(shpPath.toString());

        assertEquals(1, features.size());
        assertEquals("sample", features.getFirst().typeName());
        assertEquals("alice", features.getFirst().attributes().get("name"));
        assertNotNull(features.getFirst().geometry());
        assertEquals("POINT (116.4 39.9)", features.getFirst().geometry().wkt());
    }

    @Test
    void shouldRespectReadOptions() throws Exception {
        Path shpPath = createPointShapefile();
        ShpReadOptions options = new ShpReadOptions(StandardCharsets.UTF_8, false, true, false, false, "sample");

        List<ShpFeatureData> features = ShpReader.read(shpPath.toString(), options);

        assertNull(features.getFirst().geometry().geometry());
        assertNotNull(features.getFirst().geometry().wkt());
        assertNull(features.getFirst().geometry().geoJson());
        assertNull(features.getFirst().geometry().csv());
    }

    private Path createPointShapefile() throws Exception {
        Path shpPath = tempDir.resolve("sample.shp");

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", shpPath.toUri().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore dataStore = (ShapefileDataStore) factory.createNewDataStore(params);
        try {
            SimpleFeatureType featureType = DataUtilities.createType("sample", "the_geom:Point:srid=4326,name:String");
            dataStore.createSchema(featureType);
            dataStore.setCharset(StandardCharsets.UTF_8);

            try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                         dataStore.getFeatureWriterAppend(dataStore.getTypeNames()[0], Transaction.AUTO_COMMIT)) {
                SimpleFeature feature = writer.next();
                feature.setAttribute("the_geom", GEOMETRY_FACTORY.createPoint(new Coordinate(116.4, 39.9)));
                feature.setAttribute("name", "alice");
                writer.write();
            }
        } finally {
            dataStore.dispose();
        }

        return shpPath;
    }
}
