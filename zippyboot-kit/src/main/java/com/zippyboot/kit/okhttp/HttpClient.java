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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 可以单例模式调用 HttpClient.getInstance().xxx 方式
 * 也可以新创建对象
 * @lichunqing
 */
public class HttpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);
    private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_JSON_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_XML_CONTENT_TYPE = "application/xml";
    private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";
    private static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = HttpClientProperties.DEFAULT_CONNECT_TIMEOUT_SECONDS;
    private static final long DEFAULT_READ_TIMEOUT_SECONDS = HttpClientProperties.DEFAULT_READ_TIMEOUT_SECONDS;
    private static final long DEFAULT_WRITE_TIMEOUT_SECONDS = HttpClientProperties.DEFAULT_WRITE_TIMEOUT_SECONDS;
    private static final long DEFAULT_CALL_TIMEOUT_SECONDS = HttpClientProperties.DEFAULT_CALL_TIMEOUT_SECONDS;

    private final OkHttpClient client;
    private final boolean throwOnHttpError;

    private static volatile HttpClient instance;

    private HttpClient() {
        this(buildDefaultClient(), false);
    }

    public HttpClient(OkHttpClient client) {
        this(client, false);
    }

    public HttpClient(OkHttpClient client, boolean throwOnHttpError) {
        if (client == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null");
        }
        this.client = client;
        this.throwOnHttpError = throwOnHttpError;
    }

    private static OkHttpClient buildDefaultClient() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();

