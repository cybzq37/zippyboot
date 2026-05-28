package com.zippyboot.kit.okhttp;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * OkHttp 客户端封装。
 * <p>
 * 统一返回 {@link HttpResponse}，通过 {@link HttpResponse#body()} 等方法获取响应体。
 *
 * <pre>
 * // 注入或创建
 * HttpClient client = new HttpClient();
 *
 * // GET
 * HttpResponse resp = client.get("https://api.example.com/users", Map.of("page", "1"));
 *
 * // POST JSON
 * HttpResponse resp = client.postJson(url, jsonBody);
 *
 * // 判断结果
 * if (resp.isSuccessful()) {
 *     String data = resp.body();
 * }
 * </pre>
 */
public class HttpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);
    private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_JSON_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_XML_CONTENT_TYPE = "application/xml";
    private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    private final OkHttpClient client;
    private final boolean throwOnHttpError;
    private final long maxResponseBytes;

    public HttpClient() {
        this(new OkHttpClient.Builder()
                .connectTimeout(HttpClientProperties.DEFAULT_CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(HttpClientProperties.DEFAULT_READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(HttpClientProperties.DEFAULT_WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .callTimeout(HttpClientProperties.DEFAULT_CALL_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .build(), false, HttpClientProperties.DEFAULT_MAX_RESPONSE_BYTES);
    }

    public HttpClient(OkHttpClient client) {
        this(client, false);
    }

    public HttpClient(OkHttpClient client, boolean throwOnHttpError) {
        this(client, throwOnHttpError, HttpClientProperties.DEFAULT_MAX_RESPONSE_BYTES);
    }

    public HttpClient(OkHttpClient client, boolean throwOnHttpError, long maxResponseBytes) {
        if (client == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null");
        }
        this.client = client;
        this.throwOnHttpError = throwOnHttpError;
        this.maxResponseBytes = maxResponseBytes;
    }

    // ==================== GET ====================

    public HttpResponse get(String url) {
        return get(url, null, null);
    }

    public HttpResponse get(String url, Map<String, String> params) {
        return get(url, null, params);
    }

    public HttpResponse get(String url, Map<String, String> headers, Map<String, String> params) {
        String targetUrl = (params != null && !params.isEmpty()) ? buildUrl(url, params) : url;
        Request request = new Request.Builder()
                .headers(buildHeaders(headers))
                .url(targetUrl)
                .build();
        return execute(request);
    }

    // ==================== POST JSON ====================

    public HttpResponse postJson(String url, String json) {
        return postJson(url, null, json);
    }

    public HttpResponse postJson(String url, Map<String, String> headers, String json) {
        return postRawData(url, headers, DEFAULT_JSON_CONTENT_TYPE, json);
    }

    // ==================== POST XML ====================

    public HttpResponse postXml(String url, String body) {
        return postXml(url, null, body);
    }

    public HttpResponse postXml(String url, Map<String, String> headers, String body) {
        return postRawData(url, headers, DEFAULT_XML_CONTENT_TYPE, body);
    }

    // ==================== POST Text ====================

    public HttpResponse postPlainText(String url, String text) {
        return postPlainText(url, null, text);
    }

    public HttpResponse postPlainText(String url, Map<String, String> headers, String text) {
        return postRawData(url, headers, DEFAULT_TEXT_CONTENT_TYPE, text);
    }

    // ==================== POST Raw ====================

    public HttpResponse postRawData(String url, Map<String, String> headers, String contentType, String text) {
        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType == null) {
            throw new IllegalArgumentException("Invalid contentType: " + contentType);
        }
        RequestBody requestBody = RequestBody.create(text != null ? text : "", mediaType);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .method("POST", requestBody)
                .build();
        return execute(request);
    }

    // ==================== POST Form ====================

    public HttpResponse postForm(String url, Map<String, String> params) {
        return postForm(url, null, params);
    }

    public HttpResponse postForm(String url, Map<String, String> headers, Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null) {
            params.forEach(builder::add);
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(builder.build())
                .build();
        return execute(request);
    }

    // ==================== POST Multipart ====================

    public HttpResponse postFormData(String url, Map<String, String> fields, FormDataFile file) {
        return postFormData(url, null, fields, file);
    }

    public HttpResponse postFormData(String url, Map<String, String> headers,
                                     Map<String, String> fields, FormDataFile file) {
        return postFormData(url, headers, fields, file != null ? List.of(file) : List.of());
    }

    public HttpResponse postFormData(String url, Map<String, String> fields, List<FormDataFile> files) {
        return postFormData(url, null, fields, files);
    }

    public HttpResponse postFormData(String url, Map<String, String> headers,
                                     Map<String, String> fields, List<FormDataFile> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (files != null) {
            for (FormDataFile item : files) {
                if (item == null) continue;
                MediaType mediaType = MediaType.parse(item.contentType());
                if (mediaType == null) {
                    throw new IllegalArgumentException("Invalid file contentType: " + item.contentType());
                }
                if (item.fileData() != null && item.fileData().length > 0) {
                    builder.addFormDataPart(item.fieldName(), item.fileName(),
                            RequestBody.create(item.fileData(), mediaType));
                } else if (item.file() != null) {
                    builder.addFormDataPart(item.fieldName(), item.fileName(),
                            RequestBody.create(item.file(), mediaType));
                }
            }
        }
        if (fields != null) {
            fields.forEach(builder::addFormDataPart);
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(builder.build())
                .build();
        return execute(request);
    }

    // ==================== POST Binary ====================

    public HttpResponse postBinary(String url, byte[] data) {
        return postBinary(url, null, data);
    }

    public HttpResponse postBinary(String url, Map<String, String> headers, byte[] data) {
        if (data == null || data.length == 0) {
            return HttpResponse.failure("The send data is empty.");
        }
        RequestBody body = RequestBody.create(data, MediaType.parse(DEFAULT_BINARY_CONTENT_TYPE));
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(body)
                .build();
        return execute(request);
    }

    public HttpResponse postBinary(String url, String filePath) {
        return postBinary(url, null, filePath);
    }

    public HttpResponse postBinary(String url, Map<String, String> headers, String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            return HttpResponse.failure("The filePath is not a file: " + filePath);
        }
        RequestBody body = RequestBody.create(file, MediaType.parse(DEFAULT_BINARY_CONTENT_TYPE));
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(body)
                .build();
        return execute(request);
    }

    // ==================== PUT ====================

    public HttpResponse putJson(String url, String json) {
        return putJson(url, null, json);
    }

    public HttpResponse putJson(String url, Map<String, String> headers, String json) {
        return putRawData(url, headers, DEFAULT_JSON_CONTENT_TYPE, json);
    }

    public HttpResponse putRawData(String url, Map<String, String> headers, String contentType, String text) {
        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType == null) {
            throw new IllegalArgumentException("Invalid contentType: " + contentType);
        }
        RequestBody requestBody = RequestBody.create(text != null ? text : "", mediaType);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .method("PUT", requestBody)
                .build();
        return execute(request);
    }

    // ==================== PATCH ====================

    public HttpResponse patchJson(String url, String json) {
        return patchJson(url, null, json);
    }

    public HttpResponse patchJson(String url, Map<String, String> headers, String json) {
        return patchRawData(url, headers, DEFAULT_JSON_CONTENT_TYPE, json);
    }

    public HttpResponse patchRawData(String url, Map<String, String> headers, String contentType, String text) {
        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType == null) {
            throw new IllegalArgumentException("Invalid contentType: " + contentType);
        }
        RequestBody requestBody = RequestBody.create(text != null ? text : "", mediaType);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .method("PATCH", requestBody)
                .build();
        return execute(request);
    }

    // ==================== DELETE ====================

    public HttpResponse delete(String url) {
        return delete(url, null, null);
    }

    public HttpResponse delete(String url, Map<String, String> params) {
        return delete(url, null, params);
    }

    public HttpResponse delete(String url, Map<String, String> headers, Map<String, String> params) {
        String targetUrl = (params != null && !params.isEmpty()) ? buildUrl(url, params) : url;
        Request request = new Request.Builder()
                .headers(buildHeaders(headers))
                .url(targetUrl)
                .delete()
                .build();
        return execute(request);
    }

    public HttpResponse deleteJson(String url, String json) {
        return deleteJson(url, null, json);
    }

    public HttpResponse deleteJson(String url, Map<String, String> headers, String json) {
        MediaType mediaType = MediaType.parse(DEFAULT_JSON_CONTENT_TYPE);
        RequestBody requestBody = RequestBody.create(json != null ? json : "", mediaType);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .delete(requestBody)
                .build();
        return execute(request);
    }

    // ==================== HEAD ====================

    public HttpResponse head(String url) {
        return head(url, null);
    }

    public HttpResponse head(String url, Map<String, String> headers) {
        Request request = new Request.Builder()
                .headers(buildHeaders(headers))
                .url(url)
                .head()
                .build();
        return execute(request);
    }

    // ==================== Execute ====================

    public HttpResponse execute(Request request) {
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                return HttpResponse.success(response.code(), HttpResponse.toHeaderMap(response.headers()), null);
            }
            long contentLength = body.contentLength();
            if (contentLength > maxResponseBytes) {
                body.close();
                return HttpResponse.failure("Response body too large: " + contentLength + " bytes (max=" + maxResponseBytes + ")");
            }
            String text = body.string();
            if (text.length() > maxResponseBytes) {
                return HttpResponse.failure("Response body too large: " + text.length() + " bytes (max=" + maxResponseBytes + ")");
            }
            HttpResponse result = HttpResponse.success(
                    response.code(),
                    HttpResponse.toHeaderMap(response.headers()),
                    text
            );
            if (throwOnHttpError && !result.isSuccessful()) {
                throw new HttpClientException("HTTP request failed, status=" + result.statusCode() + ", body=" + result.body());
            }
            return result;
        } catch (IOException e) {
            if (throwOnHttpError) {
                throw new HttpClientException("HTTP request failed due to IO exception", e);
            }
            log.error("http client call exception", e);
            return HttpResponse.failure(e.getMessage());
        }
    }

    // ==================== Internal ====================

    private String buildUrl(String baseUrl, Map<String, String> params) {
        HttpUrl parsed = HttpUrl.parse(baseUrl);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid url: " + baseUrl);
        }
        HttpUrl.Builder builder = parsed.newBuilder();
        if (params != null) {
            params.forEach(builder::addQueryParameter);
        }
        return builder.build().toString();
    }

    private Headers buildHeaders(Map<String, String> headers) {
        Headers.Builder builder = new Headers.Builder();
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) {
                    builder.add(k, v);
                }
            });
        }
        return builder.build();
    }
}
