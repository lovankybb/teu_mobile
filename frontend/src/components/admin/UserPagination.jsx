import React from "react";
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";

export default function UserPagination({
  pageNumber,
  pageSize,
  totalElements,
  totalPages,
  onPageChange,
  onPageSizeChange,
}) {
  if (totalElements === 0) return null;

  const startItem = pageNumber * pageSize + 1;
  const endItem = Math.min((pageNumber + 1) * pageSize, totalElements);

  // Tính toán dải số trang hiển thị
  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      for (let i = 0; i < totalPages; i++) pages.push(i);
    } else {
      let start = Math.max(0, pageNumber - 2);
      let end = Math.min(totalPages - 1, start + maxVisible - 1);

      if (end - start < maxVisible - 1) {
        start = Math.max(0, end - maxVisible + 1);
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    return pages;
  };

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 py-3 px-2 text-xs text-slate-600">
      {/* Thông tin số lượng hiển thị */}
      <div className="flex items-center gap-3">
        <span>
          Hiển thị <strong className="text-slate-900">{startItem}</strong> -{" "}
          <strong className="text-slate-900">{endItem}</strong> trong tổng số{" "}
          <strong className="text-slate-900">{totalElements.toLocaleString()}</strong> người dùng
        </span>

        {/* Lựa chọn số mục trên mỗi trang */}
        <div className="flex items-center gap-1.5 ml-2">
          <span className="text-slate-400">/ trang:</span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            className="px-2 py-1 bg-white border border-slate-200 rounded-lg text-slate-700 outline-none focus:border-primary text-xs font-semibold"
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
            <option value={50}>50</option>
          </select>
        </div>
      </div>

      {/* Nút phân trang */}
      <div className="flex items-center gap-1">
        {/* Về trang đầu */}
        <button
          onClick={() => onPageChange(0)}
          disabled={pageNumber === 0}
          title="Trang đầu"
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronsLeft className="w-4 h-4" />
        </button>

        {/* Trang trước */}
        <button
          onClick={() => onPageChange(pageNumber - 1)}
          disabled={pageNumber === 0}
          title="Trang trước"
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>

        {/* Các số trang */}
        {getPageNumbers().map((p) => {
          const isCurrent = p === pageNumber;
          return (
            <button
              key={p}
              onClick={() => onPageChange(p)}
              className={`min-w-[32px] h-8 px-2 rounded-lg font-bold transition-all ${
                isCurrent
                  ? "bg-primary text-white shadow-sm shadow-primary/20"
                  : "bg-white border border-slate-200 text-slate-700 hover:bg-slate-50"
              }`}
            >
              {p + 1}
            </button>
          );
        })}

        {/* Trang sau */}
        <button
          onClick={() => onPageChange(pageNumber + 1)}
          disabled={pageNumber >= totalPages - 1}
          title="Trang sau"
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronRight className="w-4 h-4" />
        </button>

        {/* Trang cuối */}
        <button
          onClick={() => onPageChange(totalPages - 1)}
          disabled={pageNumber >= totalPages - 1}
          title="Trang cuối"
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronsRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
