package com.teumobile.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teumobile.config.CloudinaryProperties;
import com.teumobile.dto.CloudinarySignatureResponseDto;
import com.teumobile.service.CloudinaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final CloudinaryProperties cloudinaryProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public CloudinaryServiceImpl(CloudinaryProperties cloudinaryProperties, ObjectMapper objectMapper) {
        this(cloudinaryProperties, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build());
    }

    public CloudinaryServiceImpl(CloudinaryProperties cloudinaryProperties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.cloudinaryProperties = cloudinaryProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public CloudinarySignatureResponseDto generateAvatarUploadSignature() {
        validateConfig();

        long timestamp = Instant.now().getEpochSecond();
        String folder = sanitizeFolder(cloudinaryProperties.getFolder());

        // Sử dụng TreeMap để đảm bảo các tham số luôn được sắp xếp theo thứ tự bảng chữ cái A-Z
        Map<String, Object> paramsToSign = new TreeMap<>();
        if (folder != null) {
            paramsToSign.put("folder", folder);
        }
        paramsToSign.put("timestamp", timestamp);

        String signature = calculateSignature(paramsToSign, cloudinaryProperties.getApiSecret().trim());
        log.debug("Đã sinh chữ ký Cloudinary thành công cho timestamp: {}", timestamp);

        return CloudinarySignatureResponseDto.builder()
                .signature(signature)
                .timestamp(timestamp)
                .apiKey(cloudinaryProperties.getApiKey().trim())
                .cloudName(cloudinaryProperties.getCloudName().trim())
                .folder(folder)
                .build();
    }

    @Override
    public String extractPublicId(String cloudinaryUrl) {
        if (cloudinaryUrl == null || !cloudinaryUrl.contains("res.cloudinary.com")) {
            return null;
        }

        int uploadIdx = cloudinaryUrl.indexOf("/upload/");
        if (uploadIdx == -1) {
            return null;
        }

        String afterUpload = cloudinaryUrl.substring(uploadIdx + "/upload/".length());

        // Bỏ query parameters nếu có (?...)
        int queryIdx = afterUpload.indexOf('?');
        if (queryIdx != -1) {
            afterUpload = afterUpload.substring(0, queryIdx);
        }

        // Tìm phần version v\d+/ (có thể đứng sau transformations như c_thumb,w_200/)
        Pattern pattern = Pattern.compile("^(?:.*\\/)?v\\d+\\/(.+)$");
        Matcher matcher = pattern.matcher(afterUpload);
        String publicIdWithExt;
        if (matcher.matches()) {
            publicIdWithExt = matcher.group(1);
        } else {
            publicIdWithExt = afterUpload;
        }

        // Bỏ đuôi mở rộng file (.jpg, .png, .webp, v.v.)
        int dotIdx = publicIdWithExt.lastIndexOf('.');
        if (dotIdx != -1) {
            return publicIdWithExt.substring(0, dotIdx);
        }
        return publicIdWithExt;
    }

    @Override
    public boolean deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }

        try {
            validateConfig();
            long timestamp = Instant.now().getEpochSecond();
            Map<String, Object> paramsToSign = new TreeMap<>();
            paramsToSign.put("public_id", publicId.trim());
            paramsToSign.put("timestamp", timestamp);

            String signature = calculateSignature(paramsToSign, cloudinaryProperties.getApiSecret().trim());

            String formBody = "public_id=" + URLEncoder.encode(publicId.trim(), StandardCharsets.UTF_8)
                    + "&timestamp=" + timestamp
                    + "&api_key=" + URLEncoder.encode(cloudinaryProperties.getApiKey().trim(), StandardCharsets.UTF_8)
                    + "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);

            String url = String.format("https://api.cloudinary.com/v1_1/%s/image/destroy",
                    cloudinaryProperties.getCloudName().trim());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Kết quả xóa ảnh cũ trên Cloudinary (public_id: {}): status={}, body={}",
                    publicId, response.statusCode(), response.body());
            return response.statusCode() == 200 && response.body().contains("\"result\":\"ok\"");
        } catch (Exception e) {
            log.warn("Không thể xóa ảnh cũ trên Cloudinary (public_id: {}): {}", publicId, e.getMessage());
            return false;
        }
    }

    @Override
    public String uploadFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            validateConfig();
            long timestamp = Instant.now().getEpochSecond();
            String folder = sanitizeFolder(cloudinaryProperties.getFolder());

            Map<String, Object> paramsToSign = new TreeMap<>();
            if (folder != null) {
                paramsToSign.put("folder", folder);
            }
            paramsToSign.put("timestamp", timestamp);

            String signature = calculateSignature(paramsToSign, cloudinaryProperties.getApiSecret().trim());

            StringBuilder formBody = new StringBuilder();
            formBody.append("file=").append(URLEncoder.encode(imageUrl.trim(), StandardCharsets.UTF_8))
                    .append("&timestamp=").append(timestamp)
                    .append("&api_key=").append(URLEncoder.encode(cloudinaryProperties.getApiKey().trim(), StandardCharsets.UTF_8))
                    .append("&signature=").append(URLEncoder.encode(signature, StandardCharsets.UTF_8));

            if (folder != null) {
                formBody.append("&folder=").append(URLEncoder.encode(folder, StandardCharsets.UTF_8));
            }

            String url = String.format("https://api.cloudinary.com/v1_1/%s/image/upload",
                    cloudinaryProperties.getCloudName().trim());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode node = objectMapper.readTree(response.body());
                String secureUrl = node.path("secure_url").asText(null);
                log.info("Upload ảnh từ URL ({}) lên Cloudinary thành công: {}", imageUrl, secureUrl);
                return secureUrl;
            } else {
                log.warn("Cloudinary upload từ URL ({}) không thành công: status={}, body={}",
                        imageUrl, response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.warn("Lỗi khi upload ảnh từ URL ({}) lên Cloudinary: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    /**
     * Thuật toán tạo chữ ký Cloudinary Signed Upload:
     * 1. Ghép các tham số cần ký theo thứ tự A-Z: param1=val1&param2=val2
     * 2. Nối trực tiếp API_SECRET vào cuối chuỗi
     * 3. Băm SHA-1 và trả về chuỗi Hexadecimal viết thường
     */
    public String calculateSignature(Map<String, Object> paramsToSign, String apiSecret) {
        String serialized = paramsToSign.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isBlank())
                .map(entry -> entry.getKey() + "=" + entry.getValue().toString())
                .collect(Collectors.joining("&"));

        String stringToSign = serialized + apiSecret;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(stringToSign.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hệ thống không hỗ trợ thuật toán băm SHA-1", e);
        }
    }

    private void validateConfig() {
        if (cloudinaryProperties.getCloudName() == null || cloudinaryProperties.getCloudName().isBlank()) {
            throw new IllegalStateException("Cấu hình Cloudinary thiếu cloud-name (CLOUDINARY_CLOUD_NAME)");
        }
        if (cloudinaryProperties.getApiKey() == null || cloudinaryProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("Cấu hình Cloudinary thiếu api-key (CLOUDINARY_API_KEY)");
        }
        if (cloudinaryProperties.getApiSecret() == null || cloudinaryProperties.getApiSecret().isBlank()) {
            throw new IllegalStateException("Cấu hình Cloudinary thiếu api-secret (CLOUDINARY_API_SECRET)");
        }
    }

    private String sanitizeFolder(String rawFolder) {
        if (rawFolder == null || rawFolder.isBlank()) {
            return null;
        }
        String cleaned = rawFolder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        return cleaned.isEmpty() ? null : cleaned;
    }
}
