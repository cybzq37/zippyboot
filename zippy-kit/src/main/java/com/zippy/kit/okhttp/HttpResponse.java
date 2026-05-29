package com.zippy.kit.okhttp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 响应封装，隐藏底层 OkHttp 类型。
 *
 * @param statusCode HTTP 状态码，失败时为 -1
 * @param headers    响应头（不可变 Map）
 * @param body       响应体文本
 * @param errorMessage 错误信息（IO 异常等），成功时为 null
 */
public record HttpResponse(int statusCode, Map<String, String> headers, String body, String errorMessage) {

    public static HttpResponse success(int statusCode, Map<String, String> headers, String body) {
        return new HttpResponse(statusCode, headers != null ? headers : Collections.emptyMap(), body, null);
    }

    public static HttpResponse failure(String errorMessage) {
        return new HttpResponse(-1, Collections.emptyMap(), null, errorMessage);
    }

    /**
     * 是否成功（无错误且状态码 2xx）。
     */
    public boolean isSuccessful() {
        return errorMessage == null && statusCode >= 200 && statusCode < 300;
    }

    /**
     * 获取指定响应头（忽略大小写）。
     */
    public String header(String name) {
        return headers.get(name);
    }

    /**
     * 获取指定响应头的所有值（逗号分隔时拆分）。
     */
    public List<String> headerValues(String name) {
        String value = headers.get(name);
        if (value == null) {
            return Collections.emptyList();
        }
        return List.of(value.split(",\\s*"));
    }

    /**
     * 从 OkHttp Headers 转换为不可变 Map。
     */
    static Map<String, String> toHeaderMap(okhttp3.Headers okHeaders) {
        if (okHeaders == null || okHeaders.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>(okHeaders.size());
        for (int i = 0; i < okHeaders.size(); i++) {
            map.put(okHeaders.name(i).toLowerCase(Locale.ROOT), okHeaders.value(i));
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        if (errorMessage != null) {
            return "HttpResponse{error=" + errorMessage + "}";
        }
        return "HttpResponse{status=" + statusCode + ", body=" + (body != null ? body.length() + " chars" : "null") + "}";
    }
}
