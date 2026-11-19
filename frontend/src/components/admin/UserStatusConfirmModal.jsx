import React from "react";
import { AlertTriangle, Lock, Unlock, X } from "lucide-react";
import Button from "../ui/Button";

export default function UserStatusConfirmModal({
  isOpen,
  user,
  currentUser,
  onClose,
  onConfirm,
  loading,
}) {
  if (!isOpen || !user) return null;

  const isLocking = user.isActive;
  const isSelf = Boolean(
    currentUser && (
      (currentUser.id && user.id === currentUser.id) ||
      (currentUser.keycloakId && user.keycloakId === currentUser.keycloakId) ||
      (currentUser.username && user.username?.toLowerCase() === currentUser.username?.toLowerCase()) ||
      (currentUser.email && user.email?.toLowerCase() === currentUser.email?.toLowerCase())
    )
  );

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-100 max-w-md w-full overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center ${
                isSelf && isLocking
                  ? "bg-amber-50 text-amber-600"
                  : isLocking
                  ? "bg-red-50 text-red-600"
                  : "bg-emerald-50 text-emerald-600"
              }`}
            >
              {isLocking ? (
                <Lock className="w-6 h-6" />
              ) : (
                <Unlock className="w-6 h-6" />
              )}
            </div>
            <button
              onClick={onClose}
              disabled={loading}
              className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <h3 className="text-lg font-black text-slate-900 mb-2">
            {isSelf && isLocking
              ? "Không thể tự khóa tài khoản"
              : isLocking
              ? "Xác nhận Khóa Tài Khoản?"
              : "Xác nhận Mở Khóa Tài Khoản?"}
          </h3>

          <p className="text-xs text-slate-600 leading-relaxed mb-4">
            Bạn đang thao tác với người dùng{" "}
            <strong className="text-slate-900">
              {user.fullName || user.username}
            </strong>{" "}
            (<span className="font-mono text-slate-500">@{user.username}</span> -{" "}
            {user.email}).
          </p>

          {isSelf && isLocking ? (
            <div className="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-[11px] text-amber-800 leading-relaxed mb-6 flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
              <span>
                <strong>Hành động bị từ chối:</strong> Đây là tài khoản bạn đang sử dụng. Vì lý do an toàn bảo mật hệ thống, bạn không thể tự khóa tài khoản của chính mình.
              </span>
            </div>
          ) : isLocking ? (
            <div className="p-3.5 rounded-xl bg-red-50 border border-red-100 text-[11px] text-red-700 leading-relaxed mb-6 flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5" />
              <span>
                Khi bị khóa, tài khoản này sẽ bị vô hiệu hóa ngay lập tức trên máy chủ
                <strong> Keycloak</strong>, bị thu hồi phiên đăng nhập và không thể truy cập
                các dịch vụ của Teu Mobile.
              </span>
            </div>
          ) : (
            <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-100 text-[11px] text-emerald-800 leading-relaxed mb-6 flex items-start gap-2.5">
              <Unlock className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
              <span>
                Khi mở khóa, tài khoản sẽ được kích hoạt lại trên <strong>Keycloak</strong> và
                người dùng có thể đăng nhập bình thường.
              </span>
            </div>
          )}

          <div className="flex items-center justify-end gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={onClose}
              disabled={loading}
            >
              {isSelf && isLocking ? "Đóng" : "Hủy"}
            </Button>
            {!(isSelf && isLocking) && (
              <Button
                variant="primary"
                size="sm"
                isLoading={loading}
                onClick={() => onConfirm(user, !isLocking)}
                className={
                  isLocking
                    ? "bg-red-600 hover:bg-red-700 text-white font-bold"
                    : "bg-emerald-600 hover:bg-emerald-700 text-white font-bold"
                }
              >
                {isLocking ? "Đồng ý Khóa" : "Đồng ý Mở Khóa"}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
