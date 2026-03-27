import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate, useOutletContext } from 'react-router-dom';
import {
  ArrowLeft,
  BookOpen,
  Eye,
  Layers,
  Calendar,
  FileText,
  Palette,
  Check,
  XCircle,
  RefreshCw
} from 'lucide-react';
import adminService from '../../services/adminService';
import ChapterViewer from '../uploader/ChapterViewer';
import ActionModal from '../../components/ActionModal';

// Status badge
const statusConfig = {
  APPROVED: { label: 'Đã duyệt', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' },
  PENDING: { label: 'Chờ duyệt', bg: 'bg-amber-500/15', text: 'text-amber-400', dot: 'bg-amber-400' },
  REJECTED: { label: 'Từ chối', bg: 'bg-red-500/15', text: 'text-red-400', dot: 'bg-red-400' },
  ONGOING: { label: 'Đang tiến hành', bg: 'bg-blue-500/15', text: 'text-blue-400', dot: 'bg-blue-400' },
  COMPLETED: { label: 'Hoàn thành', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' }
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

function ChapterSkeletonRow() {
  return (
    <tr className="border-b border-dark-700/30 animate-pulse">
      <td className="px-4 py-3"><div className="h-4 w-32 rounded bg-dark-700" /></td>
      <td className="px-4 py-3"><div className="h-4 w-24 rounded bg-dark-700" /></td>
      <td className="px-4 py-3"><div className="h-4 w-28 rounded bg-dark-700" /></td>
      <td className="px-4 py-3"><div className="h-8 w-48 rounded bg-dark-700" /></td>
    </tr>
  );
}

export default function AdminComicApprovalPage() {
  const { comicId } = useParams();
  const navigate = useNavigate();
  const { addToast } = useOutletContext();

  const [comic, setComic] = useState(null);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modals state
  const [selectedChapterViewer, setSelectedChapterViewer] = useState(null);
  const [rejectModal, setRejectModal] = useState({ open: false, log: null });
  const [rejectReason, setRejectReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      // Fetch comic details & pending logs in parallel
      const [comicData, allLogs] = await Promise.all([
        adminService.getComicById(comicId),
        adminService.getPendingLogs()
      ]);
      setComic(comicData);
      
      // Filter logs for this specific comic
      const comicLogs = (Array.isArray(allLogs) ? allLogs : []).filter(
        (log) => String(log.comicId) === String(comicId)
      );
      setLogs(comicLogs);
    } catch (err) {
      console.error('Error fetching data:', err);
      // addToast('Lỗi khi tải dữ liệu truyện.', 'error'); // Optional
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [comicId]);

  // Actions
  const handleApprove = async (log) => {
    try {
      await adminService.reviewLog(log.id, { status: 'APPROVED' });
      addToast(`Đã duyệt chương "${log.chapterTitle}" thành công!`, 'success');
      fetchData(); // Refresh list to remove the approved chapter
    } catch (err) {
      console.error('Approve error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi duyệt. Vui lòng thử lại.', 'error');
    }
  };

  const openRejectModal = (log) => {
    setRejectModal({ open: true, log });
    setRejectReason('');
  };

  const handleReject = async () => {
    if (!rejectReason.trim()) {
      addToast('Vui lòng nhập lý do từ chối.', 'error');
      return;
    }
    setIsSubmitting(true);
    try {
      await adminService.reviewLog(rejectModal.log.id, {
        status: 'REJECTED',
        reason: rejectReason.trim(),
      });
      addToast(`Đã từ chối chương "${rejectModal.log.chapterTitle}".`, 'success');
      setRejectModal({ open: false, log: null });
      fetchData(); // Refresh list
    } catch (err) {
      console.error('Reject error:', err);
      addToast(err.response?.data?.message || 'Lỗi khi từ chối. Vui lòng thử lại.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return `${d.toLocaleDateString('vi-VN')} ${d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}`;
  };

  return (
    <div className="space-y-6">
      {/* Chapter Viewer */}
      <ChapterViewer
        isOpen={!!selectedChapterViewer}
        onClose={() => setSelectedChapterViewer(null)}
        chapter={selectedChapterViewer}
      />

      {/* Reject Modal */}
      <ActionModal
        isOpen={rejectModal.open}
        onClose={() => setRejectModal({ open: false, log: null })}
        title="Từ chối chương"
        onConfirm={handleReject}
        confirmText="Xác nhận từ chối"
        confirmVariant="danger"
        isLoading={isSubmitting}
      >
        <div className="space-y-4">
          {rejectModal.log && (
            <div className="bg-dark-800/50 rounded-lg p-3 border border-dark-700/30">
              <p className="text-xs text-dark-500 mb-1">Nội dung từ chối</p>
              <p className="text-sm text-white font-medium">
                {rejectModal.log.chapterTitle}
              </p>
            </div>
          )}
          <div>
            <label className="block text-sm font-medium text-dark-300 mb-2">
              Lý do từ chối <span className="text-red-400">*</span>
            </label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Nhập lý do từ chối chương này..."
              rows={4}
              className="w-full px-4 py-3 bg-dark-800 border border-dark-700/50 rounded-lg text-sm text-dark-200
                placeholder:text-dark-500 focus:outline-none focus:border-red-500/50 focus:ring-1 focus:ring-red-500/20
                transition-all resize-none z-50 relative"
            />
          </div>
        </div>
      </ActionModal>

      {/* Header */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/admin/approval-queue')}
            className="p-2 rounded-lg hover:bg-dark-800 text-dark-400 hover:text-white transition-all cursor-pointer"
          >
            <ArrowLeft size={20} />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-white">
              {loading ? (
                <div className="h-7 w-48 rounded bg-dark-700 animate-pulse" />
              ) : (
                comic?.title || 'Không tìm thấy truyện'
              )}
            </h1>
            <p className="text-sm text-dark-400 mt-1">Chi tiết & Phê duyệt chương</p>
          </div>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium
            bg-dark-800 text-dark-300 border border-dark-700/50
            hover:bg-dark-700/50 hover:text-white transition-all disabled:opacity-50 cursor-pointer"
        >
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
          Làm mới
        </button>
      </div>

      {/* Comic Info Card */}
      {!loading && comic && (
        <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl p-6">
          <div className="flex flex-col sm:flex-row gap-6">
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

      {!loading && !comic && (
        <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl p-12 text-center">
          <BookOpen size={40} className="text-dark-600 mx-auto mb-3" />
          <p className="text-dark-400">Không tìm thấy truyện này.</p>
        </div>
      )}

      {/* Chapters Waiting for Approval Section */}
      {comic && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-white">Danh sách chương chờ duyệt ({logs.length})</h2>
          </div>

          <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl overflow-hidden">
            {loading ? (
              <table className="w-full">
                <thead>
                  <tr className="border-b border-dark-700/50 text-left">
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Tiêu đề chương</th>
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Người tải</th>
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Thời gian</th>
                    <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider text-center">Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {[...Array(3)].map((_, i) => <ChapterSkeletonRow key={i} />)}
                </tbody>
              </table>
            ) : logs.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="w-16 h-16 rounded-2xl bg-dark-800 border border-dark-700/50 flex items-center justify-center mb-4">
                  <Check size={28} className="text-emerald-500" />
                </div>
                <h3 className="text-base font-semibold text-dark-300 mb-1.5">
                  Đã hoàn tất kiểm duyệt
                </h3>
                <p className="text-sm text-dark-500 max-w-xs mb-5">
                  Truyện này không còn chương nào đang chờ phê duyệt.
                </p>
                <button
                  onClick={() => navigate('/admin/approval-queue')}
                  className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-dark-800 text-white text-sm border border-dark-700 font-semibold transition-all cursor-pointer hover:bg-dark-700"
                >
                  <ArrowLeft size={16} />
                  Quay lại Hàng đợi
                </button>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-dark-700/50 text-left">
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Tiêu đề chương</th>
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Người tải</th>
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider">Thời gian tải</th>
                      <th className="px-4 py-3 text-xs font-semibold text-dark-400 uppercase tracking-wider text-center w-64">Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    {logs
                      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
                      .map((log) => (
                        <tr
                          key={log.id}
                          className="border-b border-dark-700/20 hover:bg-dark-800/50 transition-colors duration-150"
                        >
                          <td className="px-4 py-4">
                            <span className="text-sm font-medium text-white">
                              {log.chapterTitle}
                            </span>
                          </td>
                          <td className="px-4 py-4">
                            <span className="text-sm text-dark-300">
                              {log.uploaderName || '—'}
                            </span>
                          </td>
                          <td className="px-4 py-4">
                            <span className="text-sm text-dark-400 tabular-nums">
                              {formatDateTime(log.createdAt)}
                            </span>
                          </td>
                          <td className="px-4 py-4">
                            <div className="flex items-center justify-center gap-2">
                              <button
                                onClick={() => setSelectedChapterViewer({ id: log.chapterId, title: log.chapterTitle })}
                                className="inline-flex items-center justify-center p-2 rounded-lg text-primary-400 bg-primary-600/10 border border-primary-500/20 hover:bg-primary-600/20 hover:border-primary-500/40 transition-all cursor-pointer"
                                title="Xem nội dung chương"
                              >
                                <Eye size={16} />
                              </button>
                              <button
                                onClick={() => handleApprove(log)}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                                  bg-emerald-600/15 text-emerald-400 border border-emerald-500/20
                                  hover:bg-emerald-600/25 hover:border-emerald-500/40
                                  transition-all cursor-pointer"
                              >
                                <Check size={14} />
                                Duyệt
                              </button>
                              <button
                                onClick={() => openRejectModal(log)}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                                  bg-red-600/15 text-red-400 border border-red-500/20
                                  hover:bg-red-600/25 hover:border-red-500/40
                                  transition-all cursor-pointer"
                              >
                                <XCircle size={14} />
                                Từ chối
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
