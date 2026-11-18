package com.teumobile.service;

import com.teumobile.dto.CloudinarySignatureResponseDto;

public interface CloudinaryService {

    /**
     * Tạo chữ ký bảo mật (signature, timestamp, apiKey, cloudName, folder)
     * phục vụ trực tiếp cho Frontend upload ảnh avatar lên Cloudinary.
     */
    CloudinarySignatureResponseDto generateAvatarUploadSignature();

    /**
     * Trích xuất public_id của tài sản từ đường dẫn Cloudinary URL.
     * Ví dụ: https://res.cloudinary.com/demo/image/upload/v123456/teu-mobile/avatars/abc.jpg
     * -> teu-mobile/avatars/abc
     */
    String extractPublicId(String cloudinaryUrl);

    /**
     * Xóa một file ảnh trên Cloudinary bằng public_id qua Destroy API để tiết kiệm dung lượng lưu trữ.
     * @param publicId public_id của ảnh cần xóa (ví dụ: teu-mobile/avatars/abc123xyz)
     * @return true nếu xóa thành công, false nếu thất bại
     */
    boolean deleteImage(String publicId);

    /**
     * Tải ảnh trực tiếp từ đường dẫn URL từ xa (ví dụ: ảnh Google của người dùng) lên Cloudinary.
     * Cloudinary sẽ tự động fetch ảnh từ URL này và lưu trữ vào thư mục cấu hình.
     * @param imageUrl đường dẫn ảnh nguồn từ Google/IdP
     * @return secure_url của ảnh đã được lưu trữ trên Cloudinary
     */
    String uploadFromUrl(String imageUrl);
}
