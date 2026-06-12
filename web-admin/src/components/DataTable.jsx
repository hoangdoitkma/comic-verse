import { ChevronLeft, ChevronRight, AlertTriangle, Inbox } from 'lucide-react';

/**
 * Reusable DataTable Component
 *
 * @param {Object[]} columns - Cấu hình cột: [{ key, label, render?, minWidth?, align? }]
 *   - key: tên field trong data object
 *   - label: tiêu đề cột
 *   - render: (value, row) => JSX (tuỳ chỉnh hiển thị)
 *   - minWidth: chiều rộng tối thiểu (px string, ví dụ '120px')
 *   - align: 'left' | 'center' | 'right'
 * @param {Object[]} data - Mảng dữ liệu hiển thị
 * @param {boolean} isLoading - Trạng thái đang tải
 * @param {boolean} isError - Trạng thái lỗi
 * @param {string} errorMessage - Thông báo lỗi tuỳ chỉnh
 * @param {string} emptyMessage - Thông báo khi không có dữ liệu
 * @param {Object} pagination - { page, totalPages, totalElements, onPageChange }
 * @param {number} skeletonRows - Số hàng skeleton hiển thị khi loading (mặc định 5)
 */
export default function DataTable({
  columns = [],
  data = [],
  isLoading = false,
  isError = false,
  errorMessage = 'Đã xảy ra lỗi khi tải dữ liệu.',
  emptyMessage = 'Không có dữ liệu để hiển thị.',
  pagination,
  skeletonRows = 6,
}) {
  const alignClass = (align) => {
    if (align === 'center') return 'text-center';
    if (align === 'right') return 'text-right';
    return 'text-left';
  };

  // ======= Skeleton Loading =======
  const renderSkeleton = () => (
    <>
      {Array.from({ length: skeletonRows }).map((_, rowIdx) => (
        <tr key={`skeleton-${rowIdx}`} className="border-b border-dark-700/30">
          {columns.map((col, colIdx) => (
            <td key={`skel-${rowIdx}-${colIdx}`} className="px-4 py-3.5">
              <div className="h-4 bg-dark-700/50 rounded-md animate-pulse" style={{ width: `${60 + Math.random() * 30}%` }} />
            </td>
          ))}
        </tr>
      ))}
    </>
  );

  // ======= Error State =======
  const renderError = () => (
    <tr>
      <td colSpan={columns.length} className="px-4 py-16 text-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-full bg-red-500/10 flex items-center justify-center">
            <AlertTriangle size={24} className="text-red-400" />
          </div>
          <p className="text-dark-400 text-sm">{errorMessage}</p>
        </div>
      </td>
    </tr>
  );

  // ======= Empty State =======
  const renderEmpty = () => (
    <tr>
      <td colSpan={columns.length} className="px-4 py-16 text-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-full bg-dark-700/30 flex items-center justify-center">
            <Inbox size={24} className="text-dark-500" />
          </div>
          <p className="text-dark-400 text-sm">{emptyMessage}</p>
        </div>
      </td>
    </tr>
  );

  // ======= Data Rows =======
  const renderRows = () => {
    const rows = data.map((row, rowIdx) => (
      <tr
        key={row.id || rowIdx}
        className="border-b border-dark-700/30 hover:bg-dark-800/50 transition-colors duration-150"
      >
        {columns.map((col) => (
          <td
            key={`${row.id || rowIdx}-${col.key}`}
            className={`px-4 py-3.5 text-sm text-dark-200 ${alignClass(col.align)}`}
            style={col.minWidth ? { minWidth: col.minWidth } : undefined}
          >
            {col.render ? col.render(row[col.key], row) : (row[col.key] ?? '—')}
          </td>
        ))}
      </tr>
    ));

    // Thêm hàng trống để giữ chiều cao bảng cố định
    const emptyRowsCount = skeletonRows - data.length;
    if (pagination && emptyRowsCount > 0) {
      for (let i = 0; i < emptyRowsCount; i++) {
        rows.push(
          <tr key={`empty-${i}`} className="border-b border-dark-700/30">
            {columns.map((col) => (
              <td
                key={`empty-${i}-${col.key}`}
                className="px-4 py-3.5"
                style={col.minWidth ? { minWidth: col.minWidth } : undefined}
              >
                &nbsp;
              </td>
            ))}
          </tr>
        );
      }
    }

    return rows;
  };

  return (
    <div className="bg-dark-900 border border-dark-700/50 rounded-xl overflow-hidden">
      {/* Table */}
      <div className="overflow-x-auto custom-scrollbar">
        <table className="w-full">
          <thead>
            <tr className="bg-dark-800/60 border-b border-dark-700/50">
              {columns.map((col) => (
                <th
                  key={col.key}
                  className={`px-4 py-3 text-xs font-semibold uppercase tracking-wider text-dark-400 ${alignClass(col.align)}`}
                  style={col.minWidth ? { minWidth: col.minWidth } : undefined}
                >
                  {col.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading
              ? renderSkeleton()
              : isError
                ? renderError()
                : data.length === 0
                  ? renderEmpty()
                  : renderRows()}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {pagination && !isLoading && !isError && data.length > 0 && (
        <div className="flex items-center justify-between px-4 py-3 border-t border-dark-700/50 bg-dark-800/30">
          <span className="text-xs text-dark-500">
            {pagination.totalElements != null
              ? `Tổng cộng ${pagination.totalElements} mục`
              : `Trang ${pagination.page + 1} / ${pagination.totalPages || 1}`}
          </span>
          <div className="flex items-center gap-1">
            <button
              onClick={() => pagination.onPageChange(pagination.page - 1)}
              disabled={pagination.page <= 0}
              className="p-1.5 rounded-lg text-dark-400 hover:bg-dark-700/50 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
            >
              <ChevronLeft size={16} />
            </button>
            {/* Page numbers */}
            {Array.from({ length: Math.min(pagination.totalPages || 1, 5) }).map((_, i) => {
              const startPage = Math.max(
                0,
                Math.min(pagination.page - 2, (pagination.totalPages || 1) - 5)
              );
              const pageNum = startPage + i;
              if (pageNum >= (pagination.totalPages || 1)) return null;
              return (
                <button
                  key={pageNum}
                  onClick={() => pagination.onPageChange(pageNum)}
                  className={`w-8 h-8 rounded-lg text-xs font-medium transition-all cursor-pointer ${
                    pageNum === pagination.page
                      ? 'bg-primary-600 text-white shadow-md shadow-primary-500/20'
                      : 'text-dark-400 hover:bg-dark-700/50 hover:text-white'
                  }`}
                >
                  {pageNum + 1}
                </button>
              );
            })}
            <button
              onClick={() => pagination.onPageChange(pagination.page + 1)}
              disabled={pagination.page >= (pagination.totalPages || 1) - 1}
              className="p-1.5 rounded-lg text-dark-400 hover:bg-dark-700/50 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
            >
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
