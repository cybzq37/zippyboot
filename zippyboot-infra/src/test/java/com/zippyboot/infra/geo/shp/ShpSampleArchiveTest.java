package com.zippyboot.infra.geo.shp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShpSampleArchiveTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReadAndWriteRealShpDemoArchive() throws Exception {
        Path sampleZip = ShpTestSupport.resolveRepoPath("data", "shp_demo.zip");
        assertTrue(Files.exists(sampleZip), "sample zip should exist: " + sampleZip);

        ShpReadResult source = ShpReader.read(sampleZip.toString());
        assertFalse(source.features().isEmpty(), "sample shapefile should contain features");
        assertFalse(source.schema().fields().isEmpty(), "sample shapefile should contain fields");

        Path outputShp = tempDir.resolve("shp-demo-roundtrip.shp");
        ShpWriteResult writeResult = ShpWriter.write(outputShp.toString(), source.schema(), source.features());
        ShpReadResult roundTrip = ShpReader.read(outputShp.toString(), new ShpReadOptions(
                StandardCharsets.UTF_8,
                false,
                true,
                true,
                true,
                true,
                writeResult.schema().typeName()
        ));

        assertEquals(source.features().size(), roundTrip.features().size());
        assertEquals(source.schema().fields().size(), writeResult.schema().fields().size());

        for (int i = 0; i < source.features().size(); i++) {
            ShpFeatureData sourceFeature = source.features().get(i);
            ShpFeatureData roundTripFeature = roundTrip.features().get(i);
            assertNotNull(roundTripFeature.geometry());
            assertEquals(sourceFeature.geometry().wkt(), roundTripFeature.geometry().wkt());

            for (ShpFieldMeta field : source.schema().fields()) {
                String originalName = field.originalName();
                String sourceKey = field.normalizedName();
                String writtenKey = writeResult.schema().fieldNameMapping().get(originalName);
                assertNotNull(writtenKey, "written field mapping should exist for " + originalName);

                Object sourceValue = sourceFeature.attributes().get(sourceKey);
                Object roundTripValue = roundTripFeature.attributes().get(writtenKey);
                assertEquals(normalizeValue(sourceValue), normalizeValue(roundTripValue),
                        "value mismatch for field " + originalName);
            }
        }
    }

    private Object normalizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString()).stripTrailingZeros();
        }
        return value.toString();
    }
}
