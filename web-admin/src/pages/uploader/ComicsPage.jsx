import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, BookOpen, Eye, Layers, Search, Lock, Unlock } from 'lucide-react';
import comicService from '../../services/comicService';
import CreateComicModal from './CreateComicModal';
import { ToastContainer, useToast } from '../../components/Toast';

// Badge cho trạng thái truyện
const statusConfig = {
  APPROVED: { label: 'Đã duyệt', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' },
  PENDING: { label: 'Chờ duyệt', bg: 'bg-amber-500/15', text: 'text-amber-400', dot: 'bg-amber-400' },
  REJECTED: { label: 'Từ chối', bg: 'bg-red-500/15', text: 'text-red-400', dot: 'bg-red-400' },
};

function StatusBadge({ status }) {
  const config = statusConfig[status] || statusConfig.PENDING;
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${config.bg} ${config.text}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
}

function AccessBadge({ type }) {
  if (type === 'VIP') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-amber-500/15 text-amber-400 border border-amber-500/20 mt-1">
        <Lock size={10} /> VIP
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-emerald-500/15 text-emerald-400 border border-emerald-500/20 mt-1">
      <Unlock size={10} /> Free
    </span>
  );
}

// Skeleton loader cho table row
function SkeletonRow() {
  return (
    <tr className="border-b border-dark-700/30 animate-pulse">
      <td className="px-4 py-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-16 rounded-lg bg-dark-700" />
          <div className="space-y-2">
            <div className="h-4 w-36 rounded bg-dark-700" />
            <div className="h-3 w-24 rounded bg-dark-700/60" />
          </div>
        </div>
      </td>
      <td className="px-4 py-4"><div className="h-4 w-16 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-6 w-20 rounded-full bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-10 rounded bg-dark-700" /></td>
      <td className="px-4 py-4"><div className="h-4 w-14 rounded bg-dark-700" /></td>
    </tr>
  );
}

// Empty state
function EmptyState({ onAddComic }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-24 h-24 rounded-2xl bg-dark-800 border border-dark-700/50 flex items-center justify-center mb-6">
        <BookOpen size={40} className="text-dark-600" />
      </div>
      <h3 className="text-lg font-semibold text-dark-300 mb-2">
        Chưa có truyện nào
      </h3>
      <p className="text-sm text-dark-500 max-w-sm mb-6">
        Bạn chưa đăng truyện nào. Hãy bắt đầu bằng cách thêm truyện mới để chia sẻ tác phẩm của bạn với cộng đồng.
      </p>
      <button
        onClick={onAddComic}
        className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 hover:shadow-primary-500/30 cursor-pointer"
      >
        <Plus size={18} />
        Thêm truyện mới
      </button>
    </div>
  );
}

