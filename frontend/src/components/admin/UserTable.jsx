import React from "react";
import {
  Eye,
  Shield,
  Lock,
  Unlock,
  CheckCircle2,
  XCircle,
  Clock,
  User,
  ShieldAlert,
  Mail,
  Phone,
} from "lucide-react";

export default function UserTable({
  users,
  loading,
  currentUser,
  onViewDetail,
  onToggleStatus,
  onChangeRole,
}) {
  const formatDate = (dateString) => {
    if (!dateString) return "—";
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(date);
    } catch {
      return dateString;
    }
  };

  // Skeleton Loading Table
  if (loading) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="w-32 h-4 bg-slate-200 rounded animate-pulse" />
        </div>
        <div className="divide-y divide-slate-100">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="p-4 flex items-center justify-between animate-pulse gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-slate-200" />
                <div className="space-y-1.5">
                  <div className="w-28 h-3.5 bg-slate-200 rounded" />
                  <div className="w-20 h-3 bg-slate-100 rounded" />
                </div>
              </div>
              <div className="w-36 h-3 bg-slate-100 rounded hidden md:block" />
              <div className="w-20 h-6 bg-slate-100 rounded-full" />
              <div className="w-24 h-6 bg-slate-100 rounded-full hidden sm:block" />
              <div className="w-24 h-8 bg-slate-100 rounded-xl" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Empty State
  if (!users || users.length === 0) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center shadow-sm">
        <div className="w-16 h-16 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-4">
          <User className="w-8 h-8" />
        </div>
        <h3 className="text-base font-bold text-slate-800 mb-1">
          Không tìm thấy người dùng
        </h3>
        <p className="text-xs text-slate-500 max-w-sm mx-auto">
          Không có tài khoản nào khớp với tiêu chí tìm kiếm hoặc bộ lọc hiện tại của bạn.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/80 border-b border-slate-200/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
              <th className="py-3.5 px-4 sm:px-6">Người dùng</th>
              <th className="py-3.5 px-4 hidden md:table-cell">Liên hệ</th>
              <th className="py-3.5 px-4">Vai trò</th>
              <th className="py-3.5 px-4">Trạng thái</th>
              <th className="py-3.5 px-4 hidden lg:table-cell">Ngày tham gia</th>
              <th className="py-3.5 px-4 sm:px-6 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
            {users.map((u) => {
              const isAdmin = u.roles?.some(
                (r) => r.toUpperCase() === "ADMIN" || r.toUpperCase() === "ROLE_ADMIN"
              );
              const isSelf = Boolean(
                currentUser && (
                  (currentUser.id && u.id === currentUser.id) ||
                  (currentUser.keycloakId && u.keycloakId === currentUser.keycloakId) ||
                  (currentUser.username && u.username?.toLowerCase() === currentUser.username?.toLowerCase()) ||
                  (currentUser.email && u.email?.toLowerCase() === currentUser.email?.toLowerCase())
                )
              );

              return (
                <tr
                  key={u.id}
                  className="hover:bg-slate-50/60 transition-colors group"
                >
                  {/* Cột 1: Người dùng (Avatar, Tên, Username) */}
                  <td className="py-3.5 px-4 sm:px-6">
                    <div className="flex items-center gap-3">
                      {u.avatarUrl ? (
                        <img
                          src={u.avatarUrl}
                          alt={u.fullName || u.username}
                          referrerPolicy="no-referrer"
                          className="w-10 h-10 rounded-xl object-cover ring-1 ring-slate-200/80 flex-shrink-0"
                        />
                      ) : (
                        <div className="w-10 h-10 rounded-xl bg-primary/10 text-primary font-black text-sm flex items-center justify-center flex-shrink-0">
                          {u.fullName ? u.fullName[0].toUpperCase() : u.username[0].toUpperCase()}
                        </div>
                      )}
                      <div className="min-w-0">
                        <div className="flex items-center gap-1.5">
                          <p className="font-bold text-slate-900 truncate">
                            {u.fullName || "—"}
                          </p>
                          {isSelf && (
                            <span className="px-1.5 py-0.5 rounded-full bg-indigo-100 text-indigo-700 text-[9px] font-black uppercase tracking-wide flex-shrink-0">
                              Bạn
                            </span>
                          )}
                        </div>
                        <p className="text-[11px] font-mono text-slate-400 truncate">
                          @{u.username}
                        </p>
                      </div>
                    </div>
                  </td>

                  {/* Cột 2: Liên hệ (Email, SĐT) */}
                  <td className="py-3.5 px-4 hidden md:table-cell">
                    <div className="space-y-1">
                      <div className="flex items-center gap-1.5 text-slate-700 truncate">
                        <Mail className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                        <span className="truncate">{u.email}</span>
                      </div>
                      {u.phoneNumber && (
                        <div className="flex items-center gap-1.5 text-slate-500 font-mono text-[11px]">
                          <Phone className="w-3 h-3 text-slate-400 flex-shrink-0" />
                          <span>{u.phoneNumber}</span>
                        </div>
                      )}
                    </div>
                  </td>

                  {/* Cột 3: Vai trò */}
                  <td className="py-3.5 px-4">
                    <div className="flex flex-wrap gap-1">
                      {isAdmin ? (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-red-50 text-primary font-bold text-[10px] uppercase border border-red-100">
                          <ShieldAlert className="w-3 h-3" />
                          ADMIN
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 font-semibold text-[10px] uppercase">
                          USER
                        </span>
                      )}
                    </div>
                  </td>

                  {/* Cột 4: Trạng thái */}
                  <td className="py-3.5 px-4">
                    {u.isActive ? (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 font-bold text-[11px]">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                        Hoạt động
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-red-50 text-red-600 font-bold text-[11px]">
                        <span className="w-1.5 h-1.5 rounded-full bg-red-500" />
                        Đã bị khóa
                      </span>
                    )}
                  </td>

                  {/* Cột 5: Ngày tạo */}
                  <td className="py-3.5 px-4 text-slate-500 font-medium hidden lg:table-cell">
                    {formatDate(u.createdAt)}
                  </td>

                  {/* Cột 6: Thao tác */}
                  <td className="py-3.5 px-4 sm:px-6 text-right">
                    <div className="flex items-center justify-end gap-1">
                      {/* Xem chi tiết */}
                      <button
                        onClick={() => onViewDetail(u)}
                        title="Xem chi tiết hồ sơ & sổ địa chỉ"
                        className="p-1.5 rounded-lg text-slate-500 hover:text-slate-900 hover:bg-slate-100 transition-colors"
                      >
                        <Eye className="w-4 h-4" />
                      </button>

                      {/* Phân quyền vai trò */}
                      <button
                        onClick={() => onChangeRole(u)}
                        title="Quản lý vai trò (Admin / Customer)"
                        className="p-1.5 rounded-lg text-indigo-500 hover:text-indigo-700 hover:bg-indigo-50 transition-colors"
                      >
                        <Shield className="w-4 h-4" />
                      </button>

                      {/* Khóa / Mở khóa tài khoản */}
                      <button
                        onClick={() => !isSelf && onToggleStatus(u)}
                        disabled={isSelf}
                        title={
                          isSelf
                            ? "Bạn không thể tự khóa tài khoản của chính mình"
                            : u.isActive
                            ? "Khóa tài khoản này"
                            : "Mở khóa tài khoản này"
                        }
                        className={`p-1.5 rounded-lg transition-colors ${
                          isSelf
                            ? "text-slate-300 cursor-not-allowed opacity-40 hover:bg-transparent"
                            : u.isActive
                            ? "text-red-500 hover:text-red-700 hover:bg-red-50"
                            : "text-emerald-600 hover:text-emerald-800 hover:bg-emerald-50"
                        }`}
                      >
                        {u.isActive ? <Lock className="w-4 h-4" /> : <Unlock className="w-4 h-4" />}
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
