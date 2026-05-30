package com.zyn.infra.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * 自动加载 classpath 下的 zyn-services.yml，无需应用手动 import。
 */
public class ServiceDiscoveryPostProcessor implements EnvironmentPostProcessor {

    private static final String CONFIG_FILE = "zyn-services.yml";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new ClassPathResource(CONFIG_FILE);
        if (!resource.exists()) {
            return;
        }
        try {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> sources = loader.load(CONFIG_FILE, resource);
            for (PropertySource<?> source : sources) {
                environment.getPropertySources().addLast(source);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }
}
