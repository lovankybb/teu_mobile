import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import {
  keycloakLogin,
  keycloakRefreshToken,
  keycloakLogout,
  keycloakGetUserInfo,
} from '../services/keycloak';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // ─── Khởi động ứng dụng: Kiểm tra token và lấy thông tin user ────────────
  useEffect(() => {
    async function loadUser() {
      const accessToken  = localStorage.getItem('access_token');
      const refreshToken = localStorage.getItem('refresh_token');

      if (!accessToken && !refreshToken) {
        setLoading(false);
        return;
      }

      try {
        // Thử lấy profile từ backend (access_token còn hạn)
        const res = await api.get('/users/me');
        if (res.data?.success && res.data?.data) {
          setUser(res.data.data);
          localStorage.setItem('user_info', JSON.stringify(res.data.data));
        }
      } catch (err) {
        // access_token hết hạn → thử refresh
        if (refreshToken) {
          try {
            const tokenData = await keycloakRefreshToken(refreshToken);
            localStorage.setItem('access_token', tokenData.access_token);
            if (tokenData.refresh_token) {
              localStorage.setItem('refresh_token', tokenData.refresh_token);
            }

            // Thử lại lấy profile sau khi refresh thành công
            const res2 = await api.get('/users/me');
            if (res2.data?.success && res2.data?.data) {
              setUser(res2.data.data);
              localStorage.setItem('user_info', JSON.stringify(res2.data.data));
            }
          } catch {
            console.warn('[Auth] Phiên đăng nhập đã hết hạn.');
            clearSession();
          }
        } else {
          console.warn('[Auth] Không có refresh token, xóa session.');
          clearSession();
        }
      } finally {
        setLoading(false);
      }
    }

    loadUser();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ─── Helper: xóa toàn bộ session khỏi localStorage ──────────────────────
  const clearSession = useCallback(() => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_info');
    setUser(null);
  }, []);

  // ─── Đăng Nhập ───────────────────────────────────────────────────────────
  /**
   * Gọi thẳng Keycloak token endpoint (Resource Owner Password Credentials Grant),
   * lưu token vào localStorage, rồi lấy profile từ backend.
   */
  const login = useCallback(async (usernameOrEmail, password) => {
    // 1. Lấy token từ Keycloak (gọi trực tiếp, không qua backend)
    const tokenData = await keycloakLogin(usernameOrEmail, password);

    localStorage.setItem('access_token', tokenData.access_token);
    if (tokenData.refresh_token) {
      localStorage.setItem('refresh_token', tokenData.refresh_token);
    }

    // 2. Lấy thông tin profile từ backend (backend dùng JWT để xác minh)
    try {
      const userRes = await api.get('/users/me');
      if (userRes.data?.success && userRes.data?.data) {
        setUser(userRes.data.data);
        localStorage.setItem('user_info', JSON.stringify(userRes.data.data));
        return userRes.data.data;
      }
    } catch (profileErr) {
      // Nếu backend chưa có user (lần đầu login với SSO) → dùng userinfo từ Keycloak
      console.warn('[Auth] Không lấy được profile từ backend, dùng Keycloak userinfo:', profileErr);
      const kcUser = await keycloakGetUserInfo(tokenData.access_token);
      const fallbackUser = {
        username: kcUser.preferred_username,
        email:    kcUser.email,
        fullName: kcUser.name,
      };
      setUser(fallbackUser);
      localStorage.setItem('user_info', JSON.stringify(fallbackUser));
      return fallbackUser;
    }

    return tokenData;
  }, []);

  // ─── Đăng Ký ─────────────────────────────────────────────────────────────
  /**
   * Đăng ký vẫn gọi backend (backend dùng Keycloak Admin API để tạo user),
   * sau đó tự động login.
   */
  const register = useCallback(async (registerData) => {
    const res = await api.post('/auth/register', registerData);
    if (res.data?.success) {
      // Tự động đăng nhập sau khi đăng ký thành công
      return await login(registerData.username, registerData.password);
    }
    throw new Error(res.data?.message || 'Đăng ký không thành công');
  }, [login]);

  // ─── Đăng Xuất ───────────────────────────────────────────────────────────
  /**
   * Revoke refresh token trực tiếp trên Keycloak, sau đó xóa session local.
   */
  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refresh_token');
    // Revoke session trực tiếp trên Keycloak (không qua backend)
    await keycloakLogout(refreshToken);
    clearSession();
  }, [clearSession]);

  // ─── Cập nhật Profile ────────────────────────────────────────────────────
  const updateProfile = useCallback(async (updateData) => {
    const res = await api.put('/users/me', updateData);
    if (res.data?.success && res.data?.data) {
      setUser(res.data.data);
      localStorage.setItem('user_info', JSON.stringify(res.data.data));
      return res.data.data;
    }
    throw new Error(res.data?.message || 'Cập nhật hồ sơ thất bại');
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        loading,
        login,
        register,
        logout,
        updateProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth phải được sử dụng bên trong AuthProvider');
  }
  return context;
}
