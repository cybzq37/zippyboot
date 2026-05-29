package com.zippy.infra.storage.config;

public enum StorageConflictStrategy {
    APPEND_SUFFIX,
    FAIL,
    OVERWRITE
}
