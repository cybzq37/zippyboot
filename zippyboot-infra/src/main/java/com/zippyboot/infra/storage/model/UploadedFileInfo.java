package com.zippyboot.infra.storage.model;

import com.zippyboot.infra.storage.config.StorageType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadedFileInfo {

    StorageType storageType;
    String bucket;
    String objectKey;
    String originalFilename;
    String storedFilename;
    String contentType;
    long size;
    String relativePath;
    String absolutePath;
    String accessUrl;
}
