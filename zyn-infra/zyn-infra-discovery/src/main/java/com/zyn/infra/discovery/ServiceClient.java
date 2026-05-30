package com.zyn.infra.discovery;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在 {@code @HttpExchange} 接口上，声明该客户端对应的服务名。
 * <p>
 * 服务地址从 {@code zyn.discovery.services.<serviceName>} 读取。
 *
 * <pre>
 * &#64;ServiceClient("sys")
 * &#64;HttpExchange("/api/v1/user")
 * public interface RemoteUserService {
 *     ...
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceClient {

    /**
     * 服务名，对应 zyn.discovery.services 中的 key。
     */
    String value();
}
