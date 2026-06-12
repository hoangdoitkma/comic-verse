import { useEffect, useRef, useState } from 'react';
import { BookOpen, ExternalLink, FileText, ImageOff, Loader2, MapPin, X } from 'lucide-react';
import chapterReportService from '../services/chapterReportService';

const reportTypeLabels = {
  IMAGE_NOT_LOADING: 'Lỗi tải ảnh',
  WRONG_CONTENT: 'Sai nội dung chương',
  TYPO_ERROR: 'Lỗi chính tả',
  DUPLICATE_CHAPTER: 'Trùng chương',
  OTHER: 'Khác',
};

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

function getLocationLabel(report) {
  if (report?.pageNumber) return `Trang ${report.pageNumber}`;
  if (typeof report?.paragraphIndex === 'number') return `Đoạn ${report.paragraphIndex + 1}`;
  return 'Chưa có vị trí cụ thể';
}

export default function ReportChapterDetailModal({ isOpen, report, onClose }) {
  const [chapter, setChapter] = useState(null);
  const [error, setError] = useState('');
  const highlightRef = useRef(null);
  const loading = isOpen && !chapter && !error;

  const pageIndex = typeof report?.pageIndex === 'number' ? report.pageIndex : null;
  const paragraphIndex = typeof report?.paragraphIndex === 'number' ? report.paragraphIndex : null;
  const paragraphs = chapter?.content
    ? chapter.content.split('\n').map((p) => p.trim()).filter(Boolean)
    : [];
  const pages = [...(chapter?.pages || [])].sort((a, b) => (a.pageNumber || 0) - (b.pageNumber || 0));
  const isNovel = paragraphs.length > 0 || report?.readerMode === 'NOVEL';

  useEffect(() => {
    if (!isOpen || !report?.chapterId) return;

    let cancelled = false;

    chapterReportService.getChapterDetail(report.chapterId)
      .then((data) => {
        if (!cancelled) {
          setChapter(data);
        }
      })
      .catch((err) => {
        console.error('Report detail error:', err);
        if (!cancelled) {
          setError('Không thể tải nội dung chương. Vui lòng thử lại.');
        }
      });

    return () => {
      cancelled = true;
    };
  }, [isOpen, report?.chapterId]);

  useEffect(() => {
    if (!isOpen) return;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  useEffect(() => {
    if (!loading && highlightRef.current) {
      highlightRef.current.scrollIntoView({ block: 'center', behavior: 'smooth' });
    }
  }, [loading, chapter, pageIndex, paragraphIndex]);

  if (!isOpen || !report) return null;

  const chapterTitle = chapter?.title || report.chapterTitle || `Chương ${report.chapterNumber || report.chapterId}`;
  const reportedImage = pageIndex !== null ? pages[pageIndex]?.imageUrl : null;
  const snapshotDiffers = report.pageImageUrlSnapshot
    && reportedImage
    && report.pageImageUrlSnapshot !== reportedImage;

  return (
    <div className="fixed inset-0 z-[80] bg-black/70 backdrop-blur-sm">
      <div className="h-full w-full bg-dark-950 text-dark-100 flex flex-col">
        <header className="h-16 px-5 border-b border-dark-800 flex items-center justify-between">
          <div className="min-w-0">
            <p className="text-xs text-dark-400">Chi tiết báo cáo lỗi</p>
            <h2 className="text-base font-semibold text-white truncate">{chapterTitle}</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg text-dark-400 hover:text-white hover:bg-dark-800 transition cursor-pointer"
            aria-label="Đóng"
          >
            <X size={20} />
          </button>
        </header>

        <div className="flex-1 min-h-0 grid grid-cols-1 lg:grid-cols-[360px_1fr]">
          <aside className="border-b lg:border-b-0 lg:border-r border-dark-800 overflow-y-auto p-5 space-y-5">
            <section>
              <p className="text-xs uppercase text-dark-500 mb-2">Người báo cáo</p>
              <p className="text-sm text-white">{report.reporterName || 'Không rõ'}</p>
              <p className="text-xs text-dark-500 mt-1">{formatDate(report.createdAt)}</p>
            </section>

            <section className="space-y-3">
              <div>
                <p className="text-xs uppercase text-dark-500 mb-2">Loại lỗi</p>
                <p className="inline-flex px-2.5 py-1 rounded-full bg-red-500/15 text-red-300 text-xs font-medium">
                  {report.typeDescription || reportTypeLabels[report.type] || report.type}
                </p>
              </div>

              <div>
                <p className="text-xs uppercase text-dark-500 mb-2">Vị trí người dùng báo cáo</p>
                <p className="inline-flex items-center gap-2 text-sm text-primary-300">
                  <MapPin size={15} />
                  {getLocationLabel(report)}
                </p>
              </div>

              <div>
                <p className="text-xs uppercase text-dark-500 mb-2">Chi tiết lỗi</p>
                <p className="text-sm leading-6 text-dark-200 whitespace-pre-wrap">
                  {report.reason || 'Người dùng không nhập mô tả thêm.'}
                </p>
              </div>
            </section>

            {report.contentSnapshot && (
              <section>
                <p className="text-xs uppercase text-dark-500 mb-2">Đoạn được báo cáo</p>
                <p className="text-sm leading-6 text-dark-300 bg-dark-900/70 border border-dark-800 rounded-lg p-3">
                  {report.contentSnapshot}
                </p>
              </section>
            )}

            {report.pageImageUrlSnapshot && (
              <section>
                <p className="text-xs uppercase text-dark-500 mb-2">Ảnh tại lúc báo cáo</p>
                <a
                  href={report.pageImageUrlSnapshot}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-2 text-sm text-primary-300 hover:text-primary-200"
                >
                  Mở ảnh gốc
                  <ExternalLink size={14} />
                </a>
                {snapshotDiffers && (
                  <p className="text-xs text-amber-300 mt-2">
                    Ảnh hiện tại của chương khác URL lúc người dùng báo cáo.
                  </p>
                )}
              </section>
            )}

            {report.adminNotes && (
              <section>
                <p className="text-xs uppercase text-dark-500 mb-2">Ghi chú xử lý</p>
                <p className="text-sm leading-6 text-dark-300 whitespace-pre-wrap">{report.adminNotes}</p>
              </section>
            )}
          </aside>

          <main className="min-h-0 overflow-y-auto bg-dark-950">
            {loading && (
              <div className="h-full flex flex-col items-center justify-center text-dark-400">
                <Loader2 size={30} className="animate-spin text-primary-400 mb-3" />
                Đang tải chương...
              </div>
            )}

            {!loading && error && (
              <div className="h-full flex flex-col items-center justify-center text-dark-400">
                <ImageOff size={36} className="mb-3 text-dark-600" />
                {error}
              </div>
            )}

            {!loading && !error && isNovel && (
              <article className="max-w-3xl mx-auto px-5 py-8">
                <div className="mb-8 text-center">
                  <BookOpen size={22} className="mx-auto text-primary-300 mb-3" />
                  <h1 className="text-xl font-semibold text-white">{chapterTitle}</h1>
                  <p className="text-xs text-dark-500 mt-2">{paragraphs.length} đoạn</p>
                </div>
                <div className="space-y-4">
                  {paragraphs.map((paragraph, index) => {
                    const highlighted = paragraphIndex === index;
                    return (
                      <p
                        key={`${index}-${paragraph.slice(0, 20)}`}
                        ref={highlighted ? highlightRef : null}
                        className={`text-[15px] leading-8 rounded-lg px-4 py-2 ${
                          highlighted
                            ? 'bg-amber-500/15 ring-1 ring-amber-400/70 text-white'
                            : 'text-dark-200'
                        }`}
                      >
                        {paragraph}
                      </p>
                    );
                  })}
                </div>
              </article>
            )}

            {!loading && !error && !isNovel && (
              <div className="max-w-4xl mx-auto py-6">
                {pages.length === 0 ? (
                  <div className="py-28 text-center text-dark-500">
                    <ImageOff size={34} className="mx-auto mb-3 text-dark-600" />
                    Chương này chưa có trang ảnh.
                  </div>
                ) : (
                  pages.map((page, index) => {
                    const highlighted = pageIndex === index;
                    return (
                      <div
                        key={`${page.pageNumber}-${page.imageUrl || index}`}
                        ref={highlighted ? highlightRef : null}
                        className={`relative mb-4 mx-3 rounded-lg overflow-hidden ${
                          highlighted ? 'ring-2 ring-amber-400 shadow-lg shadow-amber-500/10' : ''
                        }`}
                      >
                        <div className={`absolute top-3 left-3 z-10 px-2 py-1 rounded text-xs ${
                          highlighted ? 'bg-amber-400 text-black font-semibold' : 'bg-black/60 text-white'
                        }`}>
                          Trang {page.pageNumber || index + 1}
                        </div>
                        {page.imageUrl ? (
                          <img
                            src={page.imageUrl}
                            alt={`Trang ${page.pageNumber || index + 1}`}
                            className="w-full block bg-dark-900"
                            loading={index < 2 ? 'eager' : 'lazy'}
                          />
                        ) : (
                          <div className="h-80 flex items-center justify-center bg-dark-900 text-dark-500">
                            <FileText size={28} className="mr-2" />
                            Trang chưa có ảnh
                          </div>
                        )}
                      </div>
                    );
                  })
                )}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