//      httpBuilder.addInterceptor(new HttpLogInterceptor());   // 拦截器打印日志

        return httpBuilder
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(DEFAULT_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    /**
     * @deprecated Prefer injecting HttpClient bean from Spring context.
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    public static HttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClient.class) {
                if (instance == null) {
                    instance = new HttpClient();
                }
            }
        }
        return instance;
    }

    public String get(String url, Map<String,String> params) {
        return this.getForResult(url, null, params).getBody();
    }

    public String get(String url, Map<String, String> headers, Map<String,String> params) {
        return this.getForResult(url, headers, params).getBody();
    }

    public HttpResult getForResult(String url, Map<String, String> headers, Map<String, String> params) {
        String newUrl;
        if(params != null && !params.isEmpty()){
            newUrl = buildUrlWithQueryParams(url, params);
        }else {
            newUrl = url;
        }
        Request request = new Request.Builder()
                .headers(buildHeaders(headers))
                .url(newUrl)
                .build();
        return execute(request);
    }

    public String postJson(String url, Map<String, String> headers, String json){
        return this.postRawDataForResult(url, headers, DEFAULT_JSON_CONTENT_TYPE, json).getBody();
    }

    public HttpResult postJsonForResult(String url, Map<String, String> headers, String json){
        return this.postRawDataForResult(url, headers, DEFAULT_JSON_CONTENT_TYPE, json);
    }

    public String postXml(String url, Map<String, String> headers, String json){
        return this.postRawDataForResult(url, headers, DEFAULT_XML_CONTENT_TYPE, json).getBody();
    }

    public HttpResult postXmlForResult(String url, Map<String, String> headers, String json){
        return this.postRawDataForResult(url, headers, DEFAULT_XML_CONTENT_TYPE, json);
    }

    public String postPlainText(String url, Map<String, String> headers, String json){
        return this.postRawDataForResult(url, headers, DEFAULT_TEXT_CONTENT_TYPE, json).getBody();
    }

    public HttpResult postPlainTextForResult(String url, Map<String, String> headers, String text){
        return this.postRawDataForResult(url, headers, DEFAULT_TEXT_CONTENT_TYPE, text);
    }

    public String postRawData(String url, Map<String, String> headers, String contentType, String text){
        return this.postRawDataForResult(url, headers, contentType, text).getBody();
    }

    public HttpResult postRawDataForResult(String url, Map<String, String> headers, String contentType, String text){
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

    public String postFormUrlEncoded(String url, Map<String, String> headers, Map<String, String> params){
        return postFormUrlEncodedForResult(url, headers, params).getBody();
    }

    public HttpResult postFormUrlEncodedForResult(String url, Map<String, String> headers, Map<String, String> params){
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
    public String postFormUrlencoded(String url, Map<String, String> headers, Map<String, String> params){
        return postFormUrlEncoded(url, headers, params);
    }

    public String postFormData(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               HttpFormDataFile formDataFile) {
        return this.postFormDataForResult(url, headers, fields, formDataFile).getBody();
    }

    public HttpResult postFormDataForResult(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               HttpFormDataFile formDataFile) {
        List<HttpFormDataFile> formDataFiles = new ArrayList<>(1);
        formDataFiles.add(formDataFile);
        return this.postFormDataForResult(url, headers, fields, formDataFiles);
    }

    public String postFormData(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               List<HttpFormDataFile> formDataFiles) {
        return this.postFormDataForResult(url, headers, fields, formDataFiles).getBody();
    }

    public HttpResult postFormDataForResult(String url,
                               Map<String, String> headers,
                               Map<String, String> fields,
                               List<HttpFormDataFile> formDataFiles) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        if (formDataFiles != null) {
            for(HttpFormDataFile item : formDataFiles) {
                if (item == null) {
                    continue;
                }
                if(item.getFileData() != null && item.getFileData().length > 0) {
                    MediaType mediaType = MediaType.parse(item.getContentType());
                    if (mediaType == null) {
                        throw new IllegalArgumentException("Invalid file contentType: " + item.getContentType());
                    }
                    builder.addFormDataPart(item.getFieldName(), item.getFileName(),
                            RequestBody.create(mediaType, item.getFileData()));
                }
                if(item.getFile() != null) {
                    MediaType mediaType = MediaType.parse(item.getContentType());
                    if (mediaType == null) {
                        throw new IllegalArgumentException("Invalid file contentType: " + item.getContentType());
                    }
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

    public HttpResult postBinaryForResult(String url, Map<String, String> headers, byte[] data) {
        RequestBody body;
        if (data == null || data.length == 0) {
            return HttpResult.failure("The send data is empty.");
        } else {
            MediaType mediaType = MediaType.parse(DEFAULT_BINARY_CONTENT_TYPE);
            if (mediaType == null) {
                throw new IllegalStateException("Invalid default content type");
            }
            body = RequestBody.create(mediaType, data);
        }

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

    public HttpResult postBinaryForResult(String url, Map<String, String> headers, String filePath) {
        RequestBody body;
        File file = new File(filePath);
        if (!file.isFile()) {
            return HttpResult.failure("The filePath is not a file: " + filePath);
        } else {
            MediaType mediaType = MediaType.parse(DEFAULT_BINARY_CONTENT_TYPE);
            if (mediaType == null) {
                throw new IllegalStateException("Invalid default content type");
            }
            body = RequestBody.create(mediaType, file);
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headers))
                .post(body)
                .build();

        return execute(request);
    }


    public String buildUrlWithQueryParams(String baseUrl, Map<String, String> params) {
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

    /**
     * @deprecated use buildUrlWithQueryParams instead.
     */
    @Deprecated
    public String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        return buildUrlWithQueryParams(baseUrl, params);
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

    public HttpResult execute(Request q) {
        try (Response response = client.newCall(q).execute()) {
            ResponseBody body = response.body();
            String text = body == null ? null : body.string();
            HttpResult result = HttpResult.success(response.code(), response.headers(), text);
            if (throwOnHttpError && !result.isSuccessful()) {
                throw new HttpClientException("HTTP request failed, status=" + result.getStatusCode() + ", body=" + result.getBody());
            }
            return result;
        } catch (IOException e) {
            if (throwOnHttpError) {
                throw new HttpClientException("HTTP request failed due to IO exception", e);
            }
            log.error("http client call exception", e);
            return HttpResult.failure(e.getMessage());
        }
    }
}
