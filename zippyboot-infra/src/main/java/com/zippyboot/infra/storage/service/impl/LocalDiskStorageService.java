package com.zippyboot.infra.storage.service.impl;

import com.zippyboot.infra.storage.config.StorageConfig;
import com.zippyboot.infra.storage.config.StorageType;
import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.UploadedFileInfo;
import com.zippyboot.infra.storage.service.StorageService;
import com.zippyboot.infra.storage.support.StoragePathHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LocalDiskStorageService implements StorageService {

    private final StorageConfig config;
    private final Path rootPath;

    public LocalDiskStorageService(StorageConfig config) throws IOException {
        this.config = config;
        this.rootPath = Paths.get(config.getLocal().getRootPath()).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    @Override
    public UploadedFileInfo upload(FileUploadRequest request) throws IOException {
        String objectKey = StoragePathHelper.buildObjectKey(config, request.getOriginalFilename());
        Path targetPath = rootPath.resolve(objectKey).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new IOException("Invalid upload path");
        }

        Path parentPath = targetPath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }

        try (var in = request.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String storedFilename = targetPath.getFileName().toString();
        String relativePath = StoragePathHelper.normalizeObjectKey(rootPath.relativize(targetPath).toString());
        String accessUrl = buildAccessUrl(relativePath);

        return UploadedFileInfo.builder()
                .storageType(StorageType.LOCAL)
                .bucket(null)
                .objectKey(relativePath)
                .originalFilename(request.getOriginalFilename())
                .storedFilename(storedFilename)
                .contentType(request.getContentType())
                .size(request.getSize())
                .relativePath(relativePath)
                .absolutePath(targetPath.toString())
                .accessUrl(accessUrl)
                .build();
    }

    @Override
    public boolean delete(String objectKey) {
        String normalized = StoragePathHelper.normalizeObjectKey(objectKey);
        Path targetPath = rootPath.resolve(normalized).normalize();
        if (!targetPath.startsWith(rootPath)) {
            return false;
        }
        try {
            return Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            return false;
        }
    }

    private String buildAccessUrl(String relativePath) {
        String baseUrl = config.getPublicBaseUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            return normalizedBase + "/" + relativePath;
        }
        String prefix = config.getLocal().getAccessPathPrefix();
        String normalizedPrefix = (prefix == null || prefix.isBlank()) ? "/uploads" : prefix;
        if (!normalizedPrefix.startsWith("/")) {
            normalizedPrefix = "/" + normalizedPrefix;
        }
        if (normalizedPrefix.endsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }
        return normalizedPrefix + "/" + relativePath;
    }
}
