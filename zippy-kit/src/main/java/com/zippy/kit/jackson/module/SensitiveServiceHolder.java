package com.zippy.kit.jackson.module;

import com.zippy.kit.jackson.plugins.sensitive.SensitiveService;
import org.springframework.beans.factory.InitializingBean;

/**
 * 持有 {@link SensitiveService} 的 Spring Bean 引用
 * <p>
 * 由于 Jackson 的 {@link SensitiveJsonSerializer} 由 Jackson 直接实例化，
 * 无法通过 Spring 依赖注入获取 SensitiveService，此类作为桥梁提供访问途径。
 *
 * @author lichunqing
 */
public class SensitiveServiceHolder implements InitializingBean {

    private static volatile SensitiveService instance;

    private final SensitiveService sensitiveService;

    public SensitiveServiceHolder(SensitiveService sensitiveService) {
        this.sensitiveService = sensitiveService;
    }

    @Override
    public void afterPropertiesSet() {
        instance = this.sensitiveService;
    }

    /**
     * 获取 SensitiveService 实例
     *
     * @return SensitiveService，如果 Spring 容器中不存在则返回 null
     */
    public static SensitiveService getInstance() {
        return instance;
    }
}
