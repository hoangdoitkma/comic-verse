import { useState, useCallback } from 'react';
import { CheckCircle, XCircle, Info, X } from 'lucide-react';

const iconMap = {
  success: CheckCircle,
  error: XCircle,
  info: Info,
};

const styleMap = {
  success: 'bg-emerald-500/15 border-emerald-500/30 text-emerald-300',
  error: 'bg-red-500/15 border-red-500/30 text-red-300',
  info: 'bg-blue-500/15 border-blue-500/30 text-blue-300',
};

const iconStyleMap = {
  success: 'text-emerald-400',
  error: 'text-red-400',
  info: 'text-blue-400',
};

function ToastItem({ toast, onDismiss }) {
  const Icon = iconMap[toast.type] || Info;
  return (
    <div
      className={`flex items-center gap-3 px-4 py-3 rounded-xl border shadow-2xl backdrop-blur-sm
        animate-slide-in ${styleMap[toast.type] || styleMap.info}`}
    >
      <Icon size={18} className={iconStyleMap[toast.type] || iconStyleMap.info} />
      <span className="text-sm font-medium flex-1">{toast.message}</span>
      <button
        onClick={() => onDismiss(toast.id)}
        className="text-dark-400 hover:text-white transition-colors cursor-pointer"
      >
        <X size={14} />
      </button>
    </div>
  );
}

export function ToastContainer({ toasts, dismissToast }) {
  if (!toasts.length) return null;
  return (
    <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2 max-w-sm w-full">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onDismiss={dismissToast} />
      ))}
    </div>
  );
}

export function useToast() {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);

    // Auto dismiss after 3s
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3000);
  }, []);

  const dismissToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return { toasts, addToast, dismissToast };
}
