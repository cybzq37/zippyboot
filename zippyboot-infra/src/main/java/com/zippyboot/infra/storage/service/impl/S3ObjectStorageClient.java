package com.zippyboot.infra.storage.service.impl;

import com.zippyboot.infra.storage.config.StorageConfig;
import com.zippyboot.infra.storage.config.StorageType;
import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.UploadedFileInfo;
import com.zippyboot.infra.storage.service.ObjectStorageClient;
import com.zippyboot.infra.storage.support.StoragePathHelper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

@Component
public class S3ObjectStorageClient implements ObjectStorageClient {

    @Override
    public StorageType type() {
        return StorageType.S3;
    }

    @Override
    public UploadedFileInfo upload(String objectKey, FileUploadRequest request, StorageConfig.ObjectStore objectStore) throws IOException {
        String bucket = required(objectStore.getBucket(), "bucket");
        String normalizedKey = StoragePathHelper.normalizeObjectKey(objectKey);

        PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(normalizedKey);
        if (request.getContentType() != null && !request.getContentType().isBlank()) {
            putBuilder.contentType(request.getContentType());
        }

        try (S3Client client = buildClient(objectStore); var in = request.getInputStream()) {
            client.putObject(putBuilder.build(), RequestBody.fromInputStream(in, request.getSize()));
        }

        String accessUrl = buildAccessUrl(objectStore, bucket, normalizedKey);
        String storedFilename = extractFileName(normalizedKey);

        return UploadedFileInfo.builder()
                .storageType(StorageType.S3)
                .bucket(bucket)
                .objectKey(normalizedKey)
                .originalFilename(request.getOriginalFilename())
                .storedFilename(storedFilename)
                .contentType(request.getContentType())
                .size(request.getSize())
                .relativePath(normalizedKey)
                .absolutePath(null)
                .accessUrl(accessUrl)
                .build();
    }

    @Override
    public boolean delete(String objectKey, StorageConfig.ObjectStore objectStore) {
        String bucket = objectStore.getBucket();
        if (bucket == null || bucket.isBlank() || objectKey == null || objectKey.isBlank()) {
            return false;
        }
        try (S3Client client = buildClient(objectStore)) {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(StoragePathHelper.normalizeObjectKey(objectKey))
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private S3Client buildClient(StorageConfig.ObjectStore objectStore) {
        String accessKey = required(objectStore.getAccessKey(), "access-key");
        String secretKey = required(objectStore.getSecretKey(), "secret-key");
        String regionName = objectStore.getRegion();
        if (regionName == null || regionName.isBlank()) {
            regionName = "us-east-1";
        }

        S3Client.Builder builder = S3Client.builder()
                .region(Region.of(regionName))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));

        String endpoint = objectStore.getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
            builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }
        return builder.build();
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing storage object config: " + field);
        }
        return value;
    }

    private String extractFileName(String key) {
        int index = key.lastIndexOf('/');
        if (index < 0 || index == key.length() - 1) {
            return key;
        }
        return key.substring(index + 1);
    }

    private String buildAccessUrl(StorageConfig.ObjectStore objectStore, String bucket, String objectKey) {
        if (objectStore.getDomain() != null && !objectStore.getDomain().isBlank()) {
            String domain = stripTrailingSlash(objectStore.getDomain());
            return domain + "/" + objectKey;
        }

        if (objectStore.getEndpoint() != null && !objectStore.getEndpoint().isBlank()) {
            String endpoint = stripTrailingSlash(objectStore.getEndpoint());
            return endpoint + "/" + bucket + "/" + objectKey;
        }

        String region = (objectStore.getRegion() == null || objectStore.getRegion().isBlank())
                ? "us-east-1"
                : objectStore.getRegion();
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;
    }

    private String stripTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
