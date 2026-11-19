import React, { useState } from "react";
import { UserPlus, X, Eye, EyeOff, Shield, AlertCircle } from "lucide-react";
import Button from "../ui/Button";
import Input from "../ui/Input";
import adminUserService from "../../services/adminUserService";

export default function UserCreateModal({
  isOpen,
  onClose,
  onUserCreated,
}) {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    fullName: "",
    phoneNumber: "",
    address: "",
    role: "USER",
  });

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState(null);

  if (!isOpen) return null;

  const handleChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: null }));
    }
    if (serverError) setServerError(null);
  };

  const validate = () => {
    const newErrors = {};

    // Validate username
    if (!formData.username.trim()) {
      newErrors.username = "Tên đăng nhập không được để trống";
    } else if (formData.username.length < 3 || formData.username.length > 30) {
      newErrors.username = "Tên đăng nhập phải từ 3 đến 30 ký tự";
    } else if (/\s/.test(formData.username)) {
      newErrors.username = "Tên đăng nhập không được chứa khoảng trắng";
    }

    // Validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim()) {
      newErrors.email = "Email không được để trống";
    } else if (!emailRegex.test(formData.email.trim())) {
      newErrors.email = "Định dạng email không hợp lệ";
    }

    // Validate password
    if (!formData.password) {
      newErrors.password = "Mật khẩu không được để trống";
    } else if (formData.password.length < 6) {
      newErrors.password = "Mật khẩu phải từ 6 ký tự trở lên";
    }

    // Validate phone (optional)
    if (formData.phoneNumber && formData.phoneNumber.trim()) {
      const phoneRegex = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;
      if (!phoneRegex.test(formData.phoneNumber.trim())) {
        newErrors.phoneNumber = "Số điện thoại di động Việt Nam không hợp lệ";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    setServerError(null);

    try {
      const res = await adminUserService.createUser(formData);
      if (res?.success) {
        onUserCreated(res.data);
        onClose();
      } else {
        setServerError(res?.message || "Không thể tạo tài khoản");
      }
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.errors?.map?.((e) => e.message || e)?.join(", ") ||
        err.message ||
        "Lỗi khi kết nối máy chủ";
      setServerError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-100 max-w-lg w-full overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
              <UserPlus className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-extrabold text-slate-900">
                Thêm Người Dùng Mới
              </h3>
              <p className="text-xs text-slate-500">
                Tạo tài khoản Keycloak và đồng bộ trực tiếp vào cơ sở dữ liệu
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            disabled={submitting}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {serverError && (
            <div className="p-3.5 rounded-xl bg-red-50 border border-red-200 text-xs text-red-600 font-semibold flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{serverError}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Tên đăng nhập"
              placeholder="ví dụ: admin_tech"
              value={formData.username}
              onChange={(e) => handleChange("username", e.target.value)}
              error={errors.username}
              required
            />

            <Input
              label="Địa chỉ Email"
              type="email"
              placeholder="admin@teumobile.com"
              value={formData.email}
              onChange={(e) => handleChange("email", e.target.value)}
              error={errors.email}
              required
            />
          </div>

          <Input
            label="Mật khẩu khởi tạo"
            type="password"
            placeholder="Tối thiểu 6 ký tự"
            value={formData.password}
            onChange={(e) => handleChange("password", e.target.value)}
            error={errors.password}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Họ và tên"
              placeholder="Nguyễn Văn A"
              value={formData.fullName}
              onChange={(e) => handleChange("fullName", e.target.value)}
            />

            <Input
              label="Số điện thoại"
              placeholder="0912345678"
              value={formData.phoneNumber}
              onChange={(e) => handleChange("phoneNumber", e.target.value)}
              error={errors.phoneNumber}
            />
          </div>

          <Input
            label="Địa chỉ thường trú"
            placeholder="Số nhà, tên đường, phường/xã..."
            value={formData.address}
            onChange={(e) => handleChange("address", e.target.value)}
          />

          {/* Vai trò ban đầu */}
          <div>
            <label className="block text-sm font-semibold text-slate-800 mb-1.5">
              Vai trò ban đầu
            </label>
            <div className="grid grid-cols-2 gap-3">
              <label
                className={`p-3 rounded-xl border flex items-center gap-3 cursor-pointer transition-all ${
                  formData.role === "USER"
                    ? "border-primary bg-red-50/20 text-slate-900"
                    : "border-slate-200 hover:border-slate-300 bg-white text-slate-600"
                }`}
              >
                <input
                  type="radio"
                  name="role"
                  value="USER"
                  checked={formData.role === "USER"}
                  onChange={() => handleChange("role", "USER")}
                  className="text-primary focus:ring-primary"
                />
                <span className="text-xs font-bold">Người Dùng (USER)</span>
              </label>

              <label
                className={`p-3 rounded-xl border flex items-center gap-3 cursor-pointer transition-all ${
                  formData.role === "ADMIN"
                    ? "border-primary bg-red-50/20 text-slate-900"
                    : "border-slate-200 hover:border-slate-300 bg-white text-slate-600"
                }`}
              >
                <input
                  type="radio"
                  name="role"
                  value="ADMIN"
                  checked={formData.role === "ADMIN"}
                  onChange={() => handleChange("role", "ADMIN")}
                  className="text-primary focus:ring-primary"
                />
                <span className="text-xs font-bold text-red-600">Quản Trị (ADMIN)</span>
              </label>
            </div>
          </div>

          {/* Form Actions */}
          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={onClose}
              disabled={submitting}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={submitting}
              className="font-bold"
            >
              Tạo Người Dùng
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
