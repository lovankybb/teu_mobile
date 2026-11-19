import React, { useState, useEffect } from "react";
import {
  X,
  User,
  Mail,
  Phone,
  MapPin,
  Calendar,
  KeyRound,
  ShieldAlert,
  Copy,
  Check,
  Building,
  Home,
  CheckCircle2,
  Lock,
  Unlock,
  RefreshCw,
} from "lucide-react";
import adminUserService from "../../services/adminUserService";
import Button from "../ui/Button";

export default function UserDetailModal({
  userId,
  isOpen,
  currentUser,
  onClose,
  onToggleStatus,
}) {
  const [userDetail, setUserDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copiedKeycloakId, setCopiedKeycloakId] = useState(false);

  const isSelf = Boolean(
    currentUser && userDetail && (
      (currentUser.id && userDetail.id === currentUser.id) ||
      (currentUser.keycloakId && userDetail.keycloakId === currentUser.keycloakId) ||
      (currentUser.username && userDetail.username?.toLowerCase() === currentUser.username?.toLowerCase()) ||
      (currentUser.email && userDetail.email?.toLowerCase() === currentUser.email?.toLowerCase())
    )
  );

  useEffect(() => {
    if (!isOpen || !userId) return;

    let isMounted = true;
    setLoading(true);
    setError(null);

    adminUserService
      .getUserDetail(userId)
      .then((res) => {
        if (isMounted) {
          setUserDetail(res.data);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setError(err.response?.data?.message || err.message || "Không thể tải chi tiết người dùng");
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, userId]);

  if (!isOpen) return null;

  const handleCopyKeycloakId = () => {
    if (userDetail?.keycloakId) {
      navigator.clipboard.writeText(userDetail.keycloakId);
      setCopiedKeycloakId(true);
      setTimeout(() => setCopiedKeycloakId(false), 2000);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "—";
    try {
      return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(dateString));
    } catch {
      return dateString;
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-100 max-w-2xl w-full overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-extrabold text-slate-900">Chi Tiết Người Dùng</h3>
              <p className="text-xs text-slate-500">Tra cứu hồ sơ cá nhân và sổ địa chỉ giao hàng</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 max-h-[75vh] overflow-y-auto space-y-6">
          {loading ? (
            <div className="py-12 flex flex-col items-center justify-center text-slate-400 gap-3">
              <RefreshCw className="w-8 h-8 animate-spin text-primary" />
              <p className="text-xs font-semibold">Đang tải dữ liệu hồ sơ...</p>
            </div>
          ) : error ? (
            <div className="p-4 rounded-xl bg-red-50 text-red-600 text-xs font-semibold">
              {error}
            </div>
          ) : userDetail ? (
            <>
              {/* User Identity Banner */}
              <div className="flex items-center gap-4 p-4 rounded-2xl bg-slate-50 border border-slate-100">
                {userDetail.avatarUrl ? (
                  <img
                    src={userDetail.avatarUrl}
                    alt={userDetail.fullName}
                    referrerPolicy="no-referrer"
                    className="w-16 h-16 rounded-2xl object-cover ring-2 ring-primary/20 flex-shrink-0"
                  />
                ) : (
                  <div className="w-16 h-16 rounded-2xl bg-primary text-white font-black text-xl flex items-center justify-center flex-shrink-0 shadow-sm">
                    {userDetail.fullName ? userDetail.fullName[0].toUpperCase() : "U"}
                  </div>
                )}

                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <h4 className="text-base font-black text-slate-900 truncate">
                      {userDetail.fullName || userDetail.username}
                    </h4>
                    {isSelf && (
                      <span className="px-1.5 py-0.5 rounded-full bg-indigo-100 text-indigo-700 text-[9px] font-black uppercase tracking-wide">
                        Bạn
                      </span>
                    )}
                    {userDetail.roles?.includes("ADMIN") ? (
                      <span className="px-2 py-0.5 rounded-md bg-red-100 text-primary font-bold text-[10px] uppercase">
                        ADMIN
                      </span>
                    ) : (
                      <span className="px-2 py-0.5 rounded-md bg-slate-200 text-slate-700 font-bold text-[10px] uppercase">
                        USER
                      </span>
                    )}
                  </div>
                  <p className="text-xs font-mono text-slate-500 mt-0.5">@{userDetail.username}</p>

                  <div className="flex items-center gap-2 mt-2">
                    {userDetail.isActive ? (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 text-[10px] font-bold">
                        <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                        Đang hoạt động
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-100 text-red-700 text-[10px] font-bold">
                        <Lock className="w-3 h-3 text-red-600" />
                        Tài khoản bị khóa
                      </span>
                    )}
                  </div>
                </div>
              </div>

              {/* Thông tin liên hệ & hệ thống */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 text-xs">
                <div className="p-3.5 rounded-xl border border-slate-100 bg-white">
                  <span className="text-[11px] font-semibold text-slate-400 block mb-1">Email:</span>
                  <div className="flex items-center gap-2 font-semibold text-slate-800 truncate">
                    <Mail className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                    <span className="truncate">{userDetail.email}</span>
                  </div>
                </div>

                <div className="p-3.5 rounded-xl border border-slate-100 bg-white">
                  <span className="text-[11px] font-semibold text-slate-400 block mb-1">
                    Số điện thoại:
                  </span>
                  <div className="flex items-center gap-2 font-mono font-semibold text-slate-800">
                    <Phone className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                    <span>{userDetail.phoneNumber || "Chưa cập nhật"}</span>
                  </div>
                </div>

                <div className="p-3.5 rounded-xl border border-slate-100 bg-white sm:col-span-2">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-[11px] font-semibold text-slate-400">Keycloak UUID:</span>
                    <button
                      onClick={handleCopyKeycloakId}
                      className="text-[11px] font-bold text-primary hover:underline inline-flex items-center gap-1"
                    >
                      {copiedKeycloakId ? (
                        <>
                          <Check className="w-3 h-3 text-emerald-600" />
                          <span className="text-emerald-600">Đã copy!</span>
                        </>
                      ) : (
                        <>
                          <Copy className="w-3 h-3" />
                          <span>Copy UUID</span>
                        </>
                      )}
                    </button>
                  </div>
                  <p className="font-mono text-slate-700 text-xs bg-slate-50 p-2 rounded-lg border border-slate-100 truncate">
                    {userDetail.keycloakId}
                  </p>
                </div>

                <div className="p-3.5 rounded-xl border border-slate-100 bg-white">
                  <span className="text-[11px] font-semibold text-slate-400 block mb-1">
                    Ngày đăng ký:
                  </span>
                  <div className="flex items-center gap-2 font-medium text-slate-700">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span>{formatDate(userDetail.createdAt)}</span>
                  </div>
                </div>

                <div className="p-3.5 rounded-xl border border-slate-100 bg-white">
                  <span className="text-[11px] font-semibold text-slate-400 block mb-1">
                    Cập nhật lần cuối:
                  </span>
                  <div className="flex items-center gap-2 font-medium text-slate-700">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span>{formatDate(userDetail.updatedAt)}</span>
                  </div>
                </div>
              </div>

              {/* Sổ địa chỉ giao hàng */}
              <div>
                <div className="flex items-center justify-between mb-3">
                  <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-primary" />
                    Sổ Địa Chỉ Giao Hàng ({userDetail.addresses?.length || 0})
                  </h4>
                </div>

                {userDetail.addresses && userDetail.addresses.length > 0 ? (
                  <div className="space-y-2.5">
                    {userDetail.addresses.map((addr) => (
                      <div
                        key={addr.id}
                        className={`p-3.5 rounded-xl border text-xs transition-all ${
                          addr.isDefault
                            ? "bg-red-50/30 border-red-200"
                            : "bg-slate-50/50 border-slate-100"
                        }`}
                      >
                        <div className="flex items-center justify-between mb-1.5">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-slate-900">
                              {addr.recipientName || "Người nhận"}
                            </span>
                            <span className="text-slate-400">|</span>
                            <span className="font-mono text-slate-600">{addr.phoneNumber}</span>
                          </div>
                          <div className="flex items-center gap-1.5">
                            {addr.addressType === "OFFICE" ? (
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-blue-50 text-blue-700 font-semibold text-[10px]">
                                <Building className="w-3 h-3" />
                                Văn phòng
                              </span>
                            ) : (
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 font-semibold text-[10px]">
                                <Home className="w-3 h-3" />
                                Nhà riêng
                              </span>
                            )}
                            {addr.isDefault && (
                              <span className="px-2 py-0.5 rounded-md bg-primary text-white font-bold text-[10px]">
                                Mặc định
                              </span>
                            )}
                          </div>
                        </div>
                        <p className="text-slate-600 leading-relaxed font-medium">
                          {addr.fullAddress ||
                            `${addr.streetAddress}, ${addr.ward}, ${addr.district}, ${addr.province}`}
                        </p>
                        {addr.notes && (
                          <p className="text-[11px] text-slate-400 italic mt-1">
                            Ghi chú: {addr.notes}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 text-center text-xs text-slate-400">
                    Người dùng này chưa thiết lập địa chỉ nhận hàng nào.
                  </div>
                )}
              </div>
            </>
          ) : null}
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between">
          {userDetail && (
            <Button
              variant={userDetail.isActive ? "outline" : "primary"}
              size="sm"
              disabled={isSelf && userDetail.isActive}
              title={
                isSelf && userDetail.isActive
                  ? "Bạn không thể tự khóa tài khoản của chính mình"
                  : ""
              }
              className={
                isSelf && userDetail.isActive
                  ? "text-slate-300 border-slate-200 cursor-not-allowed opacity-50 hover:bg-transparent"
                  : userDetail.isActive
                  ? "text-red-600 border-red-200 hover:bg-red-50"
                  : ""
              }
              onClick={() => {
                if (isSelf && userDetail.isActive) return;
                onToggleStatus(userDetail);
                onClose();
              }}
            >
              {userDetail.isActive ? (
                <>
                  <Lock className="w-3.5 h-3.5 mr-1.5" />
                  Khóa tài khoản này
                </>
              ) : (
                <>
                  <Unlock className="w-3.5 h-3.5 mr-1.5" />
                  Mở khóa tài khoản này
                </>
              )}
            </Button>
          )}

          <Button variant="outline" size="sm" onClick={onClose} className="ml-auto">
            Đóng
          </Button>
        </div>
      </div>
    </div>
  );
}
