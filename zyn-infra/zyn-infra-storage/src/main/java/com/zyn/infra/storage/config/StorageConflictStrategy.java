package com.zyn.infra.storage.config;

public enum StorageConflictStrategy {
    APPEND_SUFFIX,
    FAIL,
    OVERWRITE
}
