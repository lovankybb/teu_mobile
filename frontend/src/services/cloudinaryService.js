/**
 * cloudinaryService.js
 * Quản lý quy trình upload ảnh trực tiếp từ Frontend lên Cloudinary thông qua chữ ký bảo mật (Signed Upload).
 *
 * Quy trình (Flow):
 *  1. Frontend gọi Backend: GET /api/users/avatar/signature để xin chữ ký (signature, timestamp, apiKey, cloudName, folder)
 *  2. Backend dùng CLOUDINARY_API_SECRET tạo chữ ký SHA-1 và trả về Frontend (KHÔNG để lộ API_SECRET)
 *  3. Frontend dùng chữ ký đó gửi thẳng file ảnh (FormData) lên Cloudinary API:
 *     https://api.cloudinary.com/v1_1/<cloudName>/image/upload
 *  4. Cloudinary trả về secure_url, Frontend gọi PATCH /api/users/me/avatar để lưu URL vào CSDL SQL Server.
 */
import axios from 'axios';
import api from './api';

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

/**
 * Bước 1: Xin chữ ký bảo mật từ Backend
 */
export async function getAvatarUploadSignature() {
  const response = await api.get('/users/avatar/signature');
  if (!response.data?.success || !response.data?.data) {
    throw new Error(response.data?.message || 'Không thể lấy chữ ký tải ảnh từ hệ thống');
  }
  return response.data.data;
}

/**
 * Bước 2: Tải file ảnh trực tiếp lên Cloudinary bằng chữ ký bảo mật
 * @param {File} file - File ảnh người dùng chọn
 * @param {Object} signatureData - Dữ liệu chữ ký từ backend { signature, timestamp, apiKey, cloudName, folder }
 * @param {Function} [onProgress] - Callback theo dõi tiến trình upload (0 -> 100)
 */
export async function uploadImageToCloudinary(file, signatureData, onProgress) {
  const { signature, timestamp, apiKey, cloudName, folder } = signatureData;

  const formData = new FormData();
  formData.append('file', file);
  formData.append('api_key', apiKey);
  formData.append('timestamp', timestamp);
  formData.append('signature', signature);
  if (folder) {
    formData.append('folder', folder);
  }

  const uploadUrl = `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`;

  // Sử dụng axios độc lập (không gắn Bearer Token của Backend vào request sang Cloudinary)
  // KHÔNG set thủ công 'Content-Type': 'multipart/form-data' vì sẽ làm mất boundary của trình duyệt
  try {
    const response = await axios.post(uploadUrl, formData, {
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percentCompleted);
        }
      },
    });

    return response.data; // Chứa secure_url, public_id, ...
  } catch (error) {
    const cloudErrorMsg = error.response?.data?.error?.message
      || error.response?.data?.message
      || error.message;
    console.error('[Cloudinary] Upload thất bại:', error.response?.data || error);
    throw new Error(`Upload ảnh lên Cloudinary thất bại: ${cloudErrorMsg}`);
  }
}

/**
 * Bước 3: Gửi secure_url về Backend để lưu vào cơ sở dữ liệu
 * @param {string} avatarUrl - Đường dẫn ảnh an toàn trả về từ Cloudinary
 */
export async function updateUserAvatar(avatarUrl) {
  const response = await api.patch('/users/me/avatar', { avatarUrl });
  if (!response.data?.success || !response.data?.data) {
    throw new Error(response.data?.message || 'Cập nhật ảnh đại diện vào cơ sở dữ liệu thất bại');
  }
  return response.data.data;
}

/**
 * Pipeline trọn vẹn: Kiểm tra file -> Xin chữ ký -> Upload Cloudinary -> Lưu Database Backend
 * @param {File} file - File ảnh từ <input type="file">
 * @param {Function} [onProgress] - Callback cập nhật phần trăm tiến trình (0 - 100)
 * @returns {Promise<Object>} Hồ sơ người dùng đã được cập nhật từ Backend
 */
export async function uploadAvatarWorkflow(file, onProgress) {
  // 1. Kiểm tra tính hợp lệ của file ở client
  if (!file) {
    throw new Error('Vui lòng chọn một file ảnh');
  }

  if (!ALLOWED_MIME_TYPES.includes(file.type)) {
    throw new Error('Định dạng file không hỗ trợ. Vui lòng chọn ảnh định dạng JPG, PNG, WEBP hoặc GIF');
  }

  if (file.size > MAX_FILE_SIZE_BYTES) {
    throw new Error('Dung lượng ảnh vượt quá giới hạn 5MB. Vui lòng chọn file nhỏ hơn');
  }

  // 2. Xin chữ ký bảo mật từ Backend
  const signatureData = await getAvatarUploadSignature();

  // 3. Gửi thẳng file ảnh lên Cloudinary
  const cloudinaryResult = await uploadImageToCloudinary(file, signatureData, onProgress);
  const secureUrl = cloudinaryResult.secure_url;

  if (!secureUrl) {
    throw new Error('Cloudinary không trả về URL ảnh hợp lệ');
  }

  // 4. Gửi URL về Backend lưu vào Database
  const updatedUserProfile = await updateUserAvatar(secureUrl);
  return updatedUserProfile;
}
