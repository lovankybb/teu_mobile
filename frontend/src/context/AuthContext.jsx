/**
 * AuthContext.jsx
 * Quản lý trạng thái xác thực toàn ứng dụng qua Keycloak Authorization Code + PKCE.
 *
 * Flow:
 *  1. App khởi động → keycloak.init() kiểm tra session sẵn có (SSO silent check)
 *  2. Nếu chưa đăng nhập → user nhấn nút → keycloak.login() redirect sang Keycloak
 *  3. Keycloak xử lý login, redirect về app kèm ?code=...
 *  4. keycloak-js tự đổi code → token (PKCE hoàn tất)
 *  5. Token được lưu trong memory của keycloak instance (không localStorage)
 *  6. Keycloak tự refresh token trước khi hết hạn (setInterval nội bộ)
 */
import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
} from "react";
import keycloak from "../services/keycloak";
import api from "../services/api";
import { uploadAvatarWorkflow } from "../services/cloudinaryService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null); // Profile từ backend
  const [loading, setLoading] = useState(true); // Đang khởi tạo Keycloak
  const initialized = useRef(false); // Tránh init 2 lần (React StrictMode)

  // ─── Khởi tạo Keycloak một lần duy nhất khi app mount ───────────────────
  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;

    keycloak
      .init({
        onLoad: "check-sso", // Không force redirect — chỉ kiểm tra session
        silentCheckSsoRedirectUri:
          window.location.origin + "/silent-check-sso.html",
        pkceMethod: "S256", // PKCE với SHA-256 — bắt buộc với public client
        checkLoginIframe: false, // Tránh vấn đề cross-origin iframe trên một số browser
      })
      .then(async (authenticated) => {
        if (authenticated) {
          await loadUserProfile();
        }
      })
      .catch((err) => {
        console.error("[Keycloak] Khởi tạo thất bại:", err);
      })
      .finally(() => {
        setLoading(false);
      });

    // Callback khi token được refresh tự động
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).catch(() => {
        console.warn("[Keycloak] Không thể refresh token, đăng xuất.");
        clearSession();
      });
    };

    // Callback khi bị đăng xuất từ Keycloak (SSO logout từ app khác)
    keycloak.onAuthLogout = () => {
      clearSession();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ─── Lấy profile từ backend sau khi xác thực thành công ─────────────────
  const loadUserProfile = useCallback(async () => {
    try {
      const res = await api.get("/users/me");
      if (res.data?.success && res.data?.data) {
        setUser(res.data.data);
      }
    } catch (err) {
      // Trường hợp user lần đầu đăng nhập, gọi sync để Backend khởi tạo từ Access Token
      if (err.response?.status === 404) {
        try {
          const syncRes = await api.post("/users/sync");
          if (syncRes.data?.success && syncRes.data?.data) {
            setUser(syncRes.data.data);
            return;
          }
        } catch (syncErr) {
          console.warn("[Auth] Tự động sync tài khoản vào CSDL thất bại:", syncErr?.message);
        }
      }

      // Dùng thông tin cơ bản từ Keycloak token nếu Backend chưa phản hồi
      if (keycloak.tokenParsed || keycloak.idTokenParsed) {
        setUser({
          username: keycloak.tokenParsed?.preferred_username || keycloak.idTokenParsed?.preferred_username,
          email: keycloak.tokenParsed?.email || keycloak.idTokenParsed?.email,
          fullName: keycloak.tokenParsed?.name || keycloak.idTokenParsed?.name,
          avatarUrl: keycloak.tokenParsed?.picture || keycloak.idTokenParsed?.picture,
        });
      }
      console.warn("[Auth] Chưa lấy được profile từ backend:", err?.message);
    }
  }, []);

  // ─── Xóa state user (không logout Keycloak) ─────────────────────────────
  const clearSession = useCallback(() => {
    setUser(null);
  }, []);

  // ─── Đăng Nhập — redirect sang trang login của Keycloak ─────────────────
  const login = useCallback((redirectUri) => {
    keycloak.login({
      redirectUri: redirectUri || window.location.origin + "/",
    });
  }, []);

  // ─── Đăng Xuất — Keycloak thu hồi session và redirect về app ────────────
  const logout = useCallback(() => {
    clearSession();
    console.log(window.location.origin + "/");
    keycloak.logout({
      redirectUri: window.location.origin + "/",
    });
  }, [clearSession]);

  // ─── Đăng Ký — vẫn gọi backend để tạo user (Keycloak Admin API) ─────────
  const register = useCallback(async (registerData) => {
    const res = await api.post("/auth/register", registerData);
    if (res.data?.success) {
      // Sau khi đăng ký thành công → redirect đến trang login Keycloak
      keycloak.login({
        redirectUri: window.location.origin + "/",
        loginHint: registerData.username,
      });
    }
    return res.data;
  }, []);

  // ─── Cập nhật profile ────────────────────────────────────────────────────
  const updateProfile = useCallback(async (profileData) => {
    const res = await api.put("/users/me", profileData);
    if (res.data?.success && res.data?.data) {
      setUser(res.data.data);
    }
    return res.data;
  }, []);

  // ─── Tải lên và cập nhật avatar trực tiếp qua Cloudinary ──────────────────
  const uploadAvatar = useCallback(async (file, onProgress) => {
    const updatedUser = await uploadAvatarWorkflow(file, onProgress);
    setUser(updatedUser);
    return updatedUser;
  }, []);

  const value = {
    user,
    setUser,
    loading,
    isAuthenticated: keycloak.authenticated ?? false,
    keycloak, // Expose instance nếu cần dùng trực tiếp
    login,
    logout,
    register,
    updateProfile,
    uploadAvatar,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth phải được dùng bên trong <AuthProvider>");
  return ctx;
}
