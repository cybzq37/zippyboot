package com.zippyboot.kit.jackson.serializer;

import com.zippyboot.kit.jackson.plugins.sensitive.SensitiveService;

/**
 * 持有 {@link SensitiveService} 的 Spring Bean 引用
 * <p>
 * 由于 Jackson 的 {@link SensitiveJsonSerializer} 由 Jackson 直接实例化，
 * 无法通过 Spring 依赖注入获取 SensitiveService，此类作为桥梁提供访问途径。
 *
 * @author lichunqing
 */
public class SensitiveServiceHolder {

    private static SensitiveService instance;

    public SensitiveServiceHolder(SensitiveService sensitiveService) {
        instance = sensitiveService;
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
