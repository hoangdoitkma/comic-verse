import { useState, useRef, useCallback, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Upload,
  FolderOpen,
  Trash2,
  AlertCircle,
  CheckCircle2,
  X,
  HardDrive,
  Layers,
  FileText,
  Lock,
  Unlock,
  BookOpen,
  Search,
  Filter,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';
import comicService from '../../services/comicService';
import { ToastContainer, useToast } from '../../components/Toast';

// ─── Constants ────────────────────────────────────────────────────────────
const JUNK_FILES = ['.ds_store', 'thumbs.db', 'desktop.ini', '.gitkeep', '.gitignore'];

// ─── Utility Functions ────────────────────────────────────────────────────

/** Remove Vietnamese diacritics and convert to slug */
function removeVietnameseDiacritics(str) {
  const from = 'àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđÀÁẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÈÉẺẼẸÊẾỀỂỄỆÌÍỈĨỊÒÓỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÙÚỦŨỤƯỨỪỬỮỰỲÝỶỸỴĐ';
  const to   = 'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyydAAAAAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYYD';
  let result = str.trim();
  for (let i = 0; i < from.length; i++) {
    result = result.replaceAll(from[i], to[i]);
  }
  return result
    .toLowerCase()
    .replace(/[^a-z0-9\s\-_.]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

/** Parse chapter number from filename like "Chương 1 Thiên tài rơi rụng.json" */
function parseChapterNumber(filename) {
  // Match "Chương X" or "Chuong X" pattern
  const match = filename.match(/Ch[uư][oơ]ng\s+(\d+)/i);
  return match ? parseInt(match[1], 10) : null;
}

/** Check if file is a valid chapter (not Cover, TOC, etc.) */
function isValidChapterFile(filename) {
  const lower = filename.toLowerCase();
  if (JUNK_FILES.includes(lower)) return false;
  if (lower.startsWith('.')) return false;
  if (!lower.endsWith('.json')) return false;
  if (lower.includes('cover')) return false;
  if (lower.includes('giới thiệu') || lower.includes('gioi thieu')) return false;
  if (lower.match(/#\d+\s*-\s*#\d+/)) return false; // TOC file like "#1 - #1641"
  return parseChapterNumber(filename) !== null;
}

/** Format file size */
function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

/** Read file as text */
function readFileAsText(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => resolve(e.target.result);
    reader.onerror = (e) => reject(e);
    reader.readAsText(file, 'utf-8');
  });
}

// ─── Main Component ──────────────────────────────────────────────────────
export default function BulkUploadNovelPage() {
  const { comicId } = useParams();
  const navigate = useNavigate();
  const { toasts, addToast, dismissToast } = useToast();
  const fileInputRef = useRef(null);

  // State
  const [comic, setComic] = useState(null);
  const [loadingComic, setLoadingComic] = useState(true);
  const [chapters, setChapters] = useState([]);
  const [isDragOver, setIsDragOver] = useState(false);
  const [parsing, setParsing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState({ completed: 0, total: 0, failed: 0 });
  const [errors, setErrors] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [bulkAccessType, setBulkAccessType] = useState('FREE');
  const [showBulkAccess, setShowBulkAccess] = useState(false);
  const [skippedFiles, setSkippedFiles] = useState([]);

  // Load comic info
  useEffect(() => {
    const fetchComic = async () => {
      try {
        setLoadingComic(true);
        const comics = await comicService.getMyComics();
        const found = (comics || []).find((c) => String(c.id) === String(comicId));
        setComic(found || null);
      } catch (err) {
        console.error('Error loading comic:', err);
      } finally {
        setLoadingComic(false);
      }
    };
    fetchComic();
  }, [comicId]);

  // ─── Process selected/dropped files ────────────────────────────────────
  const processFiles = useCallback(async (fileList) => {
    setParsing(true);
    setErrors([]);
    const newChapters = [];
    const skipped = [];
    const parseErrors = [];

    for (const file of fileList) {
      const filename = file.name;

      // Filter out invalid files
      if (!isValidChapterFile(filename)) {
        skipped.push(filename);
        continue;
      }

      try {
        const text = await readFileAsText(file);
        const json = JSON.parse(text);
        const chapterNumber = parseChapterNumber(filename);
        const slug = removeVietnameseDiacritics(filename.replace('.json', ''));

        // Join content array into a single string with paragraph breaks
        const contentArray = json.content || [];
        const content = contentArray
          .filter((line) => line && line.trim() !== '' && line.trim() !== '.')
          .join('\n\n');

        if (!content.trim()) {
          skipped.push(`${filename} (nội dung rỗng)`);
          continue;
        }

        newChapters.push({
          chapterNumber,
          title: json.chapter_title || `Chương ${chapterNumber}`,
          slug,
          originalFilename: filename,
          content,
          accessType: 'FREE',
          size: file.size,
          wordCount: content.length,
        });
      } catch (err) {
        parseErrors.push(`Lỗi đọc "${filename}": ${err.message}`);
      }
    }

    // Sort by chapter number
    newChapters.sort((a, b) => a.chapterNumber - b.chapterNumber);

    if (parseErrors.length > 0) {
      setErrors(parseErrors);
    }
    setSkippedFiles(skipped);
    setChapters((prev) => {
      // Merge, avoiding duplicates by chapterNumber
      const existingNumbers = new Set(prev.map((c) => c.chapterNumber));
      const unique = newChapters.filter((c) => !existingNumbers.has(c.chapterNumber));
      return [...prev, ...unique].sort((a, b) => a.chapterNumber - b.chapterNumber);
    });
    setParsing(false);

    if (newChapters.length > 0) {
      addToast(`Đã quét được ${newChapters.length} chương hợp lệ`, 'success');
    }
    if (skipped.length > 0) {
      addToast(`Bỏ qua ${skipped.length} file không hợp lệ`, 'info');
    }
  }, [addToast]);

  // ─── Drag & Drop Handlers ─────────────────────────────────────────────
  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const handleDrop = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    const items = e.dataTransfer.items;
    if (!items) return;

    const allFiles = [];
    for (let i = 0; i < items.length; i++) {
      const entry = items[i].webkitGetAsEntry?.() || items[i].getAsEntry?.();
      if (entry) {
        const files = await readAllEntries(entry);
        allFiles.push(...files);
      }
    }
    if (allFiles.length > 0) processFiles(allFiles);
  };

  /** Recursively read directory entries */
  async function readAllEntries(entry) {
    const files = [];
    if (entry.isFile) {
      const file = await new Promise((resolve) => entry.file(resolve));
      files.push(file);
    } else if (entry.isDirectory) {
      const reader = entry.createReader();
      let batch;
      do {
        batch = await new Promise((resolve, reject) =>
          reader.readEntries(resolve, reject)
        );
        for (const child of batch) {
          const childFiles = await readAllEntries(child);
          files.push(...childFiles);
        }
      } while (batch.length > 0);
    }
    return files;
  }

  const handleFileInputChange = (e) => {
    const files = Array.from(e.target.files || []);
    if (files.length > 0) processFiles(files);
    e.target.value = '';
  };

  // ─── Chapter Management ───────────────────────────────────────────────
  const removeChapter = (idx) => {
    setChapters((prev) => prev.filter((_, i) => i !== idx));
  };

  const updateChapterField = (idx, field, value) => {
    setChapters((prev) => {
      const updated = [...prev];
      updated[idx] = { ...updated[idx], [field]: value };
      return updated;
    });
  };

  const clearAll = () => {
    setChapters([]);
    setErrors([]);
    setSkippedFiles([]);
  };

  const applyBulkAccessType = () => {
    setChapters((prev) => prev.map((ch) => ({ ...ch, accessType: bulkAccessType })));
    setShowBulkAccess(false);
    addToast(`Đã áp dụng quyền "${bulkAccessType}" cho tất cả chương`, 'success');
  };

  // ─── Upload Sequential ────────────────────────────────────────────────
  const handleUpload = async () => {
    if (chapters.length === 0) return;

    setUploading(true);
    setErrors([]);
    setUploadProgress({ completed: 0, total: chapters.length, failed: 0 });

    let failedCount = 0;
    const failedChapters = [];

    for (let i = 0; i < chapters.length; i++) {
      const ch = chapters[i];
      try {
        await comicService.createNovelChapter(comicId, {
          chapterNumber: ch.chapterNumber,
          title: ch.title,
          accessType: ch.accessType,
          content: ch.content,
        });
      } catch (err) {
        failedCount++;
        const msg = err.response?.data?.message || err.message || 'Lỗi không xác định';
        failedChapters.push(`Chương ${ch.chapterNumber}: ${msg}`);
        console.error(`[NovelUpload] Failed chapter ${ch.chapterNumber}:`, err);
      }
      setUploadProgress({ completed: i + 1, total: chapters.length, failed: failedCount });
    }

    if (failedCount > 0) {
      setErrors(failedChapters.slice(0, 10)); // Show max 10 errors
      addToast(`Upload hoàn tất với ${failedCount} lỗi`, 'error');
    } else {
      addToast(`Upload thành công ${chapters.length} chương!`, 'success');
      setTimeout(() => navigate(`/uploader/comics/${comicId}`), 1500);
    }

    setUploading(false);
  };

  // ─── Computed Values ──────────────────────────────────────────────────
  const totalSize = chapters.reduce((sum, ch) => sum + ch.size, 0);
  const totalWords = chapters.reduce((sum, ch) => sum + ch.wordCount, 0);
  const progressPercent = uploadProgress.total > 0
    ? Math.round((uploadProgress.completed / uploadProgress.total) * 100)
    : 0;
  const filteredChapters = chapters.filter((ch) =>
    ch.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ch.slug.includes(searchTerm.toLowerCase()) ||
    String(ch.chapterNumber).includes(searchTerm)
  );

  // ─── Render ───────────────────────────────────────────────────────────
  return (
    <div className="space-y-6 pb-8">
      <ToastContainer toasts={toasts} dismissToast={dismissToast} />

      {/* Header */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate(`/uploader/comics/${comicId}`)}
          className="p-2 rounded-lg hover:bg-dark-800 text-dark-400 hover:text-white transition-all cursor-pointer"
        >
          <ArrowLeft size={20} />
        </button>
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <BookOpen size={24} className="text-primary-400" />
            Upload Truyện Chữ Hàng Loạt
          </h1>
          <p className="text-sm text-dark-400 mt-0.5">
            {loadingComic ? (
              <span className="inline-block h-4 w-48 rounded bg-dark-700 animate-pulse" />
            ) : (
              comic?.title || 'Đang tải...'
            )}
          </p>
        </div>
      </div>

      {/* How it works */}
      <div className="px-5 py-4 rounded-xl bg-dark-900/50 border border-dark-700/30">
        <p className="text-xs text-dark-400 leading-relaxed">
          <span className="text-primary-400 font-semibold">Cách hoạt động:</span>{' '}
          Chọn thư mục chứa các file <span className="text-white font-medium">.json</span>{' '}
          (mỗi file = 1 chương). Hệ thống sẽ tự động{' '}
          <span className="text-white font-medium">parse nội dung</span>,{' '}
          <span className="text-white font-medium">chuyển tên tiếng Việt → không dấu</span>,{' '}
          sắp xếp theo số chương, và upload tuần tự lên server.
          File đặc biệt (Cover, Giới thiệu, Mục lục) sẽ được tự động bỏ qua.
        </p>
      </div>

      {/* Dropzone */}
      {!uploading && (
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`
            relative border-2 border-dashed rounded-2xl py-12 flex flex-col items-center gap-4
            transition-all duration-300 cursor-pointer group
            ${isDragOver
              ? 'border-primary-500 bg-primary-500/5 scale-[1.01] animate-drag-pulse'
              : 'border-dark-700/50 hover:border-primary-500/40 hover:bg-dark-900/40'
            }
          `}
        >
          <div
            className={`w-16 h-16 rounded-2xl flex items-center justify-center transition-all duration-300
              ${isDragOver ? 'bg-primary-500/15 scale-110' : 'bg-dark-800 group-hover:bg-dark-700'}
            `}
          >
            <FolderOpen
              size={28}
              className={`transition-colors ${isDragOver ? 'text-primary-400' : 'text-dark-500 group-hover:text-dark-400'}`}
            />
          </div>
          <div className="text-center">
            <p className={`text-base font-medium ${isDragOver ? 'text-primary-400' : 'text-dark-300'}`}>
              {isDragOver ? 'Thả thư mục vào đây...' : 'Kéo thả thư mục truyện chữ vào đây'}
            </p>
            <p className="text-sm text-dark-500 mt-1">
              Mỗi file .json = 1 chương • Tự động parse nội dung & sắp xếp
            </p>
            <p className="text-xs text-dark-600 mt-2">Hoặc click để chọn thư mục</p>
          </div>

          {parsing && (
            <div className="absolute inset-0 bg-dark-950/80 rounded-2xl flex items-center justify-center backdrop-blur-sm">
              <div className="flex items-center gap-3">
                <div className="w-5 h-5 border-2 border-primary-400/30 border-t-primary-400 rounded-full animate-spin" />
                <span className="text-sm text-dark-300">Đang quét và parse file JSON...</span>
              </div>
            </div>
          )}
        </div>
      )}

      <input
        ref={fileInputRef}
        type="file"
        multiple
        webkitdirectory=""
        directory=""
        onChange={handleFileInputChange}
        className="hidden"
      />

      {/* Skipped Files Info */}
      {skippedFiles.length > 0 && (
        <details className="px-4 py-3 rounded-xl bg-dark-900/50 border border-dark-700/30">
          <summary className="text-xs text-dark-500 cursor-pointer hover:text-dark-400 transition-colors">
            {skippedFiles.length} file đã bị bỏ qua (Cover, TOC, Giới thiệu, file không hợp lệ...)
          </summary>
          <div className="mt-2 max-h-32 overflow-y-auto custom-scrollbar">
            {skippedFiles.map((f, i) => (
              <p key={i} className="text-xs text-dark-600 py-0.5">{f}</p>
            ))}
          </div>
        </details>
      )}

      {/* Errors */}
      {errors.length > 0 && (
        <div className="space-y-2 animate-fade-in-up">
          {errors.map((err, i) => (
            <div
              key={i}
              className="flex items-start gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/20"
            >
              <AlertCircle size={16} className="text-red-400 shrink-0 mt-0.5" />
              <p className="text-sm text-red-400 flex-1">{err}</p>
              <button
                onClick={() => setErrors((prev) => prev.filter((_, idx) => idx !== i))}
                className="ml-auto text-red-400/60 hover:text-red-400 transition-colors cursor-pointer"
              >
                <X size={14} />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Summary Bar */}
      {chapters.length > 0 && !uploading && (
        <div className="flex items-center justify-between px-5 py-3 rounded-xl bg-dark-900/80 border border-dark-700/40 animate-fade-in-up">
          <div className="flex items-center gap-6 text-sm">
            <span className="flex items-center gap-2 text-dark-300">
              <Layers size={15} className="text-primary-400" />
              <span className="font-semibold text-white">{chapters.length}</span> chương
            </span>
            <span className="flex items-center gap-2 text-dark-300">
              <FileText size={15} className="text-primary-400" />
              <span className="font-semibold text-white">{(totalWords).toLocaleString('vi-VN')}</span> ký tự
            </span>
            <span className="flex items-center gap-2 text-dark-300">
              <HardDrive size={15} className="text-primary-400" />
              <span className="font-semibold text-white">{formatFileSize(totalSize)}</span>
            </span>
          </div>
          <div className="flex items-center gap-2">
            {/* Bulk Access Type */}
            <div className="relative">
              <button
                onClick={() => setShowBulkAccess(!showBulkAccess)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-dark-300 hover:bg-dark-700/50 transition-all cursor-pointer border border-dark-700/50"
              >
                <Filter size={13} />
                Quyền truy cập
                <ChevronDown size={12} className={`transition-transform ${showBulkAccess ? 'rotate-180' : ''}`} />
              </button>
              {showBulkAccess && (
                <div className="absolute right-0 top-full mt-1 w-48 bg-dark-800 border border-dark-700/50 rounded-xl shadow-2xl shadow-black/40 p-3 z-20 animate-scale-in">
                  <p className="text-xs text-dark-500 mb-2">Áp dụng cho tất cả:</p>
                  <select
                    value={bulkAccessType}
                    onChange={(e) => setBulkAccessType(e.target.value)}
                    className="w-full px-3 py-1.5 rounded-lg bg-dark-700 border border-dark-600/50 text-xs text-dark-200 mb-2 focus:outline-none"
                  >
                    <option value="FREE">Free</option>
                    <option value="VIP">VIP</option>
                  </select>
                  <button
                    onClick={applyBulkAccessType}
                    className="w-full px-3 py-1.5 rounded-lg bg-primary-600 hover:bg-primary-500 text-white text-xs font-medium transition-all cursor-pointer"
                  >
                    Áp dụng
                  </button>
                </div>
              )}
            </div>

            <button
              onClick={clearAll}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-red-400 hover:bg-red-500/10 transition-all cursor-pointer"
            >
              <Trash2 size={13} />
              Xóa tất cả
            </button>
          </div>
        </div>
      )}

      {/* Search */}
      {chapters.length > 20 && !uploading && (
        <div className="relative max-w-sm">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-500" />
          <input
            type="text"
            placeholder="Tìm chương..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-lg bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
          />
        </div>
      )}

      {/* Chapter List */}
      {chapters.length > 0 && !uploading && (
        <div className="bg-dark-900/60 border border-dark-700/40 rounded-2xl overflow-hidden">
          {/* Table Header */}
          <div className="grid grid-cols-[60px_1fr_2fr_100px_80px_50px] gap-3 px-5 py-3 border-b border-dark-700/50 text-xs font-semibold text-dark-400 uppercase tracking-wider">
            <span>STT</span>
            <span>Slug (không dấu)</span>
            <span>Tiêu đề gốc</span>
            <span>Quyền</span>
            <span>Kích thước</span>
            <span></span>
          </div>

          {/* Rows */}
          <div className="max-h-[500px] overflow-y-auto scrollbar-auto-hide">
            {filteredChapters.map((chapter, _) => {
              const idx = chapters.findIndex((c) => c.chapterNumber === chapter.chapterNumber);
              return (
                <div
                  key={chapter.chapterNumber}
                  className="grid grid-cols-[60px_1fr_2fr_100px_80px_50px] gap-3 px-5 py-2.5 border-b border-dark-700/20 hover:bg-dark-800/30 transition-colors items-center animate-fade-in-up"
                >
                  {/* Chapter Number */}
                  <span className="text-sm font-mono font-medium text-primary-400">
                    #{chapter.chapterNumber}
                  </span>

                  {/* Slug */}
                  <span className="text-xs font-mono text-dark-500 truncate" title={chapter.slug}>
                    {chapter.slug}
                  </span>

                  {/* Title (editable) */}
                  <input
                    type="text"
                    value={chapter.title}
                    onChange={(e) => updateChapterField(idx, 'title', e.target.value)}
                    className="w-full px-2 py-1 rounded bg-transparent border border-transparent hover:border-dark-700/50 focus:border-primary-500/50 text-sm text-dark-200 focus:outline-none transition-all"
                  />

                  {/* Access Type */}
                  <select
                    value={chapter.accessType}
                    onChange={(e) => updateChapterField(idx, 'accessType', e.target.value)}
                    className="px-2 py-1 rounded-lg bg-dark-800 border border-dark-700/50 text-xs text-dark-200 focus:outline-none focus:border-primary-500/50 transition-all cursor-pointer"
                  >
                    <option value="FREE">Free</option>
                    <option value="VIP">VIP</option>
                  </select>

                  {/* Size */}
                  <span className="text-xs text-dark-500">
                    {formatFileSize(chapter.size)}
                  </span>

                  {/* Delete */}
                  <button
                    onClick={() => removeChapter(idx)}
                    className="p-1 rounded-lg text-dark-500 hover:text-red-400 hover:bg-red-500/10 transition-all cursor-pointer"
                    title="Xóa chương"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              );
            })}
          </div>

          {/* Footer info */}
          {searchTerm && filteredChapters.length === 0 && (
            <div className="py-8 text-center">
              <p className="text-sm text-dark-400">
                Không tìm thấy chương nào phù hợp với "<span className="text-dark-200">{searchTerm}</span>"
              </p>
            </div>
          )}
          {searchTerm && filteredChapters.length > 0 && filteredChapters.length < chapters.length && (
            <div className="px-5 py-2 border-t border-dark-700/30">
              <p className="text-xs text-dark-500">
                Hiển thị {filteredChapters.length}/{chapters.length} chương
              </p>
            </div>
          )}
        </div>
      )}

      {/* Upload Progress Panel */}
      {uploading && (
        <div className="px-5 py-5 rounded-2xl bg-dark-900/80 border border-dark-700/40 space-y-4 animate-fade-in-up">
          <div className="flex items-center gap-3">
            {uploadProgress.completed < uploadProgress.total ? (
              <>
                <div className="w-5 h-5 border-2 border-primary-400/30 border-t-primary-400 rounded-full animate-spin" />
                <span className="text-sm text-dark-300">
                  Đang upload chương {uploadProgress.completed + 1}/{uploadProgress.total}...
                </span>
                <span className="ml-auto text-sm font-mono font-bold text-primary-400">{progressPercent}%</span>
              </>
            ) : uploadProgress.failed === 0 ? (
              <>
                <CheckCircle2 size={20} className="text-emerald-400" />
                <span className="text-sm text-emerald-400 font-medium">Upload hoàn tất!</span>
              </>
            ) : (
              <>
                <AlertCircle size={20} className="text-amber-400" />
                <span className="text-sm text-amber-400 font-medium">
                  Hoàn tất với {uploadProgress.failed} lỗi
                </span>
              </>
            )}
          </div>

          {/* Progress Bar */}
          <div className="w-full h-2.5 rounded-full bg-dark-800 overflow-hidden">
            <div
              className={`h-full rounded-full transition-all duration-300 ease-out ${
                uploadProgress.failed > 0
                  ? 'bg-gradient-to-r from-amber-600 to-amber-400'
                  : 'bg-gradient-to-r from-primary-600 to-primary-400'
              }`}
              style={{ width: `${progressPercent}%` }}
            />
          </div>

          {/* Current chapter indicator */}
          {uploadProgress.completed < uploadProgress.total && (
            <div className="flex items-center gap-2 text-xs text-dark-500">
              <FileText size={12} />
              <span>
                {chapters[uploadProgress.completed]?.title || `Chương ${uploadProgress.completed + 1}`}
              </span>
            </div>
          )}
        </div>
      )}

      {/* Action Bar */}
      {chapters.length > 0 && !uploading && (
        <div className="sticky bottom-0 z-10">
          <div className="bg-dark-950/95 backdrop-blur-xl border border-dark-700/40 rounded-2xl p-5 shadow-2xl shadow-black/40">
            <div className="flex items-center justify-between">
              <div className="text-sm text-dark-400">
                Sẵn sàng upload{' '}
                <span className="font-semibold text-white">{chapters.length}</span> chương •{' '}
                <span className="font-semibold text-white">{(totalWords).toLocaleString('vi-VN')}</span> ký tự •{' '}
                Tổng: <span className="font-semibold text-primary-400">{formatFileSize(totalSize)}</span>
              </div>
              <div className="flex items-center gap-3">
                <button
                  onClick={() => navigate(`/uploader/comics/${comicId}`)}
                  className="px-4 py-2.5 rounded-xl text-sm font-medium text-dark-400 hover:text-dark-200 hover:bg-dark-800 transition-all cursor-pointer"
                >
                  Huỷ
                </button>
                <button
                  onClick={handleUpload}
                  className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 hover:scale-[1.02] active:scale-[0.98] cursor-pointer"
                >
                  <Upload size={16} />
                  Upload {chapters.length} chương
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
