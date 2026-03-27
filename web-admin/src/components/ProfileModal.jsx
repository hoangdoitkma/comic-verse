import { useState, useEffect } from 'react';
import { X, User, Link as LinkIcon, Save } from 'lucide-react';

export default function ProfileModal({ isOpen, onClose, onSave }) {
  const [displayName, setDisplayName] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isOpen) {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          const user = JSON.parse(userStr);
          setDisplayName(user.displayName || user.email?.split('@')[0] || '');
          setAvatarUrl(user.avatarUrl || '');
        } catch {
          // ignore
        }
      }
    }
  }, [isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      // Cập nhật vào localStorage (chưa có API backend)
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        user.displayName = displayName;
        user.avatarUrl = avatarUrl;
        localStorage.setItem('user', JSON.stringify(user));
      }
      onSave?.({ displayName, avatarUrl });
      onClose();
    } catch (err) {
      console.error('Lỗi khi lưu hồ sơ:', err);
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center">
      {/* Overlay */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative bg-dark-900 border border-dark-700/50 rounded-2xl shadow-2xl w-full max-w-md mx-4 animate-scale-in">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-dark-700/50">
          <h2 className="text-lg font-semibold text-white">Hồ sơ của tôi</h2>
          <button
            onClick={onClose}
            className="text-dark-400 hover:text-white transition-colors cursor-pointer p-1"
          >
            <X size={18} />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Display Name */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
              <User size={14} />
              Tên hiển thị
            </label>
            <input
              type="text"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              placeholder="Nhập tên hiển thị..."
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
            />
          </div>

          {/* Avatar URL */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
              <LinkIcon size={14} />
              Avatar URL
            </label>
            <input
              type="url"
              value={avatarUrl}
              onChange={(e) => setAvatarUrl(e.target.value)}
              placeholder="https://example.com/avatar.jpg"
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
            />
            {/* Avatar preview */}
            {avatarUrl && (
              <div className="mt-3 flex items-center gap-3">
                <img
                  src={avatarUrl}
                  alt="Avatar preview"
                  className="w-12 h-12 rounded-full object-cover border-2 border-dark-700"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <span className="text-xs text-dark-500">Xem trước avatar</span>
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl text-sm font-medium text-dark-400 hover:text-dark-200 hover:bg-dark-800 transition-all cursor-pointer"
            >
              Huỷ
            </button>
            <button
              type="submit"
              disabled={saving}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              <Save size={16} />
              {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
