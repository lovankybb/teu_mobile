package com.teumobile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.CloudinaryProperties;
import com.teumobile.dto.CloudinarySignatureResponseDto;
import com.teumobile.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CloudinaryServiceTest {

    private CloudinaryProperties properties;
    private ObjectMapper objectMapper;
    private HttpClient mockHttpClient;
    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        properties = new CloudinaryProperties();
        properties.setCloudName("teu-cloud");
        properties.setApiKey("test-api-key");
        properties.setApiSecret("test-api-secret");
        properties.setFolder("teu-mobile/avatars");

        objectMapper = new ObjectMapper();
        mockHttpClient = Mockito.mock(HttpClient.class);

        cloudinaryService = new CloudinaryServiceImpl(properties, objectMapper, mockHttpClient);
    }

    @Test
    @DisplayName("Tạo chữ ký avatar upload thành công với đầy đủ tham số và đúng chuẩn SHA-1 Cloudinary")
    void testGenerateAvatarUploadSignatureSuccess() throws Exception {
        CloudinarySignatureResponseDto result = cloudinaryService.generateAvatarUploadSignature();

        assertNotNull(result);
        assertEquals("teu-cloud", result.getCloudName());
        assertEquals("test-api-key", result.getApiKey());
        assertEquals("teu-mobile/avatars", result.getFolder());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getSignature());
        assertEquals(40, result.getSignature().length(), "Chữ ký SHA-1 phải có đúng 40 ký tự hex");

        // Kiểm tra chữ ký khớp với chuỗi thủ công: "folder=teu-mobile/avatars&timestamp=" + timestamp + "test-api-secret"
        String expectedStringToSign = "folder=teu-mobile/avatars&timestamp=" + result.getTimestamp() + "test-api-secret";
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(expectedStringToSign.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        assertEquals(hexString.toString(), result.getSignature());
    }

    @Test
    @DisplayName("Sắp xếp tham số theo thứ tự alphabet A-Z khi tạo chữ ký")
    void testCalculateSignatureSortsAlphabetically() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", 1731920000L);
        params.put("folder", "teu-mobile/avatars");

        String signature = cloudinaryService.calculateSignature(params, "secret123");

        // 'folder' phải đứng trước 'timestamp' theo thứ tự từ điển
        String manualExpected = "folder=teu-mobile/avatars&timestamp=1731920000secret123";
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(manualExpected.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }

        assertEquals(hexString.toString(), signature);
    }

    @Test
    @DisplayName("Ném IllegalStateException khi thiếu cloud-name, api-key hoặc api-secret")
    void testValidateConfigMissing() {
        properties.setCloudName(null);
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () -> cloudinaryService.generateAvatarUploadSignature());
        assertTrue(ex1.getMessage().contains("cloud-name"));

        properties.setCloudName("cloud");
        properties.setApiKey("");
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () -> cloudinaryService.generateAvatarUploadSignature());
        assertTrue(ex2.getMessage().contains("api-key"));

        properties.setApiKey("key");
        properties.setApiSecret("   ");
        IllegalStateException ex3 = assertThrows(IllegalStateException.class, () -> cloudinaryService.generateAvatarUploadSignature());
        assertTrue(ex3.getMessage().contains("api-secret"));
    }

    @Test
    @DisplayName("Trích xuất chính xác public_id từ các định dạng Cloudinary URL khác nhau")
    void testExtractPublicId() {
        // 1. URL chuẩn có version
        assertEquals("teu-mobile/avatars/abc123xyz",
                cloudinaryService.extractPublicId("https://res.cloudinary.com/teu-cloud/image/upload/v1731920000/teu-mobile/avatars/abc123xyz.jpg"));

        // 2. URL không có version
        assertEquals("teu-mobile/avatars/avatar_no_version",
                cloudinaryService.extractPublicId("https://res.cloudinary.com/teu-cloud/image/upload/teu-mobile/avatars/avatar_no_version.png"));

        // 3. URL có transformations và version
        assertEquals("teu-mobile/avatars/thumb_avatar",
                cloudinaryService.extractPublicId("https://res.cloudinary.com/teu-cloud/image/upload/c_thumb,w_200/v1731920000/teu-mobile/avatars/thumb_avatar.webp"));

        // 4. URL có query param
        assertEquals("teu-mobile/avatars/query_avatar",
                cloudinaryService.extractPublicId("https://res.cloudinary.com/teu-cloud/image/upload/v1731920000/teu-mobile/avatars/query_avatar.jpg?_a=ABC"));

        // 5. URL không thuộc Cloudinary (ví dụ Google URL) -> trả về null
        assertNull(cloudinaryService.extractPublicId("https://lh3.googleusercontent.com/a/ACg8ocK123=s96-c"));

        // 6. null hoặc rỗng -> trả về null
        assertNull(cloudinaryService.extractPublicId(null));
        assertNull(cloudinaryService.extractPublicId(""));
    }

    @Test
    @DisplayName("Xóa ảnh trên Cloudinary thành công khi API trả về result: ok")
    @SuppressWarnings("unchecked")
    void testDeleteImageSuccess() throws Exception {
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"result\":\"ok\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        boolean success = cloudinaryService.deleteImage("teu-mobile/avatars/old_avatar");
        assertTrue(success);
    }

    @Test
    @DisplayName("Upload ảnh từ URL từ xa (Google) lên Cloudinary thành công")
    @SuppressWarnings("unchecked")
    void testUploadFromUrlSuccess() throws Exception {
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"secure_url\":\"https://res.cloudinary.com/teu-cloud/image/upload/v123/teu-mobile/avatars/google_synced.jpg\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String resultUrl = cloudinaryService.uploadFromUrl("https://lh3.googleusercontent.com/a/sample=s96-c");
        assertNotNull(resultUrl);
        assertEquals("https://res.cloudinary.com/teu-cloud/image/upload/v123/teu-mobile/avatars/google_synced.jpg", resultUrl);
    }
}
