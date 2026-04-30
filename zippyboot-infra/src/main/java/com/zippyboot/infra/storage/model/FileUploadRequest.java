package com.zippyboot.infra.storage.model;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
public class FileUploadRequest {

    String originalFilename;
    String contentType;
    long size;
    InputStream inputStream;
}
