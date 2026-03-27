import { useEffect, useRef } from 'react';
import { X, Loader2 } from 'lucide-react';

/**
 * Reusable ActionModal Component
 *
 * @param {boolean} isOpen - Hiển thị modal
 * @param {Function} onClose - Đóng modal
 * @param {string} title - Tiêu đề modal
 * @param {React.ReactNode} children - Nội dung bên trong modal
 * @param {Function} onConfirm - Hàm xử lý khi confirm
 * @param {string} confirmText - Text nút confirm (mặc định: "Xác nhận")
 * @param {string} confirmVariant - 'primary' | 'danger' (mặc định: 'primary')
 * @param {boolean} isLoading - Trạng thái loading khi submit
 * @param {boolean} hideFooter - Ẩn footer (dùng khi children tự quản lý nút)
 */
export default function ActionModal({
  isOpen,
  onClose,
  title,
  children,
  onConfirm,
  confirmText = 'Xác nhận',
  confirmVariant = 'primary',
  isLoading = false,
  hideFooter = false,
}) {
  const overlayRef = useRef(null);

  // Close on ESC
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && !isLoading) onClose();
    };
    if (isOpen) {
      document.addEventListener('keydown', handleEsc);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.removeEventListener('keydown', handleEsc);
      document.body.style.overflow = '';
    };
  }, [isOpen, isLoading, onClose]);

  if (!isOpen) return null;

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current && !isLoading) {
      onClose();
    }
  };

  const confirmBtnClass =
    confirmVariant === 'danger'
      ? 'bg-red-600 hover:bg-red-500 shadow-red-500/20 focus:ring-red-500/40'
      : 'bg-primary-600 hover:bg-primary-500 shadow-primary-500/20 focus:ring-primary-500/40';

  return (
    <div
      ref={overlayRef}
      onClick={handleOverlayClick}
      className="fixed inset-0 z-[60] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
    >
      <div className="bg-dark-900 border border-dark-700/50 rounded-2xl shadow-2xl shadow-black/50 w-full max-w-lg animate-scale-in">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-dark-700/50">
          <h3 className="text-lg font-semibold text-white">{title}</h3>
          <button
            onClick={onClose}
            disabled={isLoading}
            className="p-1.5 rounded-lg text-dark-400 hover:bg-dark-700/50 hover:text-white transition-all disabled:opacity-50 cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* Body */}
        <div className="px-6 py-5">{children}</div>

        {/* Footer */}
        {!hideFooter && (
          <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-dark-700/50 bg-dark-800/30 rounded-b-2xl">
            <button
              onClick={onClose}
              disabled={isLoading}
              className="px-4 py-2 rounded-lg text-sm font-medium text-dark-300 hover:bg-dark-700/50 hover:text-white transition-all disabled:opacity-50 cursor-pointer"
            >
              Huỷ
            </button>
            <button
              onClick={onConfirm}
              disabled={isLoading}
              className={`px-5 py-2 rounded-lg text-sm font-semibold text-white shadow-lg transition-all disabled:opacity-60 flex items-center gap-2 cursor-pointer focus:outline-none focus:ring-2 ${confirmBtnClass}`}
            >
              {isLoading && <Loader2 size={16} className="animate-spin" />}
              {confirmText}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
