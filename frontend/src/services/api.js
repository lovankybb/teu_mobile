/**
 * api.js
 * Axios instance với interceptor tự động đính kèm Bearer Token từ keycloak-js.
 * Token sống trong memory (keycloak.token) — không còn dùng localStorage.
 */
import axios from 'axios';
import keycloak from './keycloak';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── Request Interceptor ────────────────────────────────────────────────────
// Tự động đính kèm Bearer token từ Keycloak instance (in-memory, không localStorage)
api.interceptors.request.use(
  async (config) => {
    if (keycloak.authenticated) {
      // Refresh token nếu còn dưới 30 giây — tránh gửi request với token hết hạn
      try {
        await keycloak.updateToken(30);
      } catch {
        // Token không thể refresh → Keycloak sẽ handle qua onTokenExpired
        console.warn('[API] Không thể refresh token trước request');
      }
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response Interceptor ───────────────────────────────────────────────────
// Nếu nhận 401 → redirect về Keycloak login
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token thực sự không hợp lệ → yêu cầu đăng nhập lại
      try {
        await keycloak.updateToken(-1); // Force refresh
      } catch {
        keycloak.login({ redirectUri: window.location.href });
      }
    }
    return Promise.reject(error);
  }
);

export default api;
