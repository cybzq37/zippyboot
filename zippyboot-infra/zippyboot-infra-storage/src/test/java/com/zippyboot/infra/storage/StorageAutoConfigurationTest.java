package com.zippyboot.infra.storage;

import com.zippyboot.infra.storage.config.StorageAutoConfiguration;
import com.zippyboot.infra.storage.service.StorageService;
import com.zippyboot.infra.storage.spi.StorageBackend;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class StorageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class));

    @Test
    void shouldAutoConfigureLocalStorageByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(StorageService.class);
            assertThat(context).doesNotHaveBean(StorageBackend.class);
        });
    }

    @Test
    void shouldFailFastWhenS3CredentialsArePartial() {
        contextRunner
                .withPropertyValues(
                        "zippyboot.infra.storage.type=S3",
                        "zippyboot.infra.storage.s3.access-key=test-access-key"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("Both zippyboot.infra.storage.s3.access-key and secret-key must be configured together");
                });
    }

    @Test
    void shouldUseDedicatedStorageS3ClientBean() {
        contextRunner
                .withPropertyValues("zippyboot.infra.storage.type=S3")
                .withBean("storageS3Client", S3Client.class, () -> org.mockito.Mockito.mock(S3Client.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(StorageService.class);
                    assertThat(context).doesNotHaveBean(StorageBackend.class);
                });
    }
}
