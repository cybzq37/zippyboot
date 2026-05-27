package com.zippyboot.infra.storage.service.impl;

import com.zippyboot.infra.storage.config.StorageConflictStrategy;
import com.zippyboot.infra.storage.model.BatchDeleteItemResult;
import com.zippyboot.infra.storage.model.BatchDeleteResult;
import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.StoredObject;
import com.zippyboot.infra.storage.model.StoredObjectMetadata;
import com.zippyboot.infra.storage.model.UploadedFileInfo;
import com.zippyboot.infra.storage.service.StorageService;
import com.zippyboot.infra.storage.spi.StorageBackend;
import com.zippyboot.infra.storage.support.StorageObjectKeyGenerator;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DefaultStorageService implements StorageService {

    private final StorageBackend backend;
    private final StorageObjectKeyGenerator keyGenerator;
    private final StorageConflictStrategy conflictStrategy;
    private final long maxInMemoryReadBytes;

    public DefaultStorageService(StorageBackend backend,
                                 StorageObjectKeyGenerator keyGenerator,
                                 StorageConflictStrategy conflictStrategy,
                                 long maxInMemoryReadBytes) {
        this.backend = backend;
        this.keyGenerator = keyGenerator;
        this.conflictStrategy = conflictStrategy == null ? StorageConflictStrategy.APPEND_SUFFIX : conflictStrategy;
        this.maxInMemoryReadBytes = maxInMemoryReadBytes;
    }

    @Override
    public UploadedFileInfo upload(FileUploadRequest request) throws IOException {
        Assert.notNull(request, "request must not be null");
        request.validate();
        return uploadGeneratedKey(keyGenerator.generate(request.getOriginalFilename()), request);
    }

    @Override
    public UploadedFileInfo upload(String key, FileUploadRequest request) throws IOException {
        Assert.notNull(request, "request must not be null");
        request.validate();
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        return backend.upload(normalizedKey, request, conflictStrategy);
    }

    @Override
    public UploadedFileInfo uploadToDir(String dir, FileUploadRequest request) throws IOException {
        Assert.notNull(request, "request must not be null");
        request.validate();
        String normalizedDir = StorageObjectKeyGenerator.requireValidPrefix(dir, "dir");
        return uploadGeneratedKey(keyGenerator.generateUnderPrefix(normalizedDir, request.getOriginalFilename()), request);
    }

    @Override
    public StoredObject open(String key) throws IOException {
        return backend.open(StorageObjectKeyGenerator.requireValidKey(key, "key"));
    }

    @Override
    public StoredObjectMetadata getMetadata(String key) throws IOException {
        return backend.getMetadata(StorageObjectKeyGenerator.requireValidKey(key, "key"));
    }

    @Override
    public byte[] readBytes(String key) throws IOException {
        StoredObjectMetadata metadata = getMetadata(key);
        if (metadata.size() > maxInMemoryReadBytes) {
            throw new IOException("Stored object is too large to read into memory: " + metadata.key());
        }
        try (StoredObject storedObject = open(key)) {
            return readBytesWithLimit(storedObject);
        }
    }

    @Override
    public long transferTo(String key, OutputStream outputStream) throws IOException {
        Assert.notNull(outputStream, "outputStream must not be null");
        try (StoredObject storedObject = open(key)) {
            return storedObject.inputStream().transferTo(outputStream);
        }
    }

    @Override
    public Path downloadTo(String key, Path targetPath) throws IOException {
        Assert.notNull(targetPath, "targetPath must not be null");
        Path absoluteTargetPath = targetPath.toAbsolutePath().normalize();
        Path parent = absoluteTargetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream outputStream = Files.newOutputStream(absoluteTargetPath)) {
            transferTo(key, outputStream);
        }
        return absoluteTargetPath;
    }

    @Override
    public boolean exists(String key) throws IOException {
        return backend.exists(StorageObjectKeyGenerator.requireValidKey(key, "key"));
    }

    @Override
    public String getAccessUrl(String key) {
        return backend.getAccessUrl(StorageObjectKeyGenerator.requireValidKey(key, "key"));
    }

    @Override
    public void delete(String key) throws IOException {
        backend.delete(StorageObjectKeyGenerator.requireValidKey(key, "key"));
    }

    @Override
    public BatchDeleteResult deleteBatch(Collection<String> keys) {
        Assert.notNull(keys, "keys must not be null");
        List<String> requestedKeys = new ArrayList<>(keys);
        List<String> normalizedKeys = new ArrayList<>(requestedKeys.size());
        List<String> validationErrors = new ArrayList<>(requestedKeys.size());
        for (String key : requestedKeys) {
            try {
                normalizedKeys.add(StorageObjectKeyGenerator.requireValidKey(key, "key"));
                validationErrors.add(null);
            } catch (IllegalArgumentException exception) {
                validationErrors.add(exception.getMessage());
            }
        }

        List<BatchDeleteItemResult> backendItems = List.of();
        if (!normalizedKeys.isEmpty()) {
            BatchDeleteResult backendResult = backend.deleteBatch(normalizedKeys);
            backendItems = backendResult.getItems() == null ? List.of() : backendResult.getItems();
        }

        BatchDeleteResult.BatchDeleteResultBuilder builder = BatchDeleteResult.builder()
                .requestedCount(requestedKeys.size());
        Iterator<BatchDeleteItemResult> backendIterator = backendItems.iterator();
        int successCount = 0;
        int failureCount = 0;
        for (int index = 0; index < requestedKeys.size(); index++) {
            String requestedKey = requestedKeys.get(index);
            BatchDeleteItemResult item;
            String validationError = validationErrors.get(index);
            if (validationError != null) {
                item = BatchDeleteItemResult.builder()
                        .requestedKey(String.valueOf(requestedKey))
                        .resolvedKey(null)
                        .success(false)
                        .message(validationError)
                        .build();
            } else if (backendIterator.hasNext()) {
                BatchDeleteItemResult backendItem = backendIterator.next();
                item = BatchDeleteItemResult.builder()
                        .requestedKey(requestedKey)
                        .resolvedKey(backendItem.getResolvedKey())
                        .success(backendItem.isSuccess())
                        .message(backendItem.getMessage())
                        .build();
            } else {
                String resolvedKey = StorageObjectKeyGenerator.requireValidKey(requestedKey, "key");
                item = BatchDeleteItemResult.builder()
                        .requestedKey(requestedKey)
                        .resolvedKey(resolvedKey)
                        .success(false)
                        .message("Unknown delete result")
                        .build();
            }
            builder.item(item);
            if (item.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        return builder.successCount(successCount)
                .failureCount(failureCount)
                .build();
    }

    @Override
    public String copy(String sourceKey, String targetKey) throws IOException {
        String normalizedSourceKey = requireKey(sourceKey, "sourceKey");
        String normalizedTargetKey = requireKey(targetKey, "targetKey");
        if (normalizedSourceKey.equals(normalizedTargetKey)) {
            throw new IllegalArgumentException("sourceKey and targetKey must not resolve to the same key");
        }
        backend.copy(normalizedSourceKey, normalizedTargetKey);
        return normalizedTargetKey;
    }

    @Override
    public String move(String sourceKey, String targetKey) throws IOException {
        String normalizedSourceKey = requireKey(sourceKey, "sourceKey");
        String normalizedTargetKey = requireKey(targetKey, "targetKey");
        if (normalizedSourceKey.equals(normalizedTargetKey)) {
            return normalizedSourceKey;
        }
        backend.move(normalizedSourceKey, normalizedTargetKey);
        return normalizedTargetKey;
    }

    private UploadedFileInfo uploadGeneratedKey(String generatedKey, FileUploadRequest request) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(generatedKey, "key");
        return backend.upload(normalizedKey, request, conflictStrategy);
    }

    private byte[] readBytesWithLimit(StoredObject storedObject) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long totalRead = 0;
        int read;
        while ((read = storedObject.inputStream().read(buffer)) != -1) {
            totalRead += read;
            if (totalRead > maxInMemoryReadBytes) {
                throw new IOException("Stored object is too large to read into memory: " + storedObject.key());
            }
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private String requireKey(String key, String fieldName) {
        return StorageObjectKeyGenerator.requireValidKey(key, fieldName);
    }
}
