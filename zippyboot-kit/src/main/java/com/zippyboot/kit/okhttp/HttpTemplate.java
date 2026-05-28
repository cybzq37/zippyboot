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
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 封装客户端。
 * <p>
 * 推荐通过 Spring 注入 {@code HttpTemplate} Bean 使用，
 * 也支持 {@link #getInstance()} 单例方式（已废弃）。
 *
 * @author lichunqing
 */
public class HttpTemplate {

    private static final Logger log = LoggerFactory.getLogger(HttpTemplate.class);
    private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_JSON_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_XML_CONTENT_TYPE = "application/xml";
    private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    private final OkHttpClient client;
    private final boolean throwOnHttpError;

    private static volatile HttpTemplate instance;

    private HttpTemplate() {
        this(buildDefaultClient(), false);
    }

    public HttpTemplate(OkHttpClient client) {
        this(client, false);
    }

    public HttpTemplate(OkHttpClient client, boolean throwOnHttpError) {
        if (client == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null");
        }
        this.client = client;
        this.throwOnHttpError = throwOnHttpError;
    }

    private static OkHttpClient buildDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(HttpClientProperties.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HttpClientProperties.DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HttpClientProperties.DEFAULT_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(HttpClientProperties.DEFAULT_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    /**
     * @deprecated Prefer injecting HttpTemplate bean from Spring context.
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    public static HttpTemplate getInstance() {
        if (instance == null) {
            synchronized (HttpTemplate.class) {
                if (instance == null) {
                    instance = new HttpTemplate();
                }
            }
        }
        return instance;
    }

    public String get(String url, Map<String, String> params) {
        return this.getForResult(url, null, params).getBody();
    }

    public String get(String url, Map<String, String> headers, Map<String, String> params) {
        return this.getForResult(url, headers, params).getBody();
    }

    public HttpResponse getForResult(String url, Map<String, String> headers, Map<String, String> params) {
        String newUrl;
        if (params != null && !params.isEmpty()) {
            newUrl = buildUrlWithQueryParams(url, params);
        } else {
            newUrl = url;
        }
        Request request = new Request.Builder()
                .headers(buildHeaders(headers))
                .url(newUrl)
                .build();
        return execute(request);
    }

    public String postJson(String url, Map<String, String> headers, String json) {
        return this.postRawDataForResult(url, headers, DEFAULT_JSON_CONTENT_TYPE, json).getBody();
    }

    public HttpResponse postJsonForResult(String url, Map<String, String> headers, String json) {
        return this.postRawDataForResult(url, headers, DEFAULT_JSON_CONTENT_TYPE, json);
    }

    public String postXml(String url, Map<String, String> headers, String body) {
        return this.postRawDataForResult(url, headers, DEFAULT_XML_CONTENT_TYPE, body).getBody();
    }

    public HttpResponse postXmlForResult(String url, Map<String, String> headers, String body) {
        return this.postRawDataForResult(url, headers, DEFAULT_XML_CONTENT_TYPE, body);
    }

    public String postPlainText(String url, Map<String, String> headers, String text) {
        return this.postRawDataForResult(url, headers, DEFAULT_TEXT_CONTENT_TYPE, text).getBody();
    }

    public HttpResponse postPlainTextForResult(String url, Map<String, String> headers, String text) {
        return this.postRawDataForResult(url, headers, DEFAULT_TEXT_CONTENT_TYPE, text);
    }

    public String postRawData(String url, Map<String, String> headers, String contentType, String text) {
        return this.postRawDataForResult(url, headers, contentType, text).getBody();
    }

    public HttpResponse postRawDataForResult(String url, Map<String, String> headers, String contentType, String text) {
        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType == null) {
            throw new IllegalArgumentException("Invalid contentType: " + contentType);
        }

        RequestBody requestBody = RequestBody.create(mediaType, text == null ? "" : text);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .method("POST", requestBody)
                .build();
        return execute(request);
    }

    public String postFormUrlEncoded(String url, Map<String, String> headers, Map<String, String> params) {
        return postFormUrlEncodedForResult(url, headers, params).getBody();
    }

    public HttpResponse postFormUrlEncodedForResult(String url, Map<String, String> headers, Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(builder.build())
                .build();

        return execute(request);
    }

    /**
     * @deprecated use postFormUrlEncoded instead.
     */
    @Deprecated
    public String postFormUrlencoded(String url, Map<String, String> headers, Map<String, String> params) {
        return postFormUrlEncoded(url, headers, params);
    }

    public String postFormData(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               FormDataFile formDataFile) {
        return this.postFormDataForResult(url, headers, fields, formDataFile).getBody();
    }

    public HttpResponse postFormDataForResult(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               FormDataFile formDataFile) {
        return this.postFormDataForResult(url, headers, fields, List.of(formDataFile));
    }

    public String postFormData(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               List<FormDataFile> formDataFiles) {
        return this.postFormDataForResult(url, headers, fields, formDataFiles).getBody();
    }

    public HttpResponse postFormDataForResult(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               List<FormDataFile> formDataFiles) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        if (formDataFiles != null) {
            for (FormDataFile item : formDataFiles) {
                if (item == null) {
                    continue;
                }
                MediaType mediaType = MediaType.parse(item.getContentType());
                if (mediaType == null) {
                    throw new IllegalArgumentException("Invalid file contentType: " + item.getContentType());
                }
                if (item.getFileData() != null && item.getFileData().length > 0) {
                    builder.addFormDataPart(item.getFieldName(), item.getFileName(),
                            RequestBody.create(mediaType, item.getFileData()));
                } else if (item.getFile() != null) {
                    builder.addFormDataPart(item.getFieldName(), item.getFileName(),
                            RequestBody.create(mediaType, item.getFile()));
                }
            }
        }

        if (fields != null && !fields.isEmpty()) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(builder.build())
                .build();

        return execute(request);
    }

    public String postBinary(String url, Map<String, String> headers, byte[] data) {
        return postBinaryForResult(url, headers, data).getBody();
    }

    public HttpResponse postBinaryForResult(String url, Map<String, String> headers, byte[] data) {
        if (data == null || data.length == 0) {
            return HttpResponse.failure("The send data is empty.");
        }
        RequestBody body = RequestBody.create(MediaType.parse(DEFAULT_BINARY_CONTENT_TYPE), data);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(body)
                .build();
        return execute(request);
    }

    public String postBinary(String url, Map<String, String> headers, String filePath) {
        return postBinaryForResult(url, headers, filePath).getBody();
    }

    public HttpResponse postBinaryForResult(String url, Map<String, String> headers, String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            return HttpResponse.failure("The filePath is not a file: " + filePath);
        }
        RequestBody body = RequestBody.create(MediaType.parse(DEFAULT_BINARY_CONTENT_TYPE), file);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(body)
                .build();
        return execute(request);
    }

    private String buildUrlWithQueryParams(String baseUrl, Map<String, String> params) {
        HttpUrl parsed = HttpUrl.parse(baseUrl);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid url: " + baseUrl);
        }

        HttpUrl.Builder builder = parsed.newBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build().toString();
    }

    private Headers buildHeaders(Map<String, String> headers) {
        Headers.Builder builder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) {
            return builder.build();
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public HttpResponse execute(Request request) {
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            String text = body == null ? null : body.string();
            HttpResponse result = HttpResponse.success(response.code(), response.headers(), text);
            if (throwOnHttpError && !result.isSuccessful()) {
                throw new HttpClientException("HTTP request failed, status=" + result.getStatusCode() + ", body=" + result.getBody());
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
}
