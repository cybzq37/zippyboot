package com.zippyboot.infra.storage.support;

import com.zippyboot.infra.storage.config.StorageConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class StoragePathHelper {

    private StoragePathHelper() {
    }

    public static String buildObjectKey(StorageConfig config, String originalFilename) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getDatePathPattern()));
        String storedName = buildStoredFilename(config, originalFilename);
        return normalizeObjectKey(datePath + "/" + storedName);
    }

    public static String buildStoredFilename(StorageConfig config, String originalFilename) {
        String extension = ext(originalFilename);
        String baseName;
        if ("origin".equalsIgnoreCase(config.getFilenameStrategy())) {
            baseName = safeBaseName(originalFilename);
        } else {
            baseName = UUID.randomUUID().toString().replace("-", "");
        }
        return extension.isEmpty() ? baseName : baseName + "." + extension;
    }

    public static String normalizeObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return "";
        }
        String normalized = objectKey.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public static String ext(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private static String safeBaseName(String filename) {
        String source = Objects.requireNonNullElse(filename, "file");
        String cleaned = source.replace('\\', '/');
        int slashIndex = cleaned.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < cleaned.length() - 1) {
            cleaned = cleaned.substring(slashIndex + 1);
        }
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex > 0) {
            cleaned = cleaned.substring(0, dotIndex);
        }
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9_-]", "_");
        return cleaned.isBlank() ? "file" : cleaned;
    }
}
