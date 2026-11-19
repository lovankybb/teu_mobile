import React from "react";
import { Search, X, Plus, RefreshCw, Filter } from "lucide-react";
import Button from "../ui/Button";

export default function UserFilterToolbar({
  search,
  onSearchChange,
  isActive,
  onStatusChange,
  sortBy,
  sortDir,
  onSortChange,
  onRefresh,
  onOpenCreateModal,
  loading,
}) {
  const statusOptions = [
    { label: "Tất cả", value: "" },
    { label: "Đang hoạt động", value: "true" },
    { label: "Đã bị khóa", value: "false" },
  ];

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-5 shadow-sm space-y-4">
      <div className="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3">
        {/* Thanh tìm kiếm */}
        <div className="relative flex-1 max-w-lg">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Tìm theo username, họ tên, email hoặc số điện thoại..."
            className="w-full pl-10 pr-9 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
          />
          {search && (
            <button
              onClick={() => onSearchChange("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Nút tác vụ phải: Làm mới & Thêm người dùng */}
        <div className="flex items-center gap-2.5 self-end lg:self-auto">
          <Button
            variant="outline"
            size="sm"
            onClick={onRefresh}
            disabled={loading}
            className="text-slate-600"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin text-primary" : ""}`} />
            <span className="hidden sm:inline">Làm mới</span>
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={onOpenCreateModal}
            className="font-bold whitespace-nowrap"
          >
            <Plus className="w-4 h-4 mr-1.5" />
            Thêm Người Dùng
          </Button>
        </div>
      </div>

      {/* Dòng bộ lọc trạng thái và sắp xếp */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-slate-100 text-xs">
        {/* Bộ lọc trạng thái */}
        <div className="flex items-center gap-1.5">
          <span className="text-slate-400 font-semibold mr-1 hidden sm:inline flex-shrink-0">
            Trạng thái:
          </span>
          <div className="inline-flex p-1 bg-slate-100 rounded-xl">
            {statusOptions.map((opt) => {
              const selected = isActive === opt.value;
              return (
                <button
                  key={opt.label}
                  onClick={() => onStatusChange(opt.value)}
                  className={`px-3 py-1.5 rounded-lg font-bold transition-all ${
                    selected
                      ? "bg-white text-slate-900 shadow-sm"
                      : "text-slate-500 hover:text-slate-800"
                  }`}
                >
                  {opt.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Bộ sắp xếp */}
        <div className="flex items-center gap-2">
          <span className="text-slate-400 font-semibold hidden sm:inline">Sắp xếp:</span>
          <select
            value={`${sortBy}-${sortDir}`}
            onChange={(e) => {
              const [newSortBy, newSortDir] = e.target.value.split("-");
              onSortChange(newSortBy, newSortDir);
            }}
            className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 outline-none focus:border-primary"
          >
            <option value="createdAt-desc">Mới nhất trước</option>
            <option value="createdAt-asc">Cũ nhất trước</option>
            <option value="username-asc">Username A → Z</option>
            <option value="username-desc">Username Z → A</option>
          </select>
        </div>
      </div>
    </div>
  );
}
