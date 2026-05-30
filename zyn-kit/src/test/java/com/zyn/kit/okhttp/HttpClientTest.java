package com.zyn.kit.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientTest {

    private MockWebServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new HttpClient(new OkHttpClient(), false);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private String baseUrl() {
        return server.url("/").toString();
    }

    // ==================== GET ====================

    @Test
    void get_simple() {
        server.enqueue(new MockResponse().setBody("hello").setHeader("Content-Type", "text/plain"));

        HttpResponse resp = client.get(baseUrl() + "greet");

        assertTrue(resp.isSuccessful());
        assertEquals(200, resp.statusCode());
        assertEquals("hello", resp.body());
    }

    @Test
    void get_withParams() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"q\":\"test\"}").setHeader("Content-Type", "application/json"));

        HttpResponse resp = client.get(baseUrl() + "search", Map.of("q", "test", "page", "1"));

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getPath().contains("q=test"));
        assertTrue(req.getPath().contains("page=1"));
    }

    @Test
    void get_withHeaders() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.get(baseUrl() + "api", Map.of("X-Token", "abc123"), null);

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("abc123", req.getHeader("X-Token"));
    }

    // ==================== POST JSON ====================

    @Test
    void postJson() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"id\":1}").setHeader("Content-Type", "application/json"));

        HttpResponse resp = client.postJson(baseUrl() + "users", "{\"name\":\"test\"}");

        assertTrue(resp.isSuccessful());
        assertEquals("{\"id\":1}", resp.body());
        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("application/json; charset=utf-8", req.getHeader("Content-Type"));
        assertEquals("{\"name\":\"test\"}", req.getBody().readUtf8());
    }

    @Test
    void postJson_withHeaders() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.postJson(baseUrl() + "api", Map.of("X-Token", "tok"), "{\"data\":1}");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("tok", req.getHeader("X-Token"));
    }

    // ==================== POST XML ====================

    @Test
    void postXml() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.postXml(baseUrl() + "xml", "<root><name>test</name></root>");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getHeader("Content-Type").contains("xml"));
        assertEquals("<root><name>test</name></root>", req.getBody().readUtf8());
    }

    // ==================== POST PlainText ====================

    @Test
    void postPlainText() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.postPlainText(baseUrl() + "text", "hello world");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getHeader("Content-Type").contains("text/plain"));
        assertEquals("hello world", req.getBody().readUtf8());
    }

    // ==================== POST Form ====================

    @Test
    void postForm() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.postForm(baseUrl() + "login", Map.of("user", "admin", "pass", "123"));

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getHeader("Content-Type").contains("application/x-www-form-urlencoded"));
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("user=admin"));
        assertTrue(body.contains("pass=123"));
    }

    // ==================== POST Multipart ====================

    @Test
    void postFormData() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"uploaded\":true}"));

        FormDataFile file = FormDataFile.ofBytes("file", "test.txt", "text/plain", "hello".getBytes());
        HttpResponse resp = client.postFormData(baseUrl() + "upload", Map.of("desc", "test file"), List.of(file));

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getHeader("Content-Type").contains("multipart/form-data"));
    }

    @Test
    void postFormData_singleFile() {
        server.enqueue(new MockResponse().setBody("ok"));

        FormDataFile file = FormDataFile.ofBytes("avatar", "a.png", "image/png", new byte[]{1, 2, 3});
        HttpResponse resp = client.postFormData(baseUrl() + "upload", null, file);

        assertTrue(resp.isSuccessful());
    }

    // ==================== POST Binary ====================

    @Test
    void postBinary_bytes() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.postBinary(baseUrl() + "binary", new byte[]{1, 2, 3, 4, 5});

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("application/octet-stream", req.getHeader("Content-Type"));
    }

    @Test
    void postBinary_emptyBytes_returnsFailure() {
        HttpResponse resp = client.postBinary(baseUrl() + "binary", new byte[0]);

        assertFalse(resp.isSuccessful());
        assertNotNull(resp.errorMessage());
    }

    @Test
    void postBinary_file() throws IOException {
        server.enqueue(new MockResponse().setBody("ok"));

        Path tmp = Files.createTempFile("test-upload", ".bin");
        Files.write(tmp, "file-content".getBytes());
        HttpResponse resp = client.postBinary(baseUrl() + "upload-file", tmp.toAbsolutePath().toString());

        assertTrue(resp.isSuccessful());
        Files.delete(tmp);
    }

    @Test
    void postBinary_invalidFile_returnsFailure() {
        HttpResponse resp = client.postBinary(baseUrl() + "upload", "/nonexistent/file.bin");

        assertFalse(resp.isSuccessful());
        assertTrue(resp.errorMessage().contains("not a file"));
    }

    // ==================== PUT ====================

    @Test
    void putJson() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"updated\":true}").setHeader("Content-Type", "application/json"));

        HttpResponse resp = client.putJson(baseUrl() + "users/1", "{\"name\":\"new\"}");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("PUT", req.getMethod());
        assertEquals("{\"name\":\"new\"}", req.getBody().readUtf8());
    }

    // ==================== PATCH ====================

    @Test
    void patchJson() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.patchJson(baseUrl() + "users/1", "{\"name\":\"patched\"}");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("PATCH", req.getMethod());
        assertEquals("{\"name\":\"patched\"}", req.getBody().readUtf8());
    }

    // ==================== DELETE ====================

    @Test
    void delete_simple() throws Exception {
        server.enqueue(new MockResponse().setBody("deleted"));

        HttpResponse resp = client.delete(baseUrl() + "users/1");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
    }

    @Test
    void delete_withParams() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.delete(baseUrl() + "items", Map.of("id", "42"));

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getPath().contains("id=42"));
    }

    @Test
    void deleteJson() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));

        HttpResponse resp = client.deleteJson(baseUrl() + "bulk", "{\"ids\":[1,2,3]}");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertNotNull(req.getHeader("Content-Type"));
    }

    // ==================== HEAD ====================

    @Test
    void head() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setHeader("X-Custom", "value"));

        HttpResponse resp = client.head(baseUrl() + "resource");

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("HEAD", req.getMethod());
    }

    // ==================== Execute Bytes ====================

    @Test
    void executeBytes_returnsBinary() {
        byte[] binary = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header
        server.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(binary))
                .setHeader("Content-Type", "image/png"));

        HttpResponse resp = client.executeBytes(
                new okhttp3.Request.Builder().url(baseUrl() + "image").build());

        assertTrue(resp.isSuccessful());
        assertNotNull(resp.bodyBytes());
        assertArrayEquals(binary, resp.bodyBytes());
    }

    @Test
    void executeBytes_nullBody() {
        server.enqueue(new MockResponse().setResponseCode(204));

        HttpResponse resp = client.executeBytes(
                new okhttp3.Request.Builder().url(baseUrl() + "noop").build());

        assertTrue(resp.isSuccessful());
        assertTrue(resp.bodyBytes() == null || resp.bodyBytes().length == 0);
    }

    // ==================== Download (Streaming) ====================

    @Test
    void download_toFile() throws IOException {
        byte[] content = "file-content-here".getBytes(StandardCharsets.UTF_8);
        server.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(content))
                .setHeader("Content-Length", content.length));

        Path target = Files.createTempFile("download-test", ".txt");
        long downloaded = client.download(
                new okhttp3.Request.Builder().url(baseUrl() + "download").build(), target);

        assertEquals(content.length, downloaded);
        assertArrayEquals(content, Files.readAllBytes(target));
        Files.delete(target);
    }

    @Test
    void download_serverError_returnsNegative() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));

        Path target = Files.createTempFile("download-fail", ".txt");
        long downloaded = client.download(
                new okhttp3.Request.Builder().url(baseUrl() + "download").build(), target);

        assertEquals(-1, downloaded);
        Files.delete(target);
    }

    // ==================== Upload Stream ====================

    @Test
    void uploadStream() throws Exception {
        server.enqueue(new MockResponse().setBody("uploaded"));

        byte[] data = "stream-content".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        HttpResponse resp = client.uploadStream(
                baseUrl() + "upload-stream",
                Map.of("Authorization", "Bearer tok"),
                "doc.txt",
                bis,
                "text/plain",
                data.length);

        assertTrue(resp.isSuccessful());
        RecordedRequest req = server.takeRequest();
        assertEquals("Bearer tok", req.getHeader("Authorization"));
        assertEquals("text/plain", req.getHeader("Content-Type"));
    }

    // ==================== Stream (SSE) ====================

    @Test
    void stream_sseEvents() {
        StringBuilder sseBody = new StringBuilder();
        sseBody.append("data: event1\n\n");
        sseBody.append("data: event2\n\n");
        sseBody.append("data: event3\n\n");

        server.enqueue(new MockResponse()
                .setBody(sseBody.toString())
                .setHeader("Content-Type", "text/event-stream"));

        CopyOnWriteArrayList<String> lines = new CopyOnWriteArrayList<>();
        client.stream(
                new okhttp3.Request.Builder().url(baseUrl() + "events").build(),
                lines::add);

        assertTrue(lines.size() >= 3);
        assertTrue(lines.stream().anyMatch(l -> l.contains("event1")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("event2")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("event3")));
    }

    // ==================== Error Handling ====================

    @Test
    void execute_serverError() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("internal error"));

        HttpResponse resp = client.get(baseUrl() + "error");

        assertFalse(resp.isSuccessful());
        assertEquals(500, resp.statusCode());
        assertEquals("internal error", resp.body());
    }

    @Test
    void execute_404() {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("not found"));

        HttpResponse resp = client.get(baseUrl() + "missing");

        assertFalse(resp.isSuccessful());
        assertEquals(404, resp.statusCode());
    }

    @Test
    void execute_throwOnHttpError() {
        HttpClient throwingClient = new HttpClient(new OkHttpClient(), true);
        server.enqueue(new MockResponse().setResponseCode(500).setBody("error"));

        assertThrows(HttpClientException.class,
                () -> throwingClient.get(baseUrl() + "fail"));
    }

    @Test
    void execute_nullBody() {
        server.enqueue(new MockResponse().setResponseCode(204));

        HttpResponse resp = client.get(baseUrl() + "no-content");

        assertTrue(resp.isSuccessful());
        assertTrue(resp.body() == null || resp.body().isEmpty());
    }

    // ==================== HttpResponse ====================

    @Test
    void httpResponse_header_caseInsensitive() {
        server.enqueue(new MockResponse()
                .setBody("ok")
                .setHeader("X-Custom-Header", "value123"));

        HttpResponse resp = client.get(baseUrl() + "headers");

        assertEquals("value123", resp.header("x-custom-header"));
        assertEquals("value123", resp.header("X-Custom-Header"));
    }

    @Test
    void httpResponse_failure_hasErrorMessage() {
        HttpResponse resp = HttpResponse.failure("connection refused");

        assertEquals(-1, resp.statusCode());
        assertEquals("connection refused", resp.errorMessage());
        assertFalse(resp.isSuccessful());
    }
}
