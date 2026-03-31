import { ChevronLeft, ChevronRight } from 'lucide-react';

/**
 * Standalone Pagination Component
 * 
 * Dùng cho các bảng tự tạo (không dùng DataTable),
 * hỗ trợ client-side slicing.
 * 
 * @param {number} currentPage - Trang hiện tại (0-indexed)
 * @param {number} totalPages - Tổng số trang
 * @param {number} totalItems - Tổng số phần tử
 * @param {function} onPageChange - Callback khi đổi trang (nhận page 0-indexed)
 * @param {number} maxVisiblePages - Số nút trang tối đa hiển thị (mặc định 5)
 */
export default function Pagination({
  currentPage = 0,
  totalPages = 1,
  totalItems,
  onPageChange,
  maxVisiblePages = 5,
}) {
  if (totalPages <= 1) return null;

  const startPage = Math.max(
    0,
    Math.min(currentPage - Math.floor(maxVisiblePages / 2), totalPages - maxVisiblePages)
  );

  const pages = [];
  for (let i = 0; i < Math.min(totalPages, maxVisiblePages); i++) {
    const pageNum = startPage + i;
    if (pageNum >= totalPages) break;
    pages.push(pageNum);
  }

  return (
    <div className="flex flex-col items-center gap-2 px-4 py-3 border-t border-dark-700/50 bg-dark-800/30">
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage <= 0}
          className="p-1.5 rounded-lg text-dark-400 hover:bg-dark-700/50 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
        >
          <ChevronLeft size={16} />
        </button>
        {pages.map((pageNum) => (
          <button
            key={pageNum}
            onClick={() => onPageChange(pageNum)}
            className={`w-8 h-8 rounded-lg text-xs font-medium transition-all cursor-pointer ${
              pageNum === currentPage
                ? 'bg-primary-600 text-white shadow-md shadow-primary-500/20'
                : 'text-dark-400 hover:bg-dark-700/50 hover:text-white'
            }`}
          >
            {pageNum + 1}
          </button>
        ))}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="p-1.5 rounded-lg text-dark-400 hover:bg-dark-700/50 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
