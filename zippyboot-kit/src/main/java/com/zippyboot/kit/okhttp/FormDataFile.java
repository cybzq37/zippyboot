package com.zippyboot.kit.okhttp;

import java.io.File;

public class FormDataFile {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_FIELD_NAME = "file";

    private final String fieldName;
    private final String contentType;
    private final String fileName;
    private final byte[] fileData;
    private final File file;

    private FormDataFile(String fieldName, String fileName, String contentType, byte[] fileData, File file) {
        this.fieldName = isBlank(fieldName) ? DEFAULT_FIELD_NAME : fieldName;
        this.fileName = resolveFileName(fileName, file);
        this.contentType = isBlank(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
        this.fileData = fileData;
        this.file = file;
    }

    public static FormDataFile ofBytes(String fieldName, String fileName, byte[] fileData) {
        return new FormDataFile(fieldName, fileName, DEFAULT_CONTENT_TYPE, fileData, null);
    }

    public static FormDataFile ofBytes(String fieldName, String fileName, String contentType, byte[] fileData) {
        return new FormDataFile(fieldName, fileName, contentType, fileData, null);
    }

    public static FormDataFile ofFile(String fieldName, String fileName, File file) {
        return new FormDataFile(fieldName, fileName, DEFAULT_CONTENT_TYPE, null, file);
    }

    public static FormDataFile ofFile(String fieldName, String fileName, String contentType, File file) {
        return new FormDataFile(fieldName, fileName, contentType, null, file);
    }

    private static String resolveFileName(String fileName, File file) {
        if (!isBlank(fileName)) {
            return fileName;
        }
        return file != null ? file.getName() : DEFAULT_FIELD_NAME;
    }

    private static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public File getFile() {
        return file;
    }
}
