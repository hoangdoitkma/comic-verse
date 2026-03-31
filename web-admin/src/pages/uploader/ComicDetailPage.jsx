import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Plus,
  BookOpen,
  Eye,
  Layers,
  Calendar,
  FileText,
  Palette,
  Lock,
  Unlock,
  Upload,
  Trash2,
  AlertCircle
} from 'lucide-react';
import React from 'react';
import comicService from '../../services/comicService';
import CreateChapterModal from './CreateChapterModal';
import ChapterViewer from './ChapterViewer';
import { ToastContainer, useToast } from '../../components/Toast';
import Pagination from '../../components/Pagination';

// Status badge (same as ComicsPage)
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
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-500/15 text-amber-400">
        <Lock size={10} /> VIP
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-500/15 text-emerald-400">
      <Unlock size={10} /> Free
    </span>
  );
}

// Skeleton for chapter table row
function ChapterSkeletonRow() {
  return (
    <tr className="border-b border-dark-700/30 animate-pulse">
      <td className="px-4 py-3"><div className="h-4 w-10 rounded bg-dark-700" /></td>
      <td className="px-4 py-3"><div className="h-4 w-32 rounded bg-dark-700" /></td>
      <td className="px-4 py-3"><div className="h-5 w-14 rounded-full bg-dark-700" /></td>
      <td className="px-4 py-3"><div className="h-5 w-16 rounded-full bg-dark-700" /></td>
    </tr>
  );
}

const ITEMS_PER_PAGE = 20;

