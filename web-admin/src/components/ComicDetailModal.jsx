import { useState, useEffect, useRef } from 'react';
import {
  X, BookOpen, Eye, Layers, Calendar, FileText, Palette,
  Loader2, ImageOff, ChevronRight
} from 'lucide-react';
import adminService from '../services/adminService';
import comicService from '../services/comicService';
import ChapterViewer from '../pages/uploader/ChapterViewer';

// Status badge (reused from AdminComicApprovalPage pattern)
const statusConfig = {
  APPROVED: { label: 'Đã duyệt', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' },
  PENDING: { label: 'Chờ duyệt', bg: 'bg-amber-500/15', text: 'text-amber-400', dot: 'bg-amber-400' },
  REJECTED: { label: 'Từ chối', bg: 'bg-red-500/15', text: 'text-red-400', dot: 'bg-red-400' },
  ONGOING: { label: 'Đang tiến hành', bg: 'bg-blue-500/15', text: 'text-blue-400', dot: 'bg-blue-400' },
  COMPLETED: { label: 'Hoàn thành', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' },
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

/**
 * ComicDetailModal - Modal xem chi tiết truyện + danh sách chương
 * Reuse ChapterViewer để đọc chương.
 *
 * @param {boolean} isOpen
 * @param {Function} onClose
 * @param {number|string} comicId
 */
export default function ComicDetailModal({ isOpen, onClose, comicId }) {
  const overlayRef = useRef(null);
  const [comic, setComic] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // ChapterViewer state
  const [selectedChapter, setSelectedChapter] = useState(null);

  // Fetch data
  useEffect(() => {
    if (!isOpen || !comicId) return;
    setLoading(true);
    setError('');
    setComic(null);
    setChapters([]);

    Promise.all([
      adminService.getComicById(comicId),
      comicService.getChapters(comicId),
    ])
      .then(([comicData, chaptersData]) => {
        setComic(comicData);
        setChapters(Array.isArray(chaptersData) ? chaptersData : []);
      })
      .catch((err) => {
        console.error('ComicDetailModal error:', err);
        setError('Không thể tải dữ liệu truyện.');
      })
      .finally(() => setLoading(false));
  }, [isOpen, comicId]);

  // ESC to close
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && !selectedChapter) onClose();
    };
    if (isOpen) {
      document.addEventListener('keydown', handleEsc);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.removeEventListener('keydown', handleEsc);
      document.body.style.overflow = '';
    };
  }, [isOpen, onClose, selectedChapter]);

  if (!isOpen) return null;

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) onClose();
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  };

  return (
    <>
      {/* ChapterViewer overlay (highest z-index) */}
      <ChapterViewer
        isOpen={!!selectedChapter}
        onClose={() => setSelectedChapter(null)}
        chapter={selectedChapter}
      />

      {/* Modal overlay */}
      <div
        ref={overlayRef}
        onClick={handleOverlayClick}
        className="fixed inset-0 z-[70] flex items-center justify-center bg-black/70 backdrop-blur-sm p-4"
      >
        <div className="bg-dark-900 border border-dark-700/50 rounded-2xl shadow-2xl shadow-black/50 w-full max-w-3xl max-h-[90vh] flex flex-col animate-scale-in">
          {/* Header */}
          <div className="flex items-center justify-between px-6 py-4 border-b border-dark-700/50 shrink-0">
            <div className="min-w-0">
              <h3 className="text-lg font-semibold text-white truncate">
                {loading ? 'Đang tải...' : comic?.title || 'Chi tiết truyện'}
              </h3>
              <p className="text-xs text-dark-500 mt-0.5">Xem thông tin & đọc chương</p>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-dark-400 hover:bg-dark-700/50 hover:text-white transition-all cursor-pointer shrink-0 ml-4"
            >
              <X size={18} />
            </button>
          </div>

          {/* Scrollable body */}
          <div className="flex-1 overflow-y-auto px-6 py-5 space-y-6">
            {/* Loading */}
            {loading && (
              <div className="flex flex-col items-center justify-center py-16">
                <Loader2 size={32} className="text-primary-400 animate-spin mb-3" />
                <p className="text-dark-400 text-sm">Đang tải dữ liệu truyện...</p>
              </div>
            )}

            {/* Error */}
            {!loading && error && (
              <div className="flex flex-col items-center justify-center py-16">
                <ImageOff size={40} className="text-dark-600 mb-3" />
                <p className="text-dark-400 text-sm">{error}</p>
              </div>
            )}

            {/* Comic Info */}
            {!loading && comic && (
              <>
                <div className="bg-dark-800/50 border border-dark-700/30 rounded-xl p-5">
                  <div className="flex flex-col sm:flex-row gap-5">
                    {/* Thumbnail */}
                    <div className="shrink-0">
                      {comic.thumbnailUrl ? (
                        <img
                          src={comic.thumbnailUrl}
                          alt={comic.title}
                          className="w-28 h-40 rounded-xl object-cover border border-dark-700/50 shadow-lg"
                        />
                      ) : (
                        <div className="w-28 h-40 rounded-xl bg-dark-700 border border-dark-700/50 flex items-center justify-center">
                          <BookOpen size={28} className="text-dark-600" />
                        </div>
                      )}
                    </div>

                    {/* Meta */}
                    <div className="flex-1 space-y-3 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <StatusBadge status={comic.status} />
                        {comic.contentType && (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-blue-500/15 text-blue-400">
                            <FileText size={11} /> {comic.contentType}
                          </span>
                        )}
                        {comic.comicFormat && (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-violet-500/15 text-violet-400">
                            <Palette size={11} /> {comic.comicFormat}
                          </span>
                        )}
                      </div>

                      {comic.synopsis && (
                        <p className="text-sm text-dark-400 leading-relaxed line-clamp-3">
                          {comic.synopsis}
                        </p>
                      )}

                      <div className="flex items-center gap-5 text-sm text-dark-400">
                        <span className="flex items-center gap-1.5">
                          <Layers size={14} className="text-dark-500" />
                          {comic.totalChapters ?? chapters.length} chương
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

                {/* Chapters List */}
                <div>
                  <h4 className="text-sm font-semibold text-dark-300 mb-3">
                    Danh sách chương ({chapters.length})
                  </h4>

                  {chapters.length === 0 ? (
                    <div className="text-center py-10 text-dark-500 text-sm">
                      Truyện này chưa có chương nào.
                    </div>
                  ) : (
                    <div className="bg-dark-800/50 border border-dark-700/30 rounded-xl overflow-hidden">
                      <div className="divide-y divide-dark-700/30">
                        {chapters
                          .sort((a, b) => (a.chapterNumber ?? 0) - (b.chapterNumber ?? 0))
                          .map((ch) => (
                            <button
                              key={ch.id}
                              onClick={() =>
                                setSelectedChapter({
                                  id: ch.id,
                                  title: ch.title || `Chương ${ch.chapterNumber}`,
                                  chapterNumber: ch.chapterNumber,
                                })
                              }
                              className="w-full flex items-center justify-between px-4 py-3 hover:bg-dark-700/40 transition-colors cursor-pointer group text-left"
                            >
                              <div className="min-w-0 flex-1">
                                <p className="text-sm font-medium text-dark-200 group-hover:text-white truncate transition-colors">
                                  {ch.title || `Chương ${ch.chapterNumber}`}
                                </p>
                                <p className="text-xs text-dark-500 mt-0.5">
                                  {formatDate(ch.createdAt)}
                                  {ch.accessType && (
                                    <span className={`ml-2 ${ch.accessType === 'VIP' ? 'text-amber-400' : 'text-dark-500'}`}>
                                      {ch.accessType}
                                    </span>
                                  )}
                                </p>
                              </div>
                              <div className="flex items-center gap-2 ml-3 shrink-0">
                                <span className="text-xs text-primary-400 font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                                  Đọc
                                </span>
                                <ChevronRight
                                  size={16}
                                  className="text-dark-600 group-hover:text-primary-400 transition-colors"
                                />
                              </div>
                            </button>
                          ))}
                      </div>
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
