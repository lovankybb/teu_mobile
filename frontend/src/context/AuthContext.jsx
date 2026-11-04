import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Khởi động ứng dụng: Kiểm tra token và lấy thông tin user
  useEffect(() => {
    async function loadUser() {
      const token = localStorage.getItem('access_token');
      if (token) {
        try {
          const res = await api.get('/users/me');
          if (res.data?.success && res.data?.data) {
            setUser(res.data.data);
            localStorage.setItem('user_info', JSON.stringify(res.data.data));
          }
        } catch (err) {
          console.warn('Phiên đăng nhập đã hết hạn:', err);
          logout();
        }
      }
      setLoading(false);
    }

    loadUser();
  }, []);

  // Hàm Đăng Nhập
  const login = async (usernameOrEmail, password) => {
    const res = await api.post('/auth/login', {
      usernameOrEmail,
      password,
    });

    if (res.data?.success && res.data?.data) {
      const { accessToken, refreshToken } = res.data.data;
      localStorage.setItem('access_token', accessToken);
      if (refreshToken) {
        localStorage.setItem('refresh_token', refreshToken);
      }

      // Lấy ngay thông tin profile của user
      try {
        const userRes = await api.get('/users/me');
        if (userRes.data?.success) {
          setUser(userRes.data.data);
          localStorage.setItem('user_info', JSON.stringify(userRes.data.data));
        }
      } catch (profileErr) {
        console.warn('Lỗi lấy profile:', profileErr);
      }

      return res.data.data;
    }
    throw new Error(res.data?.message || 'Đăng nhập không thành công');
  };

  // Hàm Đăng Ký
  const register = async (registerData) => {
    const res = await api.post('/auth/register', registerData);
    if (res.data?.success) {
      // Sau khi đăng ký thành công, tự động đăng nhập luôn
      return await login(registerData.username, registerData.password);
    }
    throw new Error(res.data?.message || 'Đăng ký không thành công');
  };

  // Hàm Đăng Xuất
  const logout = async () => {
    const refreshToken = localStorage.getItem('refresh_token');
    if (refreshToken) {
      try {
        await api.post('/auth/logout', { refreshToken });
      } catch (err) {
        console.warn('Lỗi gọi logout API:', err);
      }
    }
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_info');
    setUser(null);
  };

  // Cập nhật thông tin profile
  const updateProfile = async (updateData) => {
    const res = await api.put('/users/me', updateData);
    if (res.data?.success && res.data?.data) {
      setUser(res.data.data);
      localStorage.setItem('user_info', JSON.stringify(res.data.data));
      return res.data.data;
    }
    throw new Error(res.data?.message || 'Cập nhật hồ sơ thất bại');
  };

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
