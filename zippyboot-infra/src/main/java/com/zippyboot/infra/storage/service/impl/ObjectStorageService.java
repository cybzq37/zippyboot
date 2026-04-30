package com.zippyboot.infra.storage.service.impl;

import com.zippyboot.infra.storage.config.StorageConfig;
import com.zippyboot.infra.storage.config.StorageType;
import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.UploadedFileInfo;
import com.zippyboot.infra.storage.service.ObjectStorageClient;
import com.zippyboot.infra.storage.service.StorageService;
import com.zippyboot.infra.storage.support.StoragePathHelper;

import java.io.IOException;

public class ObjectStorageService implements StorageService {

    private final StorageConfig config;
    private final ObjectStorageClient client;

    public ObjectStorageService(StorageConfig config, ObjectStorageClient client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public UploadedFileInfo upload(FileUploadRequest request) throws IOException {
        String objectKey = StoragePathHelper.buildObjectKey(config, request.getOriginalFilename());
        UploadedFileInfo info = client.upload(objectKey, request, config.getObject());
        if (info.getStorageType() == null) {
            return UploadedFileInfo.builder()
                    .storageType(config.getType())
                    .bucket(info.getBucket())
                    .objectKey(info.getObjectKey())
                    .originalFilename(info.getOriginalFilename())
                    .storedFilename(info.getStoredFilename())
                    .contentType(info.getContentType())
                    .size(info.getSize())
                    .relativePath(info.getRelativePath())
                    .absolutePath(info.getAbsolutePath())
                    .accessUrl(info.getAccessUrl())
                    .build();
        }
        return info;
    }

    @Override
    public boolean delete(String objectKey) {
        return client.delete(StoragePathHelper.normalizeObjectKey(objectKey), config.getObject());
    }

    public StorageType currentType() {
        return config.getType();
    }
}
