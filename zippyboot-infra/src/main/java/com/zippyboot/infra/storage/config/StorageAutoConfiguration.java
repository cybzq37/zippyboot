package com.zippyboot.infra.storage.config;

import com.zippyboot.infra.storage.service.ObjectStorageClient;
import com.zippyboot.infra.storage.service.StorageService;
import com.zippyboot.infra.storage.service.impl.LocalDiskStorageService;
import com.zippyboot.infra.storage.service.impl.ObjectStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService storageService(StorageConfig config, List<ObjectStorageClient> clients) throws IOException {
        if (config.getType() == StorageType.LOCAL) {
            return new LocalDiskStorageService(config);
        }

        return clients.stream()
                .filter(client -> client.type() == config.getType())
                .findFirst()
                .<StorageService>map(client -> new ObjectStorageService(config, client))
                .orElseThrow(() -> new IllegalStateException("No ObjectStorageClient found for type: " + config.getType()));
    }
}
