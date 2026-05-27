package com.zippyboot.infra.storage.spi.impl;

import com.zippyboot.infra.storage.config.StorageConflictStrategy;
import com.zippyboot.infra.storage.model.BatchDeleteResult;
import com.zippyboot.infra.storage.model.BatchDeleteItemResult;
import com.zippyboot.infra.storage.config.StorageProperties;
import com.zippyboot.infra.storage.config.StorageType;
import com.zippyboot.infra.storage.exception.StorageBackendException;
import com.zippyboot.infra.storage.exception.StorageConflictException;
import com.zippyboot.infra.storage.exception.StorageObjectNotFoundException;
import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.StoredObject;
import com.zippyboot.infra.storage.model.StoredObjectMetadata;
import com.zippyboot.infra.storage.model.UploadedFileInfo;
import com.zippyboot.infra.storage.spi.StorageBackend;
import com.zippyboot.infra.storage.support.StorageObjectKeyGenerator;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

public class LocalStorageBackend implements StorageBackend {

    private final StorageProperties properties;
    private final Path rootPath;

    public LocalStorageBackend(StorageProperties properties) throws IOException {
        this.properties = properties;
        String configuredRootPath = properties.getLocal().getRootPath();
        if (!StringUtils.hasText(configuredRootPath)) {
            throw new IllegalStateException("zippyboot.infra.storage.local.root-path must not be blank");
        }
        this.rootPath = Path.of(configuredRootPath).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    @Override
    public UploadedFileInfo upload(String key,
                                   FileUploadRequest request,
                                   StorageConflictStrategy conflictStrategy) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        return switch (resolveConflictStrategy(conflictStrategy)) {
            case OVERWRITE -> writeOverwrite(normalizedKey, request);
            case FAIL -> writeFailIfExists(normalizedKey, request);
            case APPEND_SUFFIX -> writeAppendSuffix(normalizedKey, request);
        };
    }

    @Override
    public StoredObject open(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        Path targetPath = resolvePath(normalizedKey);
        StoredObjectMetadata metadata = getMetadata(normalizedKey);
        InputStream inputStream = Files.newInputStream(targetPath);
        return new StoredObject(normalizedKey, metadata.contentType(), metadata.size(), inputStream);
    }

    @Override
    public StoredObjectMetadata getMetadata(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        Path targetPath = resolvePath(normalizedKey);
        requireRegularFile(targetPath, normalizedKey);
        String contentType = Files.probeContentType(targetPath);
        long size = Files.size(targetPath);
        return new StoredObjectMetadata(normalizedKey, contentType, size, buildAccessUrl(normalizedKey));
    }

    @Override
    public boolean exists(String key) throws IOException {
        Path targetPath = resolvePath(StorageObjectKeyGenerator.requireValidKey(key, "key"));
        return Files.isRegularFile(targetPath);
    }

    @Override
    public void copy(String sourceKey, String targetKey) throws IOException {
        String normalizedSourceKey = StorageObjectKeyGenerator.requireValidKey(sourceKey, "sourceKey");
        String normalizedTargetKey = StorageObjectKeyGenerator.requireValidKey(targetKey, "targetKey");
        Path sourcePath = resolvePath(normalizedSourceKey);
        Path targetPath = resolvePath(normalizedTargetKey);
        requireRegularFile(sourcePath, normalizedSourceKey);
        rejectNonRegularExistingTarget(targetPath, normalizedTargetKey);
        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void move(String sourceKey, String targetKey) throws IOException {
        String normalizedSourceKey = StorageObjectKeyGenerator.requireValidKey(sourceKey, "sourceKey");
        String normalizedTargetKey = StorageObjectKeyGenerator.requireValidKey(targetKey, "targetKey");
        Path sourcePath = resolvePath(normalizedSourceKey);
        Path targetPath = resolvePath(normalizedTargetKey);
        requireRegularFile(sourcePath, normalizedSourceKey);
        rejectNonRegularExistingTarget(targetPath, normalizedTargetKey);
        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public String getAccessUrl(String key) {
        return buildAccessUrl(StorageObjectKeyGenerator.requireValidKey(key, "key"));
    }

    @Override
    public BatchDeleteResult deleteBatch(Collection<String> keys) {
        BatchDeleteResult.BatchDeleteResultBuilder builder = BatchDeleteResult.builder()
                .requestedCount(keys.size());
        int successCount = 0;
        int failureCount = 0;
        for (String key : keys) {
            String normalizedKey = StorageObjectKeyGenerator.normalizeKey(key);
            try {
                delete(normalizedKey);
                builder.item(BatchDeleteItemResult.builder()
                        .requestedKey(key)
                        .resolvedKey(normalizedKey)
                        .success(true)
                        .message(null)
                        .build());
                successCount++;
            } catch (IOException exception) {
                builder.item(BatchDeleteItemResult.builder()
                        .requestedKey(key)
                        .resolvedKey(normalizedKey)
                        .success(false)
                        .message(exception.getMessage())
                        .build());
                failureCount++;
            }
        }
        builder.successCount(successCount);
        builder.failureCount(failureCount);
        return builder.build();
    }

    @Override
    public void delete(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        Path targetPath = resolvePath(normalizedKey);
        if (Files.notExists(targetPath)) {
            return;
        }
        requireRegularFile(targetPath, normalizedKey);
        Files.deleteIfExists(targetPath);
    }

    private String buildAccessUrl(String key) {
        String encodedKey = StorageObjectKeyGenerator.encodeUrlPath(key);
        String publicBaseUrl = stripTrailingSlash(properties.getPublicBaseUrl());
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl + "/" + encodedKey;
        }

        String accessPathPrefix = properties.getLocal().getAccessPathPrefix();
        String normalizedPrefix = StringUtils.hasText(accessPathPrefix) ? accessPathPrefix.trim() : "/uploads";
        if (!normalizedPrefix.startsWith("/")) {
            normalizedPrefix = "/" + normalizedPrefix;
        }
        normalizedPrefix = stripTrailingSlash(normalizedPrefix);
        return normalizedPrefix + "/" + encodedKey;
    }

    private String stripTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private Path resolvePath(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.normalizeKey(key);
        Path targetPath = rootPath.resolve(normalizedKey).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new IOException("Invalid storage key: " + key);
        }
        return targetPath;
    }

