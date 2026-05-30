package com.zyn.infra.discovery;

/**
 * 持有 {@link DiscoveryProperties} 的静态引用，
 * 供 {@link ServiceClientRegistrar.ServiceClientFactoryBean} 在 Bean 创建时获取。
 */
public final class DiscoveryPropertiesHolder {

    private static volatile DiscoveryProperties instance;

    private DiscoveryPropertiesHolder() {
    }

    public static void set(DiscoveryProperties props) {
        instance = props;
    }

    public static DiscoveryProperties get() {
        if (instance == null) {
            throw new IllegalStateException("DiscoveryProperties not initialized");
        }
        return instance;
    }
}
