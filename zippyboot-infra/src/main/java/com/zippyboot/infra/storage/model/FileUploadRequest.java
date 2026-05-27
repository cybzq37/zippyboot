package com.zippyboot.infra.storage.model;

import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.io.IOException;

@Value
@Builder
public class FileUploadRequest {

    String originalFilename;
    String contentType;
    long size;
    InputStreamSource inputStreamSource;

    public void validate() {
        Assert.notNull(inputStreamSource, "inputStreamSource must not be null");
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
    }

    public InputStream openStream() throws IOException {
        return inputStreamSource.getInputStream();
    }
}
