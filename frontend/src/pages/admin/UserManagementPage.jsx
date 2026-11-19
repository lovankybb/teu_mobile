import React, { useState, useEffect, useCallback, useRef } from "react";
import { Link } from "react-router-dom";
import {
  Users,
  CheckCircle2,
  AlertCircle,
  X,
  ArrowLeft,
} from "lucide-react";
import adminUserService from "../../services/adminUserService";
import UserStatsCards from "../../components/admin/UserStatsCards";
import UserFilterToolbar from "../../components/admin/UserFilterToolbar";
import UserTable from "../../components/admin/UserTable";
import UserPagination from "../../components/admin/UserPagination";
import UserDetailModal from "../../components/admin/UserDetailModal";
import UserStatusConfirmModal from "../../components/admin/UserStatusConfirmModal";
import UserRoleModal from "../../components/admin/UserRoleModal";
import UserCreateModal from "../../components/admin/UserCreateModal";
import Button from "../../components/ui/Button";
import { useAuth } from "../../context/AuthContext";

export default function UserManagementPage() {
  const { user: currentUser } = useAuth();

  // ─── States ─────────────────────────────────────────────────────────────
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // Bộ lọc & phân trang
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [isActive, setIsActive] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState("desc");
  const [pageNumber, setPageNumber] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Modals state
  const [detailUserId, setDetailUserId] = useState(null);
  const [statusTargetUser, setStatusTargetUser] = useState(null);
  const [roleTargetUser, setRoleTargetUser] = useState(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Toast thông báo
  const [toast, setToast] = useState(null);
  const toastTimeoutRef = useRef(null);

  const showToast = useCallback((message, type = "success") => {
    if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    setToast({ message, type });
    toastTimeoutRef.current = setTimeout(() => {
      setToast(null);
    }, 3500);
  }, []);

  // ─── Debounce Search ───────────────────────────────────────────────────
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPageNumber(0); // Reset về trang 1 khi search thay đổi
    }, 350);

    return () => clearTimeout(timer);
  }, [search]);

  // ─── Tải Thống Kê ──────────────────────────────────────────────────────
  const loadStats = useCallback(async () => {
    setStatsLoading(true);
    try {
      const res = await adminUserService.getUserStats();
      if (res?.success && res.data) {
        setStats(res.data);
      }
    } catch (err) {
      console.warn("Không thể tải thống kê người dùng:", err.message);
    } finally {
      setStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  // ─── Tải Danh Sách Người Dùng ──────────────────────────────────────────
  const loadUsers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await adminUserService.getUsers({
        search: debouncedSearch,
        isActive: isActive !== "" ? isActive === "true" : null,
        page: pageNumber,
        size: pageSize,
        sortBy,
        sortDir,
      });

      if (res?.success && res.data) {
        setUsers(res.data.content || []);
        setTotalElements(res.data.totalElements || 0);
        setTotalPages(res.data.totalPages || 0);
      }
    } catch (err) {
      showToast(
        err.response?.data?.message || err.message || "Không thể tải danh sách người dùng",
        "error"
      );
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, isActive, pageNumber, pageSize, sortBy, sortDir, showToast]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  // ─── Xử Lý Thao Tác Khóa / Mở Khóa ──────────────────────────────────────
  const handleConfirmStatus = async (user, newStatus) => {
    setActionLoading(true);
    try {
      const res = await adminUserService.updateUserStatus(user.id, newStatus);
      if (res?.success) {
        showToast(
          `${newStatus ? "Mở khóa" : "Khóa"} tài khoản ${user.username} thành công!`,
          "success"
        );
        setStatusTargetUser(null);
        loadUsers();
        loadStats();
      }
    } catch (err) {
      showToast(
        err.response?.data?.message || err.message || "Lỗi khi cập nhật trạng thái tài khoản",
        "error"
      );
    } finally {
      setActionLoading(false);
    }
  };

  // ─── Xử Lý Thao Tác Phân Quyền Vai Trò ─────────────────────────────────
  const handleRoleUpdate = async (user, role, assign) => {
    setActionLoading(true);
    try {
      const res = await adminUserService.updateUserRole(user.id, role, assign);
      if (res?.success) {
        showToast(
          `${assign ? "Gán" : "Gỡ"} vai trò ${role} cho ${user.username} thành công!`,
          "success"
        );
        setRoleTargetUser(null);
        loadUsers();
      }
    } catch (err) {
      showToast(
        err.response?.data?.message || err.message || "Lỗi khi cập nhật vai trò người dùng",
        "error"
      );
    } finally {
      setActionLoading(false);
    }
  };

  // ─── Xử Lý Sau Khi Tạo Người Dùng Mới ──────────────────────────────────
  const handleUserCreated = (newUser) => {
    showToast(`Đã tạo thành công tài khoản: @${newUser.username}!`, "success");
    loadUsers();
    loadStats();
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* Toast Notification */}
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 animate-in slide-in-from-bottom-3 duration-200">
          <div
            className={`px-4 py-3 rounded-2xl shadow-xl flex items-center gap-3 text-xs font-bold text-white border ${
              toast.type === "success"
                ? "bg-slate-900 border-emerald-500/30"
                : "bg-red-600 border-red-700"
            }`}
          >
            {toast.type === "success" ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            ) : (
              <AlertCircle className="w-4 h-4 text-white" />
            )}
            <span>{toast.message}</span>
            <button
              onClick={() => setToast(null)}
              className="ml-2 p-0.5 text-slate-400 hover:text-white"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      )}

      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2.5">
              <Users className="w-7 h-7 text-primary" />
              Quản Trị Người Dùng
            </h1>
            <span className="px-2 py-0.5 rounded-full bg-red-100 text-primary text-[10px] font-black uppercase">
              Module 1
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Quản lý danh sách thành viên, phân quyền vai trò (Admin / Customer) và đồng bộ trạng thái Keycloak.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <Link to="/admin/dashboard">
            <Button variant="outline" size="sm" className="text-slate-600">
              <ArrowLeft className="w-3.5 h-3.5 mr-1.5" />
              Về Tổng quan
            </Button>
          </Link>
        </div>
      </div>

      {/* 3 Thống kê Cards */}
      <UserStatsCards stats={stats} loading={statsLoading} />

      {/* Thanh tìm kiếm & lọc */}
      <UserFilterToolbar
        search={search}
        onSearchChange={setSearch}
        isActive={isActive}
        onStatusChange={(newStatus) => {
          setIsActive(newStatus);
          setPageNumber(0);
        }}
        sortBy={sortBy}
        sortDir={sortDir}
        onSortChange={(newSortBy, newSortDir) => {
          setSortBy(newSortBy);
          setSortDir(newSortDir);
          setPageNumber(0);
        }}
        onRefresh={() => {
          loadUsers();
          loadStats();
          showToast("Đã làm mới dữ liệu người dùng", "success");
        }}
        onOpenCreateModal={() => setIsCreateModalOpen(true)}
        loading={loading}
      />

      {/* Bảng Dữ Liệu Người Dùng */}
      <UserTable
        users={users}
        loading={loading}
        currentUser={currentUser}
        onViewDetail={(u) => setDetailUserId(u.id)}
        onToggleStatus={(u) => setStatusTargetUser(u)}
        onChangeRole={(u) => setRoleTargetUser(u)}
      />

      {/* Phân Trang */}
      <UserPagination
        pageNumber={pageNumber}
        pageSize={pageSize}
        totalElements={totalElements}
        totalPages={totalPages}
        onPageChange={(newPage) => setPageNumber(newPage)}
        onPageSizeChange={(newSize) => {
          setPageSize(newSize);
          setPageNumber(0);
        }}
      />

      {/* Modal 1: Chi tiết người dùng & sổ địa chỉ */}
      <UserDetailModal
        userId={detailUserId}
        isOpen={Boolean(detailUserId)}
        currentUser={currentUser}
        onClose={() => setDetailUserId(null)}
        onToggleStatus={(u) => setStatusTargetUser(u)}
      />

      {/* Modal 2: Xác nhận Khóa / Mở khóa tài khoản */}
      <UserStatusConfirmModal
        isOpen={Boolean(statusTargetUser)}
        user={statusTargetUser}
        currentUser={currentUser}
        onClose={() => setStatusTargetUser(null)}
        onConfirm={handleConfirmStatus}
        loading={actionLoading}
      />

      {/* Modal 3: Phân quyền vai trò (Admin / User) */}
      <UserRoleModal
        isOpen={Boolean(roleTargetUser)}
        user={roleTargetUser}
        currentUser={currentUser}
        onClose={() => setRoleTargetUser(null)}
        onRoleUpdate={handleRoleUpdate}
        loading={actionLoading}
      />

      {/* Modal 4: Tạo tài khoản người dùng mới */}
      <UserCreateModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onUserCreated={handleUserCreated}
      />
    </div>
  );
}