export default function ComicsPage() {
  const [comics, setComics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const navigate = useNavigate();
  const { toasts, addToast, dismissToast } = useToast();

  useEffect(() => {
    fetchComics();
  }, []);

  const fetchComics = async () => {
    try {
      setLoading(true);
      const data = await comicService.getMyComics();
      setComics(data || []);
    } catch (error) {
      console.error('Lỗi khi tải danh sách truyện:', error);
      setComics([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSuccess = () => {
    addToast('Tạo truyện mới thành công!', 'success');
    fetchComics();
  };

  const filteredComics = comics.filter((comic) =>
    comic.title?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  return (
    <div className="space-y-6">
      {/* Toast Notifications */}
      <ToastContainer toasts={toasts} dismissToast={dismissToast} />

      {/* Create Comic Modal */}
      <CreateComicModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={handleCreateSuccess}
      />

      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Quản lý Truyện</h1>
          <p className="text-sm text-dark-400 mt-1">
            Quản lý tất cả truyện bạn đã đăng tải
          </p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 hover:shadow-primary-500/30 hover:scale-[1.02] active:scale-[0.98] cursor-pointer"
        >
          <Plus size={18} />
          Thêm truyện mới
        </button>
      </div>

      {/* Stats cards */}
      {!loading && comics.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-dark-800/50 border border-dark-700/40 rounded-xl p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-primary-500/15 flex items-center justify-center">
                <BookOpen size={18} className="text-primary-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-white">{comics.length}</p>
                <p className="text-xs text-dark-400">Tổng truyện</p>
              </div>
            </div>
          </div>
          <div className="bg-dark-800/50 border border-dark-700/40 rounded-xl p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-emerald-500/15 flex items-center justify-center">
                <Layers size={18} className="text-emerald-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-white">
                  {comics.reduce((sum, c) => sum + (c.totalChapters || 0), 0)}
                </p>
                <p className="text-xs text-dark-400">Tổng chương</p>
              </div>
            </div>
          </div>
          <div className="bg-dark-800/50 border border-dark-700/40 rounded-xl p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-violet-500/15 flex items-center justify-center">
                <Eye size={18} className="text-violet-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-white">
                  {comics.reduce((sum, c) => sum + (c.viewCount || 0), 0).toLocaleString('vi-VN')}
                </p>
                <p className="text-xs text-dark-400">Tổng lượt xem</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Table Section */}
      <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl overflow-hidden">
        {/* Search Bar */}
        {!loading && comics.length > 0 && (
          <div className="p-4 border-b border-dark-700/40">
            <div className="relative max-w-sm">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500" />
              <input
                type="text"
                placeholder="Tìm kiếm truyện..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-4 py-2 rounded-lg bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
              />
            </div>
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <table className="w-full">
            <thead>
              <tr className="border-b border-dark-700/50 text-left">
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Truyện</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Loại</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Trạng thái</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Chương</th>
                <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Lượt xem</th>
              </tr>
            </thead>
            <tbody>
              {[...Array(5)].map((_, i) => (
                <SkeletonRow key={i} />
              ))}
            </tbody>
          </table>
        )}

        {/* Empty State */}
        {!loading && comics.length === 0 && <EmptyState onAddComic={() => setShowCreateModal(true)} />}

        {/* Data Table */}
        {!loading && comics.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-dark-700/50 text-left">
                  <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Truyện</th>
                  <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Loại</th>
                  <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Trạng thái</th>
                  <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Chương</th>
                  <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Lượt xem</th>
                  <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Ngày tạo</th>
                </tr>
              </thead>
              <tbody>
                {filteredComics.map((comic) => (
                  <tr
                    key={comic.id}
                    onClick={() => navigate(`/uploader/comics/${comic.id}`)}
                    className="border-b border-dark-700/20 hover:bg-dark-800/50 transition-colors duration-150 cursor-pointer"
                  >
                    {/* Thumbnail + Title */}
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        {comic.thumbnailUrl ? (
                          <img
                            src={comic.thumbnailUrl}
                            alt={comic.title}
                            className="w-12 h-16 rounded-lg object-cover border border-dark-700/50 shadow-sm"
                          />
                        ) : (
                          <div className="w-12 h-16 rounded-lg bg-dark-700 border border-dark-600/50 flex items-center justify-center">
                            <BookOpen size={16} className="text-dark-500" />
                          </div>
                        )}
                        <div className="min-w-0">
                          <p className="text-sm font-medium text-dark-100 truncate max-w-[200px]">
                            {comic.title}
                          </p>
                          <p className="text-xs text-dark-500 mt-0.5">
                            {comic.comicFormat || '—'}
                          </p>
                        </div>
                      </div>
                    </td>

                    {/* Content Type & Access */}
                    <td className="px-4 py-3">
                      <div className="flex flex-col items-start gap-0.5">
                        <span className="text-sm text-dark-300">{comic.contentType || '—'}</span>
                        <AccessBadge type={comic.accessType} />
                      </div>
                    </td>

                    {/* Status */}
                    <td className="px-4 py-3">
                      <StatusBadge status={comic.status} />
                    </td>

                    {/* Chapters */}
                    <td className="px-4 py-3">
                      <span className="text-sm text-dark-300 font-medium">{comic.totalChapters ?? 0}</span>
                    </td>

                    {/* Views */}
                    <td className="px-4 py-3">
                      <span className="text-sm text-dark-300">{(comic.viewCount ?? 0).toLocaleString('vi-VN')}</span>
                    </td>

                    {/* Created Date */}
                    <td className="px-4 py-3">
                      <span className="text-sm text-dark-400">{formatDate(comic.createdAt)}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* No search results */}
            {filteredComics.length === 0 && searchTerm && (
              <div className="py-12 text-center">
                <p className="text-sm text-dark-400">
                  Không tìm thấy truyện nào phù hợp với "<span className="text-dark-200">{searchTerm}</span>"
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
