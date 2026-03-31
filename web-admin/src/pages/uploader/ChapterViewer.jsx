import { useState, useEffect, useRef } from 'react';
import { X, ChevronUp, Loader2, ImageOff, Maximize2, Minimize2, BookOpen } from 'lucide-react';
import comicService from '../../services/comicService';

export default function ChapterViewer({ isOpen, onClose, chapter }) {
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [loadedImages, setLoadedImages] = useState(new Set());
  const [showScrollTop, setShowScrollTop] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const scrollRef = useRef(null);

  // Detect if this chapter has text content (novel) or image pages (comic)
  const isNovelChapter = !!(chapter?.content && chapter.content.trim().length > 0);

  useEffect(() => {
    if (isOpen && chapter?.id) {
      setLoading(true);
      setError('');
      setPages([]);
      setLoadedImages(new Set());

      // If novel chapter with content, no need to fetch pages
      if (isNovelChapter) {
        setLoading(false);
        return;
      }

      comicService.getChapterPages(chapter.id)
        .then((data) => {
          const filtered = (data || []).filter((p) => p.imageUrl);
          if (filtered.length === 0) {
            setError('Chương này chưa có ảnh nào được tải lên.');
          }
          setPages(filtered);
        })
        .catch((err) => {
          setError('Không thể tải ảnh chương. Vui lòng thử lại.');
          console.error('ChapterViewer error:', err);
        })
        .finally(() => setLoading(false));
    }
  }, [isOpen, chapter?.id, isNovelChapter]);

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    const handleScroll = () => setShowScrollTop(el.scrollTop > 400);
    el.addEventListener('scroll', handleScroll);
    return () => el.removeEventListener('scroll', handleScroll);
  }, [isOpen]);

  // Prevent body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => { document.body.style.overflow = ''; };
  }, [isOpen]);

  const scrollToTop = () => {
    scrollRef.current?.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleImageLoad = (idx) => {
    setLoadedImages((prev) => new Set(prev).add(idx));
  };

  if (!isOpen) return null;

  const chapterTitle = chapter?.title || `Chương ${chapter?.chapterNumber}`;

  // Parse novel content into paragraphs
  const novelParagraphs = isNovelChapter
    ? chapter.content.split('\n').filter((p) => p.trim().length > 0)
    : [];

  return (
    <div className="fixed inset-0 z-[90] flex flex-col bg-dark-950">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 bg-dark-900/95 border-b border-dark-700/50 backdrop-blur-sm shrink-0 z-10">
        <div className="flex items-center gap-3 min-w-0">
          <button
            onClick={onClose}
            className="p-2 rounded-lg hover:bg-dark-800 text-dark-400 hover:text-white transition-all cursor-pointer shrink-0"
          >
            <X size={18} />
          </button>
          <div className="min-w-0">
            <h2 className="text-sm font-semibold text-white truncate">{chapterTitle}</h2>
            <p className="text-xs text-dark-500">
              {loading
                ? 'Đang tải...'
                : isNovelChapter
                  ? `${novelParagraphs.length} đoạn văn • ${chapter.content.length.toLocaleString('vi-VN')} ký tự`
                  : `${pages.length} trang`
              }
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {isNovelChapter && (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-violet-500/15 text-violet-400">
              <BookOpen size={10} />
              Novel
            </span>
          )}
          <button
            onClick={() => setIsFullscreen(!isFullscreen)}
            className="p-2 rounded-lg hover:bg-dark-800 text-dark-400 hover:text-white transition-all cursor-pointer"
            title={isFullscreen ? 'Thu nhỏ' : 'Toàn màn hình'}
          >
            {isFullscreen ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
          </button>
        </div>
      </div>

      {/* Content */}
      <div
        ref={scrollRef}
        className="flex-1 overflow-y-auto"
        style={{ scrollBehavior: 'smooth' }}
      >
        {/* Loading */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-32">
            <Loader2 size={32} className="text-primary-400 animate-spin mb-3" />
            <p className="text-dark-400 text-sm">
              {isNovelChapter ? 'Đang tải nội dung...' : 'Đang tải ảnh chương...'}
            </p>
          </div>
        )}

        {/* Error */}
        {!loading && error && !isNovelChapter && (
          <div className="flex flex-col items-center justify-center py-32">
            <ImageOff size={40} className="text-dark-600 mb-3" />
            <p className="text-dark-400 text-sm">{error}</p>
          </div>
        )}

        {/* ====== NOVEL TEXT CONTENT ====== */}
        {!loading && isNovelChapter && (
          <div className={`mx-auto px-6 py-8 ${isFullscreen ? 'max-w-4xl' : 'max-w-2xl'} transition-all duration-300`}>
            {/* Chapter title */}
            <h1 className="text-xl font-bold text-white mb-8 text-center leading-relaxed">
              {chapterTitle}
            </h1>

            {/* Paragraphs */}
            <div className="space-y-4">
              {novelParagraphs.map((paragraph, idx) => (
                <p
                  key={idx}
                  className="text-[15px] leading-[1.9] text-dark-200 indent-8"
                  style={{ fontFamily: "'Inter', 'Noto Serif', Georgia, serif" }}
                >
                  {paragraph}
                </p>
              ))}
            </div>

            {/* End indicator */}
            <div className="py-12 text-center border-t border-dark-800 mt-12">
              <p className="text-dark-500 text-sm">— Hết chương —</p>
              <p className="text-dark-600 text-xs mt-1">
                {novelParagraphs.length} đoạn văn • {chapter.content.length.toLocaleString('vi-VN')} ký tự
              </p>
            </div>
          </div>
        )}

        {/* ====== COMIC IMAGE PAGES ====== */}
        {!loading && !error && !isNovelChapter && pages.length > 0 && (
          <div className={`mx-auto ${isFullscreen ? 'max-w-full' : 'max-w-3xl'} transition-all duration-300`}>
            {pages.map((page, idx) => (
              <div key={idx} className="relative w-full">
                {/* Page number indicator */}
                <div className="absolute top-2 left-2 z-10 px-2 py-0.5 rounded bg-black/60 backdrop-blur-sm">
                  <span className="text-[10px] text-white/70 font-mono">{page.pageNumber}</span>
                </div>

                {/* Loading skeleton */}
                {!loadedImages.has(idx) && (
                  <div className="w-full h-[400px] bg-dark-900 flex items-center justify-center animate-pulse">
                    <Loader2 size={24} className="text-dark-600 animate-spin" />
                  </div>
                )}

                {/* Image */}
                <img
                  src={page.imageUrl}
                  alt={`Trang ${page.pageNumber}`}
                  onLoad={() => handleImageLoad(idx)}
                  loading={idx < 3 ? 'eager' : 'lazy'}
                  className={`w-full block transition-opacity duration-300 ${
                    loadedImages.has(idx) ? 'opacity-100' : 'opacity-0 h-0'
                  }`}
                  style={{ userSelect: 'none' }}
                />
              </div>
            ))}

            {/* End indicator */}
            <div className="py-12 text-center border-t border-dark-800">
              <p className="text-dark-500 text-sm">— Hết chương —</p>
              <p className="text-dark-600 text-xs mt-1">{pages.length} trang</p>
            </div>
          </div>
        )}
      </div>

      {/* Scroll to top button */}
      {showScrollTop && (
        <button
          onClick={scrollToTop}
          className="fixed bottom-6 right-6 z-[100] p-3 rounded-full bg-primary-600/90 hover:bg-primary-500 text-white shadow-lg shadow-primary-600/30 transition-all duration-200 hover:scale-105 cursor-pointer backdrop-blur-sm"
        >
          <ChevronUp size={20} />
        </button>
      )}
    </div>
  );
}
