package com.teumobile.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cloudinary")
@Getter
@Setter
public class CloudinaryProperties {

    /**
     * Tên Cloud Name trên Cloudinary
     */
    private String cloudName;

    /**
     * API Key công khai của Cloudinary
     */
    private String apiKey;

    /**
     * API Secret bảo mật của Cloudinary (CHỈ lưu ở backend, TUYỆT ĐỐI không trả về frontend)
     */
    private String apiSecret;

    /**
     * Thư mục lưu trữ trên Cloudinary (mặc định: teu-mobile/avatars)
     */
    private String folder = "teu-mobile/avatars";
}
