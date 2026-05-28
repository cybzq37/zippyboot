package com.zippyboot.kit.okhttp;

import com.zippyboot.kit.util.StringUtils;

import java.io.File;

public record FormDataFile(String fieldName, String contentType, String fileName, byte[] fileData, File file) {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_FIELD_NAME = "file";

    public FormDataFile {
        fieldName = StringUtils.isBlank(fieldName) ? DEFAULT_FIELD_NAME : fieldName;
        contentType = StringUtils.isBlank(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
        fileName = resolveFileName(fileName, file);
    }

    public static FormDataFile ofBytes(String fieldName, String fileName, byte[] fileData) {
        return new FormDataFile(fieldName, DEFAULT_CONTENT_TYPE, fileName, fileData, null);
    }

    public static FormDataFile ofBytes(String fieldName, String fileName, String contentType, byte[] fileData) {
        return new FormDataFile(fieldName, contentType, fileName, fileData, null);
    }

    public static FormDataFile ofFile(String fieldName, String fileName, File file) {
        return new FormDataFile(fieldName, DEFAULT_CONTENT_TYPE, fileName, null, file);
    }

    public static FormDataFile ofFile(String fieldName, String fileName, String contentType, File file) {
        return new FormDataFile(fieldName, contentType, fileName, null, file);
    }

    private static String resolveFileName(String fileName, File file) {
        if (!StringUtils.isBlank(fileName)) {
            return fileName;
        }
        return file != null ? file.getName() : DEFAULT_FIELD_NAME;
    }
}
