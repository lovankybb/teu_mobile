import React, { useState } from "react";
import { Shield, ShieldAlert, ShieldCheck, X, AlertTriangle } from "lucide-react";
import Button from "../ui/Button";

export default function UserRoleModal({
  isOpen,
  user,
  currentUser,
  onClose,
  onRoleUpdate,
  loading,
}) {
  if (!isOpen || !user) return null;

  const isSelf = Boolean(
    currentUser && user && (
      (currentUser.id && user.id === currentUser.id) ||
      (currentUser.keycloakId && user.keycloakId === currentUser.keycloakId) ||
      (currentUser.username && user.username?.toLowerCase() === currentUser.username?.toLowerCase()) ||
      (currentUser.email && user.email?.toLowerCase() === currentUser.email?.toLowerCase())
    )
  );

  const currentIsAdmin = user.roles?.some(
    (r) => r.toUpperCase() === "ADMIN" || r.toUpperCase() === "ROLE_ADMIN"
  );

  const [targetIsAdmin, setTargetIsAdmin] = useState(currentIsAdmin);

  const handleSave = () => {
    if (isSelf && !targetIsAdmin) {
      return;
    }
    if (targetIsAdmin === currentIsAdmin) {
      onClose();
      return;
    }
    // Gán hoặc gỡ role ADMIN
    onRoleUpdate(user, "ADMIN", targetIsAdmin);
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-100 max-w-md w-full overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Shield className="w-6 h-6" />
            </div>
            <button
              onClick={onClose}
              disabled={loading}
              className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <h3 className="text-lg font-black text-slate-900 mb-1">
            Phân Quyền Vai Trò
          </h3>
          <p className="text-xs text-slate-500 mb-4">
            Thiết lập vai trò người dùng đồng bộ trực tiếp với Keycloak Realm
          </p>

          {/* User info chip */}
          <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50 border border-slate-100 mb-5">
            {user.avatarUrl ? (
              <img
                src={user.avatarUrl}
                alt={user.fullName}
                referrerPolicy="no-referrer"
                className="w-10 h-10 rounded-xl object-cover ring-1 ring-slate-200"
              />
            ) : (
              <div className="w-10 h-10 rounded-xl bg-primary text-white font-black text-xs flex items-center justify-center">
                {user.fullName ? user.fullName[0].toUpperCase() : "U"}
              </div>
            )}
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-1.5">
                <p className="text-xs font-bold text-slate-900 truncate">
                  {user.fullName || user.username}
                </p>
                {isSelf && (
                  <span className="px-1.5 py-0.2 rounded-full bg-indigo-100 text-indigo-700 text-[9px] font-black uppercase tracking-wide flex-shrink-0">
                    Bạn
                  </span>
                )}
              </div>
              <p className="text-[11px] text-slate-500 font-mono truncate">{user.email}</p>
            </div>
          </div>

          {/* Cảnh báo bảo mật tài khoản cá nhân */}
          {isSelf && (
            <div className="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-[11px] text-amber-800 leading-relaxed mb-4 flex items-start gap-2.5">
              <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
              <span>
                <strong>Tài khoản đang đăng nhập:</strong> Vì lý do an toàn bảo mật, bạn không thể tự hạ cấp hoặc gỡ bỏ quyền <strong>Quản trị viên (ADMIN)</strong> của chính mình.
              </span>
            </div>
          )}

          {/* Role selection toggle */}
          <div className="space-y-3 mb-5">
            {/* User Role */}
            <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50 flex items-center justify-between">
              <div>
                <p className="text-xs font-bold text-slate-800">Người Dùng (USER)</p>
                <p className="text-[11px] text-slate-500">Quyền mua hàng, quản lý hồ sơ và địa chỉ</p>
              </div>
              <span className="px-2 py-0.5 rounded-md bg-slate-200 text-slate-700 text-[10px] font-bold">
                Mặc định
              </span>
            </div>

            {/* Admin Role Toggle */}
            <label
              className={`p-3.5 rounded-xl border bg-white flex items-center justify-between transition-all ${
                isSelf && currentIsAdmin
                  ? "border-slate-200 bg-slate-50/60 cursor-not-allowed"
                  : "border-slate-200 hover:border-slate-300 cursor-pointer"
              }`}
            >
              <div>
                <div className="flex items-center gap-1.5">
                  <p className="text-xs font-bold text-slate-900">Quản Trị Viên (ADMIN)</p>
                  <span className="px-1.5 py-0.5 rounded bg-red-100 text-primary text-[10px] font-black uppercase">
                    Cấp cao
                  </span>
                </div>
                <p className="text-[11px] text-slate-500 mt-0.5">
                  {isSelf && currentIsAdmin
                    ? "Bạn không thể tự gỡ bỏ vai trò này của bản thân"
                    : "Toàn quyền truy cập bảng điều khiển và quản trị"}
                </p>
              </div>

              <input
                type="checkbox"
                checked={targetIsAdmin}
                onChange={(e) => {
                  if (isSelf && !e.target.checked) return;
                  setTargetIsAdmin(e.target.checked);
                }}
                disabled={loading || (isSelf && currentIsAdmin)}
                className={`w-4 h-4 rounded text-primary focus:ring-primary border-slate-300 ${
                  isSelf && currentIsAdmin ? "cursor-not-allowed opacity-50" : ""
                }`}
              />
            </label>
          </div>

          {/* Warning Message if toggling to Admin */}
          {targetIsAdmin && !currentIsAdmin && (
            <div className="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-[11px] text-amber-800 leading-relaxed mb-6 flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
              <span>
                <strong>Cảnh báo:</strong> Cấp quyền <strong>ADMIN</strong> cho phép tài khoản này
                truy cập toàn bộ hệ thống quản trị, xem danh sách thành viên và các báo cáo nội bộ.
              </span>
            </div>
          )}

          {/* Warning Message if toggling from Admin to Customer */}
          {!targetIsAdmin && currentIsAdmin && (
            <div className="p-3.5 rounded-xl bg-slate-100 border border-slate-200 text-[11px] text-slate-700 leading-relaxed mb-6 flex items-start gap-2.5">
              <ShieldAlert className="w-4 h-4 text-slate-500 flex-shrink-0 mt-0.5" />
              <span>
                Tài khoản này sẽ bị thu hồi quyền Quản trị viên và quay về vai trò người dùng thông thường (USER).
              </span>
            </div>
          )}

          <div className="flex items-center justify-end gap-3">
            <Button variant="outline" size="sm" onClick={onClose} disabled={loading}>
              Hủy
            </Button>
            <Button
              variant="primary"
              size="sm"
              isLoading={loading}
              onClick={handleSave}
              disabled={targetIsAdmin === currentIsAdmin || (isSelf && !targetIsAdmin)}
              className="font-bold"
            >
              Lưu Phân Quyền
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
