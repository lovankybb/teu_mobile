package com.teumobile.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudinarySignatureResponseDto {

    /**
     * Chữ ký mã hóa SHA-1 tạo từ API_SECRET và các tham số upload
     */
    private String signature;

    /**
     * Unix timestamp (tính bằng giây) lúc tạo chữ ký
     */
    private Long timestamp;

    /**
     * Cloudinary API Key công khai
     */
    private String apiKey;

    /**
     * Tên Cloud Name của Cloudinary
     */
    private String cloudName;

    /**
     * Thư mục định tuyến file upload trên Cloudinary (nếu có cấu hình)
     */
    private String folder;
}