    private void requireRegularFile(Path targetPath, String key) throws IOException {
        if (!Files.isRegularFile(targetPath)) {
            throw new StorageObjectNotFoundException("Storage object not found: " + key);
        }
    }

    private void rejectNonRegularExistingTarget(Path targetPath, String key) throws IOException {
        if (Files.exists(targetPath) && !Files.isRegularFile(targetPath)) {
            throw new StorageConflictException("Storage target is not a regular file: " + key);
        }
    }

    private UploadedFileInfo writeOverwrite(String normalizedKey, FileUploadRequest request) throws IOException {
        Path targetPath = resolvePath(normalizedKey);
        rejectNonRegularExistingTarget(targetPath, normalizedKey);
        createParentDirectories(targetPath);
        try (InputStream inputStream = request.openStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return buildUploadedFileInfo(normalizedKey, targetPath, request);
        } catch (IOException exception) {
            throw new StorageBackendException("Failed to upload object to local storage: " + normalizedKey, exception);
        }
    }

    private UploadedFileInfo writeFailIfExists(String normalizedKey, FileUploadRequest request) throws IOException {
        Path targetPath = resolvePath(normalizedKey);
        rejectNonRegularExistingTarget(targetPath, normalizedKey);
        createParentDirectories(targetPath);
        try (InputStream inputStream = request.openStream()) {
            Files.copy(inputStream, targetPath);
            return buildUploadedFileInfo(normalizedKey, targetPath, request);
        } catch (FileAlreadyExistsException exception) {
            throw new StorageConflictException("Storage object already exists: " + normalizedKey, exception);
        } catch (IOException exception) {
            throw new StorageBackendException("Failed to upload object to local storage: " + normalizedKey, exception);
        }
    }

    private UploadedFileInfo writeAppendSuffix(String normalizedKey, FileUploadRequest request) throws IOException {
        int suffix = 0;
        while (true) {
            String candidateKey = suffix == 0
                    ? normalizedKey
                    : StorageObjectKeyGenerator.appendNumericSuffix(normalizedKey, suffix);
            Path candidatePath = resolvePath(candidateKey);
            rejectNonRegularExistingTarget(candidatePath, candidateKey);
            createParentDirectories(candidatePath);
            try (InputStream inputStream = request.openStream()) {
                try (var outputStream = Files.newOutputStream(candidatePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    inputStream.transferTo(outputStream);
                }
                return buildUploadedFileInfo(candidateKey, candidatePath, request);
            } catch (FileAlreadyExistsException exception) {
                suffix++;
            } catch (IOException exception) {
                throw new StorageBackendException("Failed to upload object to local storage: " + candidateKey, exception);
            }
        }
    }

    private UploadedFileInfo buildUploadedFileInfo(String key, Path targetPath, FileUploadRequest request) {
        return UploadedFileInfo.builder()
                .storageType(StorageType.LOCAL)
                .bucket(null)
                .key(key)
                .originalFilename(request.getOriginalFilename())
                .storedFilename(targetPath.getFileName().toString())
                .contentType(request.getContentType())
                .size(request.getSize())
                .accessUrl(buildAccessUrl(key))
                .build();
    }

    private void createParentDirectories(Path targetPath) throws IOException {
        Path parent = targetPath.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new StorageBackendException("Failed to create local storage directories: " + parent, exception);
        }
    }

    private StorageConflictStrategy resolveConflictStrategy(StorageConflictStrategy conflictStrategy) {
        return conflictStrategy == null ? StorageConflictStrategy.APPEND_SUFFIX : conflictStrategy;
    }
}
