package com.zippyboot.kit.okhttp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zippyboot.kit.okhttp")
public class HttpClientProperties {

    /**
     * 建立 TCP 连接的超时时间（秒）。
     * 内网服务通常很快，超时过大会延迟故障发现。
     */
    public static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 2;

    /**
     * 读取响应数据的超时时间（秒）。
     * 适用于绝大多数普通业务接口。
     */
    public static final long DEFAULT_READ_TIMEOUT_SECONDS = 8;

    /**
     * 发送请求体的超时时间（秒）。
     * 普通 JSON 请求通常不需要很长时间。
     */
    public static final long DEFAULT_WRITE_TIMEOUT_SECONDS = 8;

    /**
     * 整个请求生命周期总超时时间（秒）。
     * 用于限制一次调用的总耗时上限。
     */
    public static final long DEFAULT_CALL_TIMEOUT_SECONDS = 12;

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

    public long getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(long connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public long getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(long readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public long getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public void setWriteTimeoutSeconds(long writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

    public long getCallTimeoutSeconds() {
        return callTimeoutSeconds;
    }

    public void setCallTimeoutSeconds(long callTimeoutSeconds) {
        this.callTimeoutSeconds = callTimeoutSeconds;
    }

    public boolean isThrowOnHttpError() {
        return throwOnHttpError;
    }

    public void setThrowOnHttpError(boolean throwOnHttpError) {
        this.throwOnHttpError = throwOnHttpError;
    }

    public boolean isEnableLogInterceptor() {
        return enableLogInterceptor;
    }

    public void setEnableLogInterceptor(boolean enableLogInterceptor) {
        this.enableLogInterceptor = enableLogInterceptor;
    }
}
