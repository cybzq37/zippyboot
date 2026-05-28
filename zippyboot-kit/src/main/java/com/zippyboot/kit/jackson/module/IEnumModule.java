package com.zippyboot.kit.jackson.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zippyboot.kit.enums.IEnum;

/**
 * Jackson 模块，自动注册 {@link IEnum} 的序列化/反序列化器。
 * <p>
 * 注册后，所有实现 {@link IEnum} 的枚举无需额外配置即可：
 * <ul>
 *   <li>序列化：返回 {@code {"code":1,"desc":"男"}} 格式</li>
 *   <li>反序列化：接受对象格式或 code 值格式</li>
 * </ul>
 * 通常由 {@link com.zippyboot.kit.jackson.config.JacksonConfig} 自动注册，无需手动使用。
 */
public class IEnumModule extends SimpleModule {

    public IEnumModule() {
        super("IEnumModule");
        addDeserializer(IEnum.class, new IEnumDeserializer());
        addKeyDeserializer(IEnum.class, new IEnumKeyDeserializer());
    }
}
