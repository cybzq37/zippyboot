package com.zippy.kit.okhttp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "zippy.kit.okhttp")
public class HttpClientProperties {

    /**
     * 建立 TCP 连接的超时时间（秒）。
     * 兼顾内网快速连接和外网 API 的网络延迟。
     */
    public static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;

    /**
     * 读取响应数据的超时时间（秒）。
     * 适用于绝大多数业务接口，大文件下载场景需单独调大。
     */
    public static final long DEFAULT_READ_TIMEOUT_SECONDS = 10;

    /**
     * 发送请求体的超时时间（秒）。
     * 普通 JSON 请求通常不需要很长时间。
     */
    public static final long DEFAULT_WRITE_TIMEOUT_SECONDS = 10;

    /**
     * 整个请求生命周期总超时时间（秒）。
     * 应大于 connect + read 之和，否则会在单项超时之前触发。
     */
    public static final long DEFAULT_CALL_TIMEOUT_SECONDS = 30;

    /**
     * 响应体最大读取字节数（默认 10MB）。
     * 超过此限制将截断响应并抛出异常，防止 OOM。
     */
    public static final long DEFAULT_MAX_RESPONSE_BYTES = 10 * 1024 * 1024;

    /** 建立连接超时（秒）。 */
    private long connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;

    /** 读取响应超时（秒）。 */
    private long readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SECONDS;

    /** 发送请求超时（秒）。 */
    private long writeTimeoutSeconds = DEFAULT_WRITE_TIMEOUT_SECONDS;

    /** 单次请求总超时（秒）。 */
    private long callTimeoutSeconds = DEFAULT_CALL_TIMEOUT_SECONDS;

    /** 是否在 HTTP 非 2xx 或 IO 异常时抛出异常。 */
    private boolean throwOnHttpError = false;

    /** 是否启用 OkHttp 请求日志拦截器。 */
    private boolean enableLogInterceptor = false;

    /** 响应体最大读取字节数，超过将抛出异常防止 OOM。 */
    private long maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;
}
