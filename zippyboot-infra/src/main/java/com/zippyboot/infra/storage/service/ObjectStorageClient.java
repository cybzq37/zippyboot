package com.zippyboot.infra.storage.service;

import com.zippyboot.infra.storage.config.StorageConfig;
import com.zippyboot.infra.storage.config.StorageType;
import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.UploadedFileInfo;

import java.io.IOException;

public interface ObjectStorageClient {

    StorageType type();

    UploadedFileInfo upload(String objectKey, FileUploadRequest request, StorageConfig.ObjectStore objectStore) throws IOException;

    boolean delete(String objectKey, StorageConfig.ObjectStore objectStore);
}
