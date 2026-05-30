package com.zyn.infra.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;

/**
 * 扫描所有 {@link ServiceClient @ServiceClient} 标记的接口，
 * 自动注册为 HttpExchange 代理 Bean。
 */
@Slf4j
public class ServiceClientRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ServiceClient.class));
        scanner.setResourceLoader(null);

        // 扫描所有包
        int count = scanner.scan("com");
        if (count == 0) {
            return;
        }

        // 为扫描到的接口创建代理 FactoryBean
        String[] names = registry.getBeanNamesForAnnotation(ServiceClient.class);
        for (String name : names) {
            BeanDefinition bd = registry.getBeanDefinition(name);
            String className = bd.getBeanClassName();
            if (className == null) continue;

            try {
                Class<?> clientType = Class.forName(className);
                ServiceClient anno = clientType.getAnnotation(ServiceClient.class);
                if (anno == null) continue;

                String serviceName = anno.value();
                String beanName = className + "Proxy";

                // 避免重复注册
                if (registry.containsBeanDefinition(beanName)) continue;

                AbstractBeanDefinition proxyBd = BeanDefinitionBuilder
                        .genericBeanDefinition(ServiceClientFactoryBean.class)
                        .addConstructorArgValue(clientType)
                        .addConstructorArgValue(serviceName)
                        .setScope(BeanDefinition.SCOPE_SINGLETON)
                        .getBeanDefinition();

                registry.registerBeanDefinition(beanName, proxyBd);

                // 移除原始的空 BeanDefinition（扫描器注册的）
                registry.removeBeanDefinition(name);

                log.info("Discovered service client: {} -> {}", clientType.getSimpleName(), serviceName);
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load service client class: {}", className, e);
            }
        }
    }

    /**
     * FactoryBean：根据服务名从 DiscoveryProperties 获取地址，创建 HttpExchange 代理。
     */
    public static class ServiceClientFactoryBean implements FactoryBean<Object> {

        private final Class<?> clientType;
        private final String serviceName;

        public ServiceClientFactoryBean(Class<?> clientType, String serviceName) {
            this.clientType = clientType;
            this.serviceName = serviceName;
        }

        @Override
        public Object getObject() {
            // DiscoveryProperties 在运行时由 Spring 注入，这里用静态持有者获取
            DiscoveryProperties props = DiscoveryPropertiesHolder.get();
            String url = props.getRequiredUrl(serviceName);
            return ServiceProxyBuilder.build(clientType, url);
        }

        @Override
        public Class<?> getObjectType() {
            return clientType;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
