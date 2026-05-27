package com.zippyboot.infra.geo.shp;

import com.zippyboot.infra.geo.GeoCrsUtils;
import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ShpWriter {

    private static final int DBF_FIELD_NAME_LIMIT = 10;
    private static final String[] SHAPEFILE_EXTENSIONS = {".shp", ".shx", ".dbf", ".prj", ".fix", ".qix", ".cpg"};

    private ShpWriter() {
    }

    public static ShpWriteResult write(String shpPath, ShpSchema schema, List<ShpFeatureData> features) throws Exception {
        return write(shpPath, schema, features, ShpWriteOptions.defaults());
    }

    public static ShpWriteResult write(
            String shpPath,
            ShpSchema schema,
            List<ShpFeatureData> features,
            ShpWriteOptions options
    ) throws Exception {
        if (schema == null) {
            throw new IllegalArgumentException("schema must not be null");
        }
        if (features == null) {
            throw new IllegalArgumentException("features must not be null");
        }

        ShpWriteOptions writeOptions = options == null ? ShpWriteOptions.defaults() : options;
        File shpFile = requireShpFile(shpPath);
        prepareTarget(shpFile, writeOptions.overwrite());

        String typeName = resolveTypeName(shpFile.toPath(), schema, writeOptions);
        List<ResolvedField> resolvedFields = resolveFields(schema, features);
        ShpGeometryResolver.ResolvedGeometry resolvedGeometry =
                ShpGeometryResolver.resolveDefinition(features, writeOptions.geometryType());
        Integer srid = ShpGeometryResolver.resolveSrid(features, writeOptions.srid());
        SimpleFeatureType featureType = buildFeatureType(typeName, resolvedFields, resolvedGeometry.binding(), writeOptions, srid);

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore dataStore = (ShapefileDataStore) factory.createNewDataStore(params);
        String actualTypeName;
        try {
            dataStore.setCharset(writeOptions.charset());
            dataStore.createSchema(featureType);
            actualTypeName = dataStore.getTypeNames()[0];
            if (srid != null) {
                dataStore.forceSchemaCRS(GeoCrsUtils.decode("EPSG:" + srid));
            }

            try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                         dataStore.getFeatureWriterAppend(dataStore.getTypeNames()[0], Transaction.AUTO_COMMIT)) {
                for (ShpFeatureData feature : features) {
                    SimpleFeature target = writer.next();
                    target.setAttribute(writeOptions.geometryFieldName(),
                            ShpGeometryResolver.resolve(feature.geometry(), resolvedGeometry.geometryType()));
                    for (ResolvedField field : resolvedFields) {
                        Object value = feature.attributes().get(field.sourceName());
                        target.setAttribute(field.writtenName(), coerceValue(value, field.valueType()));
                    }
                    writer.write();
                }
            }
        } finally {
            dataStore.dispose();
        }

        List<ShpFieldMeta> writtenFields = resolvedFields.stream()
                .map(field -> new ShpFieldMeta(field.originalName(), field.writtenName(), field.valueType()))
                .toList();
        return new ShpWriteResult(shpFile.getAbsolutePath(), new ShpSchema(actualTypeName, writtenFields), features.size());
    }

    private static File requireShpFile(String shpPath) {
        if (shpPath == null || shpPath.isBlank()) {
            throw new IllegalArgumentException("shpPath must not be blank");
        }
        if (!shpPath.toLowerCase(Locale.ROOT).endsWith(".shp")) {
            throw new IllegalArgumentException("shpPath must end with .shp");
        }
        return new File(shpPath);
    }

    private static void prepareTarget(File shpFile, boolean overwrite) throws IOException {
        File parent = shpFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
        }

        if (!shpFile.exists()) {
            return;
        }
        if (!overwrite) {
            throw new IOException("Shapefile already exists: " + shpFile.getAbsolutePath());
        }

        String basePath = stripExtension(shpFile.getAbsolutePath());
        for (String extension : SHAPEFILE_EXTENSIONS) {
            File candidate = new File(basePath + extension);
            if (candidate.exists() && !candidate.delete()) {
                throw new IOException("Failed to delete existing shapefile component: " + candidate.getAbsolutePath());
            }
        }
    }

    private static String resolveTypeName(Path shpPath, ShpSchema schema, ShpWriteOptions options) {
        if (options.typeName() != null && !options.typeName().isBlank()) {
            return options.typeName();
        }
        if (schema != null && schema.typeName() != null && !schema.typeName().isBlank()) {
            return schema.typeName();
        }
        String fileName = shpPath.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    private static List<ResolvedField> resolveFields(ShpSchema schema, List<ShpFeatureData> features) {
        List<ResolvedField> resolvedFields = new ArrayList<>();
        Set<String> usedFieldNames = new HashSet<>();
        for (ShpFieldMeta field : schema.fields()) {
            String writtenName = resolveDbfFieldName(field.normalizedName(), usedFieldNames);
            Class<?> valueType = resolveFieldType(field, features);
            resolvedFields.add(new ResolvedField(field.originalName(), field.normalizedName(), writtenName, valueType));
        }
        return resolvedFields;
    }

    private static Class<?> resolveFieldType(ShpFieldMeta field, List<ShpFeatureData> features) {
        Class<?> binding = field.valueType();
        if (binding == null || Object.class.equals(binding)) {
            for (ShpFeatureData feature : features) {
                Object value = feature.attributes().get(field.normalizedName());
                if (value != null) {
                    binding = value.getClass();
                    break;
                }
            }
        }
        return normalizeBinding(binding);
    }

    private static Class<?> normalizeBinding(Class<?> binding) {
        if (binding == null || Object.class.equals(binding)) {
            return String.class;
        }
        if (binding.isPrimitive()) {
            if (binding == int.class) {
                return Integer.class;
            }
            if (binding == long.class) {
                return Long.class;
            }
            if (binding == double.class) {
                return Double.class;
            }
            if (binding == float.class) {
                return Float.class;
            }
            if (binding == boolean.class) {
                return Boolean.class;
            }
            if (binding == short.class) {
                return Short.class;
            }
        }
        if (CharSequence.class.isAssignableFrom(binding) || binding.isEnum()) {
            return String.class;
        }
        if (Integer.class.isAssignableFrom(binding) || Short.class.isAssignableFrom(binding)
                || Byte.class.isAssignableFrom(binding)) {
            return Integer.class;
        }
        if (Long.class.isAssignableFrom(binding)) {
            return Long.class;
        }
        if (Float.class.isAssignableFrom(binding) || Double.class.isAssignableFrom(binding)) {
            return Double.class;
        }
        if (Boolean.class.isAssignableFrom(binding)) {
            return Boolean.class;
        }
        if (java.util.Date.class.isAssignableFrom(binding)) {
            return java.util.Date.class;
        }
        return String.class;
    }

    private static SimpleFeatureType buildFeatureType(
            String typeName,
            List<ResolvedField> fields,
            Class<? extends Geometry> geometryBinding,
            ShpWriteOptions options,
            Integer srid
    ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);
        if (srid != null) {
            builder.setSRS("EPSG:" + srid);
        }
        builder.add(options.geometryFieldName(), geometryBinding);
        for (ResolvedField field : fields) {
            builder.add(field.writtenName(), field.valueType());
        }
        return builder.buildFeatureType();
    }

    private static Object coerceValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (String.class.equals(targetType)) {
            return String.valueOf(value);
        }
        if (Integer.class.equals(targetType)) {
            return value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString());
        }
        if (Long.class.equals(targetType)) {
            return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
        }
        if (Double.class.equals(targetType)) {
            return value instanceof Number number ? number.doubleValue() : Double.parseDouble(value.toString());
        }
        if (Boolean.class.equals(targetType)) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
        }
        return value;
    }

    private static String resolveDbfFieldName(String sourceName, Set<String> usedFieldNames) {
        String sanitized = sanitizeFieldName(sourceName);
        String candidate = truncate(sanitized, DBF_FIELD_NAME_LIMIT);
        if (usedFieldNames.add(candidate)) {
            return candidate;
        }

        int suffix = 2;
        while (true) {
            String suffixText = String.valueOf(suffix);
            int prefixLength = Math.max(1, DBF_FIELD_NAME_LIMIT - suffixText.length());
            String resolved = truncate(sanitized, prefixLength) + suffixText;
            if (usedFieldNames.add(resolved)) {
                return resolved;
            }
            suffix++;
        }
    }

    private static String sanitizeFieldName(String sourceName) {
        if (sourceName == null || sourceName.isBlank()) {
            return "field";
        }

        StringBuilder builder = new StringBuilder(sourceName.length());
        for (int i = 0; i < sourceName.length(); i++) {
            char current = sourceName.charAt(i);
            if (Character.isLetterOrDigit(current) || current == '_') {
                builder.append(current);
            }
        }

        if (builder.isEmpty()) {
            builder.append("field");
        }
        if (Character.isDigit(builder.charAt(0))) {
            builder.insert(0, 'f');
        }
        return builder.toString();
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String stripExtension(String filePath) {
        int extensionIndex = filePath.lastIndexOf('.');
        return extensionIndex > 0 ? filePath.substring(0, extensionIndex) : filePath;
    }

    private record ResolvedField(
            String originalName,
            String sourceName,
            String writtenName,
            Class<?> valueType
    ) {
    }
}