export default function ComicDetailPage() {
  const { comicId } = useParams();
  const navigate = useNavigate();
  const [comic, setComic] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loadingComic, setLoadingComic] = useState(true);
  const [loadingChapters, setLoadingChapters] = useState(true);
  const [showChapterModal, setShowChapterModal] = useState(false);
  const [selectedChapter, setSelectedChapter] = useState(null);
  const [deletingChapter, setDeletingChapter] = useState(null);
  const { toasts, addToast, dismissToast } = useToast();
  const [currentPage, setCurrentPage] = useState(0);

  // Fetch comic info (from listing)
  useEffect(() => {
    const fetchComic = async () => {
      try {
        setLoadingComic(true);
        const comics = await comicService.getMyComics();
        const found = (comics || []).find((c) => String(c.id) === String(comicId));
        setComic(found || null);
      } catch (err) {
        console.error('Lỗi khi tải thông tin truyện:', err);
      } finally {
        setLoadingComic(false);
      }
    };
    fetchComic();
  }, [comicId]);

  // Fetch chapters
  useEffect(() => {
    fetchChapters();
  }, [comicId]);

  const fetchChapters = async () => {
    try {
      setLoadingChapters(true);
      const data = await comicService.getChapters(comicId);
      setChapters(data || []);
    } catch (err) {
      console.error('Lỗi khi tải danh sách chương:', err);
      setChapters([]);
    } finally {
      setLoadingChapters(false);
    }
  };

  const handleChapterSuccess = () => {
    addToast('Thêm chương mới thành công!', 'success');
    fetchChapters();
  };

  const handleDeleteDraft = async (chapterId, chapterTitle) => {
    if (!window.confirm(`Bạn có chắc muốn xóa bản nháp bị từ chối của chương "${chapterTitle || chapterId}" không? Hành động này không thể hoàn tác.`)) {
      return;
    }
    try {
      setDeletingChapter(chapterId);
      await comicService.deleteRejectedDraft(chapterId);
      addToast('Xóa bản nháp thành công!', 'success');
      fetchChapters();
    } catch (err) {
      console.error('Lỗi khi xóa bản nháp:', err);
      const msg = err.response?.data?.message || err.message || 'Lỗi không xác định khi xóa bản nháp';
      addToast(msg, 'error');
    } finally {
      setDeletingChapter(null);
    }
  };

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
      {/* Toast */}
      <ToastContainer toasts={toasts} dismissToast={dismissToast} />

      {/* Chapter Viewer */}
      <ChapterViewer
        isOpen={!!selectedChapter}
        onClose={() => setSelectedChapter(null)}
        chapter={selectedChapter}
      />

      {/* Chapter Modal */}
      <CreateChapterModal
        isOpen={showChapterModal}
        onClose={() => setShowChapterModal(false)}
        onSuccess={handleChapterSuccess}
        comicId={comicId}
        contentType={comic?.contentType}
      />

      {/* Back button + Header */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/uploader/comics')}
          className="p-2 rounded-lg hover:bg-dark-800 text-dark-400 hover:text-white transition-all cursor-pointer"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-white">
            {loadingComic ? (
              <div className="h-7 w-48 rounded bg-dark-700 animate-pulse" />
            ) : (
              comic?.title || 'Không tìm thấy truyện'
            )}
          </h1>
          <p className="text-sm text-dark-400 mt-0.5">Chi tiết & Quản lý chương</p>
        </div>
      </div>

      {/* Comic Info Card */}
      {!loadingComic && comic && (
        <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl p-6">
          <div className="flex flex-col sm:flex-row gap-6">
            {/* Thumbnail */}
            <div className="shrink-0">
              {comic.thumbnailUrl ? (
                <img
                  src={comic.thumbnailUrl}
                  alt={comic.title}
                  className="w-32 h-44 rounded-xl object-cover border border-dark-700/50 shadow-lg"
                />
              ) : (
                <div className="w-32 h-44 rounded-xl bg-dark-800 border border-dark-700/50 flex items-center justify-center">
                  <BookOpen size={32} className="text-dark-600" />
                </div>
              )}
            </div>

            {/* Details */}
            <div className="flex-1 space-y-4">
              <div className="flex items-center gap-3 flex-wrap">
                <StatusBadge status={comic.status} />
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-500/15 text-blue-400">
                  <FileText size={12} />
                  {comic.contentType || '—'}
                </span>
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-violet-500/15 text-violet-400">
                  <Palette size={12} />
                  {comic.comicFormat || '—'}
                </span>
              </div>

              {comic.synopsis && (
                <p className="text-sm text-dark-400 leading-relaxed line-clamp-3">
                  {comic.synopsis}
                </p>
              )}

              <div className="flex items-center gap-6 text-sm text-dark-400">
                <span className="flex items-center gap-1.5">
                  <Layers size={14} className="text-dark-500" />
                  {comic.totalChapters ?? 0} chương
                </span>
                <span className="flex items-center gap-1.5">
                  <Eye size={14} className="text-dark-500" />
                  {(comic.viewCount ?? 0).toLocaleString('vi-VN')} lượt xem
                </span>
                <span className="flex items-center gap-1.5">
                  <Calendar size={14} className="text-dark-500" />
                  {formatDate(comic.createdAt)}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Not found */}
      {!loadingComic && !comic && (
        <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl p-12 text-center">
          <BookOpen size={40} className="text-dark-600 mx-auto mb-3" />
          <p className="text-dark-400">Không tìm thấy truyện này.</p>
        </div>
      )}

      {/* Chapters Section */}
      {comic && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-white">Danh sách Chương</h2>
            <div className="flex items-center gap-3">
              <button
                onClick={() => navigate(
                  comic?.contentType === 'NOVEL'
                    ? `/uploader/comics/${comicId}/bulk-upload-novel`
                    : `/uploader/comics/${comicId}/bulk-upload`
                )}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-dark-800 hover:bg-dark-700 text-dark-200 text-sm font-semibold transition-all duration-200 border border-dark-700/50 hover:border-dark-600 hover:scale-[1.02] active:scale-[0.98] cursor-pointer"
              >
                <Upload size={16} />
                Upload Hàng Loạt
              </button>
              <button
                onClick={() => setShowChapterModal(true)}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 hover:scale-[1.02] active:scale-[0.98] cursor-pointer"
              >
                <Plus size={16} />
                Thêm Chương Mới
              </button>
            </div>
          </div>

          <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl overflow-hidden">
            {/* Loading */}
            {loadingChapters && (
              <table className="w-full">
                <thead>
                  <tr className="border-b border-dark-700/50 text-left">
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Chương</th>
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Tiêu đề</th>
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Quyền truy cập</th>
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {[...Array(3)].map((_, i) => <ChapterSkeletonRow key={i} />)}
                </tbody>
              </table>
            )}

            {/* Empty */}
            {!loadingChapters && chapters.length === 0 && (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="w-16 h-16 rounded-2xl bg-dark-800 border border-dark-700/50 flex items-center justify-center mb-4">
                  <Layers size={28} className="text-dark-600" />
                </div>
                <h3 className="text-base font-semibold text-dark-300 mb-1.5">
                  Chưa có chương nào
                </h3>
                <p className="text-sm text-dark-500 max-w-xs mb-5">
                  Bắt đầu thêm chương đầu tiên cho truyện của bạn.
                </p>
                <button
                  onClick={() => setShowChapterModal(true)}
                  className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all cursor-pointer"
                >
                  <Plus size={16} />
                  Thêm Chương Mới
                </button>
              </div>
            )}

            {!loadingChapters && chapters.length > 0 && (
              <>
              <Pagination
                currentPage={currentPage}
                totalPages={Math.ceil(chapters.length / ITEMS_PER_PAGE)}
                totalItems={chapters.length}
                onPageChange={setCurrentPage}
              />
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-dark-700/50 text-left">
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider w-24">Chương</th>
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Tiêu đề</th>
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider w-32">Quyền truy cập</th>
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider w-32">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(() => {
                      const sorted = [...chapters].sort((a, b) => a.chapterNumber - b.chapterNumber);
                      const totalPages = Math.ceil(sorted.length / ITEMS_PER_PAGE);
                      const safePage = Math.min(currentPage, totalPages - 1);
                      const paged = sorted.slice(safePage * ITEMS_PER_PAGE, (safePage + 1) * ITEMS_PER_PAGE);
                      return paged.map((chapter, idx) => (
                        <React.Fragment key={idx}>
                          <tr
                            onClick={() => setSelectedChapter(chapter)}
                            className={`border-b border-dark-700/20 hover:bg-dark-800/50 transition-colors duration-150 cursor-pointer ${chapter.status === 'REJECTED' ? 'bg-red-500/5' : ''}`}
                          >
                            <td className="px-4 py-3">
                              <span className="text-sm font-mono font-medium text-primary-400">
                                #{chapter.chapterNumber}
                              </span>
                            </td>
                            <td className="px-4 py-3">
                              <span className="text-sm text-dark-200">
                                {chapter.title || `Chương ${chapter.chapterNumber}`}
                              </span>
                            </td>
                            <td className="px-4 py-3">
                              <AccessBadge type={chapter.accessType} />
                            </td>
                            <td className="px-4 py-3">
                              <StatusBadge status={chapter.status} />
                            </td>
                          </tr>
                          {chapter.status === 'REJECTED' && (
                            <tr className="border-b border-dark-700/20 bg-red-500/10">
                              <td colSpan={4} className="px-4 py-3">
                                <div className="flex items-start gap-3">
                                  <AlertCircle size={18} className="text-red-400 shrink-0 mt-0.5" />
                                  <div className="flex-1">
                                    <p className="text-sm font-semibold text-red-400 mb-1">Lý do từ chối:</p>
                                    <p className="text-sm text-red-300 leading-relaxed mb-3">
                                      {chapter.rejectReason || 'Không có lý do cụ thể. Vui lòng kiểm tra lại nội dung vi phạm tiêu chuẩn cộng đồng.'}
                                    </p>
                                    <button
                                      onClick={(e) => { e.stopPropagation(); handleDeleteDraft(chapter.id, chapter.title); }}
                                      disabled={deletingChapter === chapter.id}
                                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-500/20 text-red-400 hover:bg-red-500/30 text-sm font-medium transition-all cursor-pointer disabled:opacity-50"
                                    >
                                      {deletingChapter === chapter.id ? (
                                        <div className="w-4 h-4 rounded-full border-2 border-red-400/30 border-t-red-400 animate-spin" />
                                      ) : (
                                        <Trash2 size={15} />
                                      )}
                                      Xóa bản nháp & Dọn dẹp
                                    </button>
                                  </div>
                                </div>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      ));
                    })()}
                  </tbody>
                </table>
              </div>
              <Pagination
                currentPage={currentPage}
                totalPages={Math.ceil(chapters.length / ITEMS_PER_PAGE)}
                totalItems={chapters.length}
                onPageChange={setCurrentPage}
              />
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
