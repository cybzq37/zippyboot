package com.zippyboot.kit.okhttp;

import java.io.File;

public class HttpFormDataFile {

    private String fieldName; // 字段名称

    private String contentType;

    private String fileName; // 原文件名
    private byte[] fileData;
    private File file;

    private HttpFormDataFile() {}

    public static HttpFormDataFile ofBytes(String fieldName, String fileName, byte[] fileData) {
        return new HttpFormDataFile(fieldName, fileName, fileData);
    }

    public static HttpFormDataFile ofFile(String fieldName, String fileName, File file) {
        return new HttpFormDataFile(fieldName, fileName, file);
    }

    public HttpFormDataFile(String fieldName, String fileName, byte[] fileData) {
        this(fieldName, fileName, "application/octet-stream", fileData);
    }

    public HttpFormDataFile(String fieldName, String fileName, String contentType, byte[] fileData) {
        this.fieldName = isBlank(fieldName) ? "file" : fieldName;
        this.fileName = isBlank(fileName) ? "file" : fileName;
        this.contentType = isBlank(contentType) ? "application/octet-stream" : contentType;

        this.fileData = fileData;
    }

    public HttpFormDataFile(String fieldName, String fileName, File file) {
        this(fieldName, fileName, "application/octet-stream", file);
    }

    public HttpFormDataFile(String fieldName, String fileName, String contentType, File file) {
        this.fieldName = isBlank(fieldName) ? "file" : fieldName;
        this.fileName = !isBlank(fileName) ? fileName : (file != null ? file.getName() : "file");
        this.contentType = isBlank(contentType) ? "application/octet-stream" : contentType;

        this.file = file;
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
