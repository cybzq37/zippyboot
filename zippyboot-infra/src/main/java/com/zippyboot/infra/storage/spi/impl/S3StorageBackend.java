package com.zippyboot.infra.storage.spi.impl;

import com.zippyboot.infra.storage.model.BatchDeleteResult;
import com.zippyboot.infra.storage.model.BatchDeleteItemResult;
import com.zippyboot.infra.storage.config.StorageConflictStrategy;
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
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.DeletedObject;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class S3StorageBackend implements StorageBackend, AutoCloseable {

    private final StorageProperties properties;
    private final StorageProperties.S3Properties s3Properties;
    private final S3Client s3Client;
    private final boolean closeClient;

    public S3StorageBackend(StorageProperties properties, S3Client s3Client, boolean closeClient) {
        this.properties = properties;
        this.s3Properties = properties.getS3();
        this.s3Client = Objects.requireNonNull(s3Client, "s3Client must not be null");
        this.closeClient = closeClient;
        validate();
    }

    @Override
    public UploadedFileInfo upload(String key,
                                   FileUploadRequest request,
                                   StorageConflictStrategy conflictStrategy) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        return switch (resolveConflictStrategy(conflictStrategy)) {
            case OVERWRITE -> putObject(normalizedKey, request, false);
            case FAIL -> putObject(normalizedKey, request, true);
            case APPEND_SUFFIX -> putObjectWithSuffix(normalizedKey, request);
        };
    }

    @Override
    public StoredObject open(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        try {
            ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(normalizedKey)
                    .build());
            GetObjectResponse response = inputStream.response();
            long size = response.contentLength() == null ? -1L : response.contentLength();
            return new StoredObject(normalizedKey, response.contentType(), size, inputStream);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404 || exception.statusCode() == 403) {
                throw new StorageObjectNotFoundException("Storage object not found: " + normalizedKey, exception);
            }
            throw new StorageBackendException("Failed to open object from S3: " + normalizedKey, exception);
        } catch (SdkException exception) {
            throw new StorageBackendException("Failed to open object from S3: " + normalizedKey, exception);
        }
    }

    @Override
    public StoredObjectMetadata getMetadata(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        try {
            HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(normalizedKey)
                    .build());
            long size = response.contentLength() == null ? -1L : response.contentLength();
            return new StoredObjectMetadata(normalizedKey, response.contentType(), size, buildAccessUrl(normalizedKey));
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404 || exception.statusCode() == 403) {
                throw new StorageObjectNotFoundException("Storage object not found: " + normalizedKey, exception);
            }
            throw new StorageBackendException("Failed to load object metadata from S3: " + normalizedKey, exception);
        } catch (SdkException exception) {
            throw new StorageBackendException("Failed to load object metadata from S3: " + normalizedKey, exception);
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(normalizedKey)
                    .build());
            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }
            throw new StorageBackendException("Failed to query object from S3: " + normalizedKey, exception);
        } catch (SdkException exception) {
            throw new StorageBackendException("Failed to query object from S3: " + normalizedKey, exception);
        }
    }

    @Override
    public void copy(String sourceKey, String targetKey) throws IOException {
        String normalizedSourceKey = StorageObjectKeyGenerator.requireValidKey(sourceKey, "sourceKey");
        String normalizedTargetKey = StorageObjectKeyGenerator.requireValidKey(targetKey, "targetKey");
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .destinationBucket(s3Properties.getBucket())
                    .destinationKey(normalizedTargetKey)
                    .copySource(encodeCopySource(s3Properties.getBucket(), normalizedSourceKey))
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404 || exception.statusCode() == 403) {
                throw new StorageObjectNotFoundException("Storage object not found: " + normalizedSourceKey, exception);
            }
            throw new StorageBackendException("Failed to copy object in S3: " + normalizedSourceKey + " -> " + normalizedTargetKey, exception);
        } catch (SdkException exception) {
            throw new StorageBackendException("Failed to copy object in S3: " + normalizedSourceKey + " -> " + normalizedTargetKey, exception);
        }
    }

    @Override
    public void move(String sourceKey, String targetKey) throws IOException {
        String normalizedSourceKey = StorageObjectKeyGenerator.requireValidKey(sourceKey, "sourceKey");
        String normalizedTargetKey = StorageObjectKeyGenerator.requireValidKey(targetKey, "targetKey");
        copy(normalizedSourceKey, normalizedTargetKey);
        try {
            delete(normalizedSourceKey);
        } catch (StorageObjectNotFoundException deleteException) {
            deleteQuietly(normalizedTargetKey, deleteException);
            throw deleteException;
        } catch (IOException deleteException) {
            try {
                delete(normalizedTargetKey);
            } catch (IOException rollbackException) {
                deleteException.addSuppressed(rollbackException);
                throw new StorageBackendException("Failed to move object in S3 and rollback target failed: "
                        + normalizedSourceKey + " -> " + normalizedTargetKey, deleteException);
            }
            throw new StorageBackendException("Failed to move object in S3, target rollback completed: "
                    + normalizedSourceKey + " -> " + normalizedTargetKey, deleteException);
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
        if (keys.isEmpty()) {
            builder.successCount(0);
            builder.failureCount(0);
            return builder.build();
        }
        List<String> normalizedKeys = keys.stream()
                .map(key -> StorageObjectKeyGenerator.requireValidKey(key, "key"))
                .toList();
        List<ObjectIdentifier> identifiers = normalizedKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();
        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .delete(Delete.builder().objects(identifiers).build())
                    .build());
            List<DeletedObject> deletedObjects = response.deleted() == null ? List.of() : response.deleted();
            List<S3Error> errors = response.errors() == null ? List.of() : response.errors();
            Map<String, Integer> successCounts = new HashMap<>();
            for (DeletedObject deleted : deletedObjects) {
                successCounts.merge(deleted.key(), 1, Integer::sum);
            }
            Map<String, ArrayDeque<String>> errorMessages = new HashMap<>();
            for (S3Error error : errors) {
                errorMessages.computeIfAbsent(error.key(), ignored -> new ArrayDeque<>()).add(error.message());
            }
            int successCount = 0;
            int failureCount = 0;
            for (String key : normalizedKeys) {
                ArrayDeque<String> messages = errorMessages.get(key);
                Integer remainingSuccess = successCounts.getOrDefault(key, 0);
                if (messages != null && !messages.isEmpty()) {
                    builder.item(BatchDeleteItemResult.builder()
                            .requestedKey(key)
                            .resolvedKey(key)
                            .success(false)
                            .message(messages.removeFirst())
                            .build());
                    failureCount++;
                    continue;
                }
                if (remainingSuccess > 0) {
                    successCounts.put(key, remainingSuccess - 1);
                    builder.item(BatchDeleteItemResult.builder()
                            .requestedKey(key)
                            .resolvedKey(key)
                            .success(true)
                            .message(null)
                            .build());
                    successCount++;
                    continue;
                }
                builder.item(BatchDeleteItemResult.builder()
                        .requestedKey(key)
                        .resolvedKey(key)
                        .success(false)
                        .message("Unknown delete result")
                        .build());
                failureCount++;
            }
            builder.successCount(successCount);
            builder.failureCount(failureCount);
            return builder.build();
        } catch (SdkException exception) {
            int failureCount = 0;
            for (String key : normalizedKeys) {
                builder.item(BatchDeleteItemResult.builder()
                        .requestedKey(key)
                        .resolvedKey(key)
                        .success(false)
                        .message(exception.getMessage())
                        .build());
                failureCount++;
            }
            builder.successCount(0);
            builder.failureCount(failureCount);
            return builder.build();
        }
    }

    @Override
    public void delete(String key) throws IOException {
        String normalizedKey = StorageObjectKeyGenerator.requireValidKey(key, "key");
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(normalizedKey)
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404 || exception.statusCode() == 403) {
                throw new StorageObjectNotFoundException("Storage object not found: " + normalizedKey, exception);
            }
            throw new StorageBackendException("Failed to delete object from S3: " + normalizedKey, exception);
        } catch (SdkException exception) {
            throw new StorageBackendException("Failed to delete object from S3: " + normalizedKey, exception);
        }
    }

    @Override
    public void close() {
        if (closeClient) {
            s3Client.close();
        }
    }

    private String buildAccessUrl(String key) {
        String encodedKey = StorageObjectKeyGenerator.encodeUrlPath(key);
        String publicBaseUrl = stripTrailingSlash(properties.getPublicBaseUrl());
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl + "/" + encodedKey;
        }

        String domain = stripTrailingSlash(s3Properties.getDomain());
        if (StringUtils.hasText(domain)) {
            return domain + "/" + encodedKey;
        }

        String endpoint = stripTrailingSlash(s3Properties.getEndpoint());
        if (StringUtils.hasText(endpoint)) {
            return buildEndpointAccessUrl(endpoint, encodedKey);
        }

        String region = StringUtils.hasText(s3Properties.getRegion()) ? s3Properties.getRegion().trim() : "us-east-1";
        return "https://" + s3Properties.getBucket()
                + ".s3."
                + region
                + ".amazonaws.com/"
                + encodedKey;
    }

    private void validate() {
        if (!StringUtils.hasText(s3Properties.getBucket())) {
            throw new IllegalStateException("zippyboot.infra.storage.s3.bucket must not be blank");
        }
    }

    private String buildEndpointAccessUrl(String endpoint, String key) {
        URI endpointUri = URI.create(endpoint);
        String normalizedPath = normalizeUriPath(endpointUri.getPath());
        if (s3Properties.isPathStyleAccess() || !StringUtils.hasText(endpointUri.getHost())) {
            return rebuildUri(endpointUri, endpointUri.getHost(), joinPath(normalizedPath, s3Properties.getBucket(), key));
        }
        String bucketHost = s3Properties.getBucket() + "." + endpointUri.getHost();
        return rebuildUri(endpointUri, bucketHost, joinPath(normalizedPath, key));
    }

    private String rebuildUri(URI uri, String host, String path) {
        try {
            return new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    host,
                    uri.getPort(),
                    path,
                    null,
                    null
            ).toString();
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Failed to build storage access URL", exception);
        }
    }

    private String normalizeUriPath(String path) {
        if (!StringUtils.hasText(path) || "/".equals(path)) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String joinPath(String prefix, String... segments) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(prefix)) {
            builder.append(prefix);
        }
        for (String segment : segments) {
            if (!StringUtils.hasText(segment)) {
                continue;
            }
            if (builder.isEmpty() || builder.charAt(builder.length() - 1) != '/') {
                builder.append('/');
            }
            builder.append(StorageObjectKeyGenerator.normalizeKey(segment));
        }
        return builder.isEmpty() ? "/" : builder.toString();
    }

    private String encodeCopySource(String bucket, String key) {
        String encodedKey = StorageObjectKeyGenerator.encodeUrlPath(key);
        return bucket + "/" + encodedKey;
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

    private UploadedFileInfo putObject(String key,
                                       FileUploadRequest request,
                                       boolean failIfExists) throws IOException {
        if (failIfExists && exists(key)) {
            throw new StorageConflictException("Storage object already exists: " + key);
        }
        PutObjectRequest.Builder putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key);
        if (StringUtils.hasText(request.getContentType())) {
            putObjectRequest.contentType(request.getContentType());
        }
        try (InputStream inputStream = request.openStream()) {
            s3Client.putObject(putObjectRequest.build(), RequestBody.fromInputStream(inputStream, request.getSize()));
            return buildUploadedFileInfo(key, request);
        } catch (S3Exception exception) {
            throw new StorageBackendException("Failed to upload object to S3: " + key, exception);
        } catch (SdkException exception) {
            throw new StorageBackendException("Failed to upload object to S3: " + key, exception);
        }
    }

    private UploadedFileInfo putObjectWithSuffix(String normalizedKey, FileUploadRequest request) throws IOException {
        int suffix = 0;
        while (true) {
            String candidateKey = suffix == 0
                    ? normalizedKey
                    : StorageObjectKeyGenerator.appendNumericSuffix(normalizedKey, suffix);
            try {
                return putObject(candidateKey, request, true);
            } catch (StorageConflictException exception) {
                suffix++;
            }
        }
    }

    private UploadedFileInfo buildUploadedFileInfo(String key, FileUploadRequest request) {
        return UploadedFileInfo.builder()
                .storageType(StorageType.S3)
                .bucket(s3Properties.getBucket())
                .key(key)
                .originalFilename(request.getOriginalFilename())
                .storedFilename(StorageObjectKeyGenerator.extractFilename(key))
                .contentType(request.getContentType())
                .size(request.getSize())
                .accessUrl(buildAccessUrl(key))
                .build();
    }

    private StorageConflictStrategy resolveConflictStrategy(StorageConflictStrategy conflictStrategy) {
        return conflictStrategy == null ? StorageConflictStrategy.APPEND_SUFFIX : conflictStrategy;
    }

    private void deleteQuietly(String key, IOException originalException) throws IOException {
        try {
            delete(key);
        } catch (IOException rollbackException) {
            originalException.addSuppressed(rollbackException);
            throw originalException;
        }
    }
}
