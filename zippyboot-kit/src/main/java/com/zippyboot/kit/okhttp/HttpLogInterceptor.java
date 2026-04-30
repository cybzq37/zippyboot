package com.zippyboot.kit.okhttp;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Headers;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * okhttp 拦截器
 */
public class HttpLogInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpLogInterceptor.class);

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final long FULL_BODY_LOG_MAX_BYTES = 2048;
    private static final int FULL_BODY_LOG_MAX_CHARS = 2048;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!log.isDebugEnabled()) {
            return chain.proceed(request);
        }

        String requestBodySummary = summarizeRequestBody(request);
        log.debug("[HTTP-REQ] {} {}{}headers={}{}body={}",
                request.method(),
                request.url(),
                System.lineSeparator(),
                request.headers(),
                System.lineSeparator(),
                requestBodySummary);

        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            log.debug("[HTTP-RESP] code={} url={} tookMs={}{}headers={}{}body=<null>",
                    response.code(),
                    response.request().url(),
                    tookMs,
                    System.lineSeparator(),
                    response.headers(),
                    System.lineSeparator());
            return response;
        }

        String responseBodySummary = summarizeResponseBody(response, responseBody);
        log.debug("[HTTP-RESP] code={} url={} tookMs={}ms{}headers={}{}body={}",
                response.code(),
                response.request().url(),
                tookMs,
                System.lineSeparator(),
                response.headers(),
                System.lineSeparator(),
                responseBodySummary);

        return response;
    }

    private String summarizeRequestBody(Request request) {
        RequestBody body = request.body();
        if (body == null) {
            return "<empty>";
        }

        MediaType mediaType = body.contentType();
        long contentLength;
        try {
            contentLength = body.contentLength();
        } catch (IOException ex) {
            contentLength = -1;
        }

        if (isFileLike(mediaType, request.headers())) {
            return "<file-like request body, size=" + formatLength(contentLength) + ">";
        }
        if (!isTextLike(mediaType)) {
            return "<binary request body, size=" + formatLength(contentLength) + ">";
        }
        if (contentLength > FULL_BODY_LOG_MAX_BYTES) {
            return "<text request body too large, size=" + contentLength + " bytes>";
        }

        try {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            if (buffer.size() > FULL_BODY_LOG_MAX_BYTES) {
                return "<text request body too large, size=" + buffer.size() + " bytes>";
            }

            Charset charset = mediaType != null ? mediaType.charset(UTF8) : UTF8;
            String text = buffer.readString(charset == null ? UTF8 : charset);
            if (text.length() > FULL_BODY_LOG_MAX_CHARS) {
                return "<text request body too long, chars=" + text.length() + ">";
            }
            return text;
        } catch (IOException ex) {
            return "<request body read failed: " + ex.getMessage() + ">";
        }
    }

    private String summarizeResponseBody(Response response, ResponseBody responseBody) {
        MediaType mediaType = responseBody.contentType();
        long contentLength = responseBody.contentLength();
        Headers headers = response.headers();

        if (isFileLike(mediaType, headers)) {
            return "<file-like response body, size=" + formatLength(contentLength) + ">";
        }
        if (!isTextLike(mediaType)) {
            return "<binary response body, size=" + formatLength(contentLength) + ">";
        }
        if (contentLength > FULL_BODY_LOG_MAX_BYTES) {
            return "<text response body too large, size=" + contentLength + " bytes>";
        }

        try {
            String text = response.peekBody(FULL_BODY_LOG_MAX_BYTES).string();
            if (text.length() > FULL_BODY_LOG_MAX_CHARS) {
                return "<text response body too long, chars=" + text.length() + ">";
            }
            return text;
        } catch (IOException ex) {
            return "<response body peek failed: " + ex.getMessage() + ">";
        }
    }

    private boolean isTextLike(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }

        String type = mediaType.type();
        String subtype = mediaType.subtype();
        if ("text".equalsIgnoreCase(type)) {
            return true;
        }
        if (subtype == null) {
            return false;
        }

        String normalized = subtype.toLowerCase();
        return normalized.contains("json")
                || normalized.contains("xml")
                || normalized.contains("x-www-form-urlencoded")
                || normalized.contains("graphql")
                || normalized.contains("javascript")
                || normalized.contains("html")
                || normalized.contains("plain");
    }

    private boolean isFileLike(MediaType mediaType, Headers headers) {
        String disposition = headers.get("Content-Disposition");
        if (disposition != null && disposition.toLowerCase().contains("attachment")) {
            return true;
        }
        if (mediaType == null) {
            return false;
        }

        String type = mediaType.type();
        String subtype = mediaType.subtype();
        if ("image".equalsIgnoreCase(type)
                || "video".equalsIgnoreCase(type)
                || "audio".equalsIgnoreCase(type)
                || "application".equalsIgnoreCase(type) && "octet-stream".equalsIgnoreCase(subtype)) {
            return true;
        }

        if (subtype == null) {
            return false;
        }

        String normalized = subtype.toLowerCase();
        return normalized.contains("zip")
                || normalized.contains("pdf")
                || normalized.contains("msword")
                || normalized.contains("excel")
                || normalized.contains("wordprocessingml")
                || normalized.contains("spreadsheetml");
    }

    private String formatLength(long len) {
        return len < 0 ? "unknown" : len + " bytes";
    }
}
