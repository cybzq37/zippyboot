package com.zyn.infra.storage.model;

import com.zyn.infra.storage.config.StorageType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadedFileInfo {

    StorageType storageType;
    String bucket;
    String key;
    String originalFilename;
    String storedFilename;
    String contentType;
    long size;
    String accessUrl;
}
