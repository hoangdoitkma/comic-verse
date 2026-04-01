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
  Image,
  Lock,
  Unlock,
  FileText,
} from 'lucide-react';
import comicService from '../../services/comicService';
import { ToastContainer, useToast } from '../../components/Toast';

// ─── Constants ────────────────────────────────────────────────────────────
const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp'];
const JUNK_FILES = ['.ds_store', 'thumbs.db', 'desktop.ini', '.gitkeep', '.gitignore'];
const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

// ─── Utility Functions ────────────────────────────────────────────────────
function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

/** Sanitize folder name → slug (supports Vietnamese characters) */
function toSlug(str) {
  const from = 'àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđ';
  const to   = 'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd';
  let result = str.toLowerCase().trim();
  for (let i = 0; i < from.length; i++) {
    result = result.replace(new RegExp(from[i], 'g'), to[i]);
  }
  return result
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

/** Check if file is valid image */
function isValidImage(file) {
  const name = file.name.toLowerCase();
  if (JUNK_FILES.includes(name)) return false;
  if (name.startsWith('.')) return false;
  const ext = name.split('.').pop();
  return ALLOWED_EXTENSIONS.includes(ext);
}

/** Natural sort comparator for filenames */
function naturalSort(a, b) {
  return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
}

/** Recursively read directory entries using webkitGetAsEntry */
async function readEntries(entry) {
  const files = [];
  if (entry.isFile) {
    const file = await new Promise((resolve) => entry.file(resolve));
    files.push({ file, path: entry.fullPath });
  } else if (entry.isDirectory) {
    const reader = entry.createReader();
    let batch;
    do {
      batch = await new Promise((resolve, reject) =>
        reader.readEntries(resolve, reject)
      );
      for (const child of batch) {
        const childFiles = await readEntries(child);
        files.push(...childFiles);
      }
    } while (batch.length > 0);
  }
  return files;
}

// ─── Main Component ──────────────────────────────────────────────────────
export default function BulkUploadChaptersPage() {
  const { comicId } = useParams();
  const navigate = useNavigate();
  const { toasts, addToast, dismissToast } = useToast();
  const fileInputRef = useRef(null);

  // State
  const [comic, setComic] = useState(null);
  const [loadingComic, setLoadingComic] = useState(true);
  // chapters: [{folderName, slug, title, accessType, files: [{file, name, size}]}]
  const [chapters, setChapters] = useState([]);
  const [isDragOver, setIsDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadStep, setUploadStep] = useState(''); // 'init' | 'uploading' | 'done'
  const [uploadProgress, setUploadProgress] = useState({ completed: 0, total: 0, failed: 0 });
  const [errors, setErrors] = useState([]);

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

  // ─── Process dropped/selected files → SORT & BUILD STATE ──────────────
  const processFiles = useCallback((fileEntries) => {
    const folderMap = new Map();
    const newErrors = [];
    let oversizedCount = 0;
    let invalidCount = 0;

    for (const entry of fileEntries) {
      const { file, path } = entry;
      const parts = path.split('/').filter(Boolean);
      if (parts.length < 2) continue;

      const folderName = parts[parts.length - 2];

      if (!isValidImage(file)) {
        invalidCount++;
        continue;
      }

      if (file.size > MAX_FILE_SIZE) {
        oversizedCount++;
        newErrors.push(`"${file.name}" vượt quá 5MB (${formatFileSize(file.size)})`);
        continue;
      }

      const slug = toSlug(folderName);
      if (!folderMap.has(slug)) {
        folderMap.set(slug, {
          folderName,
          slug,
          title: '',
          accessType: 'FREE',
          files: [],
        });
      }

      folderMap.get(slug).files.push({
        file,
        name: file.name,
        size: file.size,
      });
    }

    // ── CRITICAL: Sort files within each folder by name (alpha-numeric) ──
    for (const [, chapter] of folderMap) {
      chapter.files.sort((a, b) => naturalSort(a.name, b.name));
    }

    // Sort chapters by folder slug
    const newChapters = Array.from(folderMap.values()).sort((a, b) =>
      naturalSort(a.slug, b.slug)
    );

    if (newChapters.length === 0 && fileEntries.length > 0) {
      newErrors.push('Không tìm thấy thư mục chứa ảnh hợp lệ. Hãy thả folder chứa file ảnh.');
    }
    if (invalidCount > 0) {
      newErrors.push(`${invalidCount} file không hợp lệ đã bị bỏ qua (chỉ chấp nhận .jpg, .png, .jpeg, .webp)`);
    }
    if (oversizedCount > 0) {
      newErrors.push(`${oversizedCount} file vượt quá giới hạn 5MB đã bị loại bỏ`);
    }

    setErrors(newErrors);
    setChapters((prev) => [...prev, ...newChapters]);
  }, []);

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
        const files = await readEntries(entry);
        allFiles.push(...files);
      }
    }
    if (allFiles.length > 0) processFiles(allFiles);
  };

  const handleFileInputChange = (e) => {
    const files = Array.from(e.target.files || []);
    const entries = files.map((f) => ({
      file: f,
      path: '/' + (f.webkitRelativePath || f.name),
    }));
    processFiles(entries);
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
  };

  // ─── 2-Step Upload (SEQUENTIAL — one file at a time) ──────────────────
  const handleUpload = async () => {
    if (chapters.length === 0) return;

    setUploading(true);
    setErrors([]);

    try {
      // ── Step 1: Send metadata JSON → create Chapter + ChapterPage records ──
      setUploadStep('init');
      const metadata = {
        chapters: chapters.map((ch) => ({
          folderName: ch.slug,
          title: ch.title || null,
          accessType: ch.accessType,
          pageFileNames: ch.files.map((f) => f.name),
        })),
      };

      console.log('[BulkUpload] Step 1: Sending metadata', metadata);
      const initResponse = await comicService.initBulkChapters(comicId, metadata);
      console.log('[BulkUpload] Step 1 response:', initResponse);

      // Defensive: handle response shape
      const chaptersData = initResponse?.chapters || [];

      if (chaptersData.length === 0) {
        throw new Error('Server trả về danh sách chương rỗng. Vui lòng kiểm tra backend logs.');
      }

      // ── Step 2: Upload files SEQUENTIALLY (one by one) ──
      setUploadStep('uploading');

      // Build flat ordered upload task list
      const uploadTasks = [];
      const chapterFileMap = new Map();
      for (const ch of chapters) {
        chapterFileMap.set(ch.slug, ch.files);
      }

      for (const chapterResult of chaptersData) {
        const chapterFiles = chapterFileMap.get(chapterResult.folderName);
        if (!chapterFiles) {
          console.warn('[BulkUpload] No files found for folder:', chapterResult.folderName);
          continue;
        }

        for (const pageMapping of chapterResult.pages) {
          const fileItem = chapterFiles.find((f) => f.name === pageMapping.fileName);
          if (fileItem) {
            uploadTasks.push({
              pageId: pageMapping.pageId,
              file: fileItem.file,
              fileName: pageMapping.fileName,
            });
          } else {
            console.warn('[BulkUpload] File not found:', pageMapping.fileName);
          }
        }
      }

      console.log('[BulkUpload] Step 2: Upload tasks count:', uploadTasks.length);

      if (uploadTasks.length === 0) {
        throw new Error('Không có file nào để upload. Kiểm tra mapping giữa metadata và files.');
      }

      setUploadProgress({ completed: 0, total: uploadTasks.length, failed: 0 });

      // Sequential upload: one file at a time, top to bottom
      let failedCount = 0;
      for (let i = 0; i < uploadTasks.length; i++) {
        const task = uploadTasks[i];
        try {
          await comicService.uploadChapterPage(task.pageId, task.file);
        } catch (err) {
          failedCount++;
          console.error(`[BulkUpload] Failed to upload ${task.fileName}:`, err.message);
        }
        setUploadProgress({ completed: i + 1, total: uploadTasks.length, failed: failedCount });
      }

      if (failedCount > 0) {
        setErrors([`${failedCount}/${uploadTasks.length} ảnh upload thất bại. Dữ liệu chương đã được tạo.`]);
        setUploadStep('done');
        addToast(`Upload hoàn tất với ${failedCount} lỗi`, 'error');
      } else {
        setUploadStep('done');
        addToast(`Upload thành công ${chapters.length} chương (${uploadTasks.length} ảnh)!`, 'success');
        setTimeout(() => navigate(`/uploader/comics/${comicId}`), 1500);
      }
    } catch (err) {
      console.error('[BulkUpload] Error:', err);
      const msg = err.response?.data?.message || err.message || 'Đã xảy ra lỗi. Vui lòng thử lại.';
      setErrors([msg]);
      addToast(msg, 'error');
    } finally {
      setUploading(false);
    }
  };

  // ─── Computed Values ──────────────────────────────────────────────────
  const totalFiles = chapters.reduce((sum, ch) => sum + ch.files.length, 0);
  const totalSize = chapters.reduce(
    (sum, ch) => sum + ch.files.reduce((s, f) => s + f.size, 0),
    0
  );
  const progressPercent = uploadProgress.total > 0
    ? Math.round((uploadProgress.completed / uploadProgress.total) * 100)
    : 0;

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
          <h1 className="text-2xl font-bold text-white">Upload Hàng Loạt</h1>
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
          Kéo thả các thư mục chương truyện (mỗi folder = 1 chương). File ảnh trong mỗi folder sẽ được{' '}
          <span className="text-white font-medium">tự động sắp xếp theo tên</span> (01.jpg → trang 1, 02.jpg → trang 2...).
          Thứ tự này được ghi nhận cố định trước khi upload.
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
              {isDragOver ? 'Thả thư mục vào đây...' : 'Kéo thả thư mục chương truyện vào đây'}
            </p>
            <p className="text-sm text-dark-500 mt-1">
              Mỗi thư mục = 1 chương • PNG, JPG, WEBP • Tối đa 5MB/ảnh
            </p>
            <p className="text-xs text-dark-600 mt-2">Hoặc click để chọn thư mục</p>
          </div>
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

      {/* Errors */}
      {errors.length > 0 && (
        <div className="space-y-2 animate-fade-in-up">
          {errors.map((err, i) => (
            <div
              key={i}
              className="flex items-start gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/20"
            >
              <AlertCircle size={16} className="text-red-400 shrink-0 mt-0.5" />
              <p className="text-sm text-red-400">{err}</p>
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
              <Image size={15} className="text-primary-400" />
              <span className="font-semibold text-white">{totalFiles}</span> ảnh
            </span>
            <span className="flex items-center gap-2 text-dark-300">
              <HardDrive size={15} className="text-primary-400" />
              <span className="font-semibold text-white">{formatFileSize(totalSize)}</span>
            </span>
          </div>
          <button
            onClick={clearAll}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-red-400 hover:bg-red-500/10 transition-all cursor-pointer"
          >
            <Trash2 size={13} />
            Xóa tất cả
          </button>
        </div>
      )}

      {/* Chapter List (minimal — no thumbnails, no reorder) */}
      {chapters.length > 0 && (
        <div className="space-y-2">
          {chapters.map((chapter, idx) => (
            <div
              key={chapter.slug + idx}
              className="flex items-center gap-4 px-5 py-3.5 bg-dark-900/60 border border-dark-700/40 rounded-xl animate-fade-in-up"
            >
              {/* Folder icon + slug */}
              <div className="flex items-center gap-2.5 min-w-0 flex-1">
                <FolderOpen size={16} className="text-primary-400 shrink-0" />
                <span className="text-sm font-mono text-primary-400 font-medium">{chapter.slug}</span>
                <span className="text-xs text-dark-500 shrink-0">
                  {chapter.files.length} ảnh • {formatFileSize(chapter.files.reduce((s, f) => s + f.size, 0))}
                </span>
              </div>

              {/* Title input */}
              <input
                type="text"
                placeholder="Tiêu đề (tuỳ chọn)"
                value={chapter.title}
                onChange={(e) => updateChapterField(idx, 'title', e.target.value)}
                disabled={uploading}
                className="w-44 px-3 py-1.5 rounded-lg bg-dark-800 border border-dark-700/50 text-xs text-dark-200 placeholder:text-dark-600 focus:outline-none focus:border-primary-500/50 transition-all disabled:opacity-50"
              />

              {/* Access Type */}
              <select
                value={chapter.accessType}
                onChange={(e) => updateChapterField(idx, 'accessType', e.target.value)}
                disabled={uploading}
                className="px-3 py-1.5 rounded-lg bg-dark-800 border border-dark-700/50 text-xs text-dark-200 focus:outline-none focus:border-primary-500/50 transition-all cursor-pointer disabled:opacity-50"
              >
                <option value="FREE">Free</option>
                <option value="VIP">VIP</option>
              </select>

              {chapter.accessType === 'VIP' ? (
                <Lock size={14} className="text-amber-400 shrink-0" />
              ) : (
                <Unlock size={14} className="text-emerald-400 shrink-0" />
              )}

              {/* File list preview (text only) */}
              <button
                type="button"
                title={chapter.files.map((f, i) => `${i + 1}. ${f.name}`).join('\n')}
                className="p-1.5 rounded-lg text-dark-500 hover:text-dark-300 hover:bg-dark-800 transition-all cursor-help"
              >
                <FileText size={14} />
              </button>

              {/* Delete */}
              {!uploading && (
                <button
                  onClick={() => removeChapter(idx)}
                  className="p-1.5 rounded-lg text-dark-500 hover:text-red-400 hover:bg-red-500/10 transition-all cursor-pointer"
                  title="Xóa chương"
                >
                  <Trash2 size={15} />
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Upload Progress Panel */}
      {uploading && (
        <div className="px-5 py-5 rounded-2xl bg-dark-900/80 border border-dark-700/40 space-y-4 animate-fade-in-up">
          <div className="flex items-center gap-3">
            {uploadStep === 'init' && (
              <>
                <div className="w-5 h-5 border-2 border-primary-400/30 border-t-primary-400 rounded-full animate-spin" />
                <span className="text-sm text-dark-300">Đang tạo dữ liệu chương truyện...</span>
              </>
            )}
            {uploadStep === 'uploading' && (
              <>
                <div className="w-5 h-5 border-2 border-primary-400/30 border-t-primary-400 rounded-full animate-spin" />
                <span className="text-sm text-dark-300">
                  Đang upload ảnh... {uploadProgress.completed}/{uploadProgress.total}
                </span>
                <span className="ml-auto text-sm font-mono font-bold text-primary-400">{progressPercent}%</span>
              </>
            )}
            {uploadStep === 'done' && uploadProgress.failed === 0 && (
              <>
                <CheckCircle2 size={20} className="text-emerald-400" />
                <span className="text-sm text-emerald-400 font-medium">Upload hoàn tất!</span>
              </>
            )}
            {uploadStep === 'done' && uploadProgress.failed > 0 && (
              <>
                <AlertCircle size={20} className="text-amber-400" />
                <span className="text-sm text-amber-400 font-medium">
                  Hoàn tất với {uploadProgress.failed} lỗi
                </span>
              </>
            )}
          </div>

          {/* Progress Bar */}
          {(uploadStep === 'uploading' || uploadStep === 'done') && (
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
                <span className="font-semibold text-white">{totalFiles}</span> ảnh •{' '}
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
