package com.zippyboot.infra.storage.config;

public enum StorageConflictStrategy {
    APPEND_SUFFIX,
    FAIL,
    OVERWRITE
}
