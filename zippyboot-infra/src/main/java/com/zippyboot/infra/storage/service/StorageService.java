package com.zippyboot.infra.storage.service;

import com.zippyboot.infra.storage.model.FileUploadRequest;
import com.zippyboot.infra.storage.model.UploadedFileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    UploadedFileInfo upload(FileUploadRequest request) throws IOException;

    default UploadedFileInfo upload(MultipartFile file) throws IOException {
        return upload(FileUploadRequest.builder()
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .inputStream(file.getInputStream())
                .build());
    }

    boolean delete(String objectKey);
}
