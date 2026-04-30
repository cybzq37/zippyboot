package com.zippyboot.infra.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.storage")
public class StorageConfig {

    private StorageType type = StorageType.LOCAL;
    private String publicBaseUrl;
    private String datePathPattern = "yyyy/MM/dd";
    private String filenameStrategy = "uuid";

    private Local local = new Local();
    private ObjectStore object = new ObjectStore();

    @Data
    public static class Local {
        private String rootPath = "uploads";
        private String accessPathPrefix = "/uploads";
    }

    @Data
    public static class ObjectStore {
        private String bucket = "zippyboot";
        private String endpoint;
        private String region;
        private String accessKey;
        private String secretKey;
        private String domain;
    }
}
