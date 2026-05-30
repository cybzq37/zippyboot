package com.zyn.kit.okhttp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "zyn.kit.okhttp")
public class HttpClientProperties {

    public static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
    public static final long DEFAULT_READ_TIMEOUT_SECONDS = 10;
    public static final long DEFAULT_WRITE_TIMEOUT_SECONDS = 10;
    public static final long DEFAULT_CALL_TIMEOUT_SECONDS = 30;

    /**
     * 建立 TCP 连接的超时时间（秒）。
     * 兼顾内网快速连接和外网 API 的网络延迟。
     */
    private long connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;

    /**
     * 读取响应数据的超时时间（秒）。
     * 适用于绝大多数业务接口，大文件下载场景需单独调大。
     */
    private long readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SECONDS;

    /**
     * 发送请求体的超时时间（秒）。
     * 普通 JSON 请求通常不需要很长时间。
     */
    private long writeTimeoutSeconds = DEFAULT_WRITE_TIMEOUT_SECONDS;

    /**
     * 整个请求生命周期总超时时间（秒）。
     * 应大于 connect + read 之和，否则会在单项超时之前触发。
     */
    private long callTimeoutSeconds = DEFAULT_CALL_TIMEOUT_SECONDS;

    /** 是否在 HTTP 非 2xx 或 IO 异常时抛出异常。 */
    private boolean throwOnHttpError = false;

    /** 是否启用 OkHttp 请求日志拦截器。 */
    private boolean enableLogInterceptor = false;

    /** 日志中请求/响应体最大显示字节数。 */
    private int logMaxBodyBytes = 8192;

    /** 日志中请求/响应体最大显示字符数。 */
    private int logMaxBodyChars = 8192;
}
