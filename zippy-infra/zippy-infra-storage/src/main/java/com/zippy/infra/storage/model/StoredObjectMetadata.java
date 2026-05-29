package com.zippy.infra.storage.model;

public record StoredObjectMetadata(
        String key,
        String contentType,
        long size,
        String accessUrl
) {
}
