import { useState, useRef, useCallback, useEffect } from 'react';
import { X, Upload, Image, Trash2, GripVertical, Info, FolderOpen, Edit3, Check } from 'lucide-react';
import comicService from '../../services/comicService';

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

/** Natural sort comparator */
function naturalSort(a, b) {
  return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
}

export default function CreateChapterModal({ isOpen, onClose, onSuccess, comicId, contentType }) {
  const [formData, setFormData] = useState({
    chapterNumber: '',
    title: '',
    accessType: 'FREE',
    content: '',
  });
  // files: [{file, name, displayName, size, preview}] — sorted, with editable displayName
  const [files, setFiles] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [uploadProgress, setUploadProgress] = useState({ current: 0, total: 0 });
  const [error, setError] = useState('');
  const [isDragOver, setIsDragOver] = useState(false);
  const [maxChapterNumber, setMaxChapterNumber] = useState(null);
  const [editingIdx, setEditingIdx] = useState(null);
  const [editValue, setEditValue] = useState('');
  const folderInputRef = useRef(null);
  const fileInputRef = useRef(null);

  const isNovel = contentType === 'NOVEL';

  // Load max chapter number when modal opens
  useEffect(() => {
    if (isOpen && comicId) {
      comicService.getMaxChapterNumber(comicId)
        .then((max) => {
          const num = parseFloat(max) || 0;
          setMaxChapterNumber(num);
          setFormData((prev) => ({
            ...prev,
            chapterNumber: prev.chapterNumber || String(num + 1),
          }));
        })
        .catch(() => setMaxChapterNumber(null));
    }
  }, [isOpen, comicId]);

  // Cleanup previews on unmount
  useEffect(() => {
    return () => {
      files.forEach((f) => { if (f.preview) URL.revokeObjectURL(f.preview); });
    };
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // Process files from any source: sort by name, create previews
  const processAndSetFiles = useCallback((rawFiles) => {
    const imageFiles = Array.from(rawFiles)
      .filter((f) => f.type.startsWith('image/') || /\.(jpg|jpeg|png|webp)$/i.test(f.name))
      .sort((a, b) => naturalSort(a.name, b.name));

    if (imageFiles.length === 0) return;

    // Cleanup old previews
    setFiles((prev) => {
      prev.forEach((f) => { if (f.preview) URL.revokeObjectURL(f.preview); });
      return [];
    });

    const processed = imageFiles.map((file, idx) => ({
      file,
      name: file.name,
      displayName: file.name, // editable
      size: file.size,
      preview: URL.createObjectURL(file),
    }));

    setFiles(processed);
  }, []);

  // Folder picker via webkitdirectory
  const handleFolderSelect = (e) => {
    if (e.target.files) processAndSetFiles(e.target.files);
    e.target.value = '';
  };

  // Fallback: regular file picker
  const handleFileSelect = (e) => {
    if (e.target.files) processAndSetFiles(e.target.files);
    e.target.value = '';
  };

  // Drag & Drop
  const handleDragOver = (e) => { e.preventDefault(); setIsDragOver(true); };
  const handleDragLeave = (e) => { e.preventDefault(); setIsDragOver(false); };
  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files) processAndSetFiles(e.dataTransfer.files);
  };

  // File management
  const removeFile = (idx) => {
    setFiles((prev) => {
      if (prev[idx]?.preview) URL.revokeObjectURL(prev[idx].preview);
      return prev.filter((_, i) => i !== idx);
    });
  };

  const clearAllFiles = () => {
    files.forEach((f) => { if (f.preview) URL.revokeObjectURL(f.preview); });
    setFiles([]);
  };

  // Rename file
  const startRename = (idx) => {
    setEditingIdx(idx);
    setEditValue(files[idx].displayName);
  };

  const confirmRename = () => {
    if (editingIdx !== null && editValue.trim()) {
      setFiles((prev) => {
        const updated = [...prev];
        updated[editingIdx] = { ...updated[editingIdx], displayName: editValue.trim() };
        return updated;
      });
    }
    setEditingIdx(null);
    setEditValue('');
  };

  const cancelRename = () => {
    setEditingIdx(null);
    setEditValue('');
  };

  const resetForm = () => {
    setFormData({ chapterNumber: '', title: '', accessType: 'FREE', content: '' });
    clearAllFiles();
    setError('');
    setUploadProgress({ current: 0, total: 0 });
  };

  // ─── Submit: 2-step sequential upload (init → upload one by one) ────
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.chapterNumber) {
      setError('Vui lòng nhập số chương');
      return;
    }

    if (isNovel && !formData.content.trim()) {
      setError('Vui lòng nhập nội dung chương');
      return;
    }

    if (!isNovel && files.length === 0) {
      setError('Vui lòng chọn ít nhất một trang ảnh');
      return;
    }

    setSubmitting(true);
    try {
      if (isNovel) {
        await comicService.createNovelChapter(comicId, {
          chapterNumber: parseFloat(formData.chapterNumber),
          title: formData.title.trim(),
          accessType: formData.accessType,
          content: formData.content.trim(),
        });
      } else {
        // ── Step 1: Init chapter — create Chapter + ChapterPage records (no images yet)
        const initData = {
          chapterNumber: parseFloat(formData.chapterNumber),
          title: formData.title.trim() || null,
          accessType: formData.accessType,
          pageFileNames: files.map((f) => f.displayName),
        };

        const initResult = await comicService.initSingleChapter(comicId, initData);

        if (!initResult?.pages || initResult.pages.length === 0) {
          throw new Error('Không thể tạo chương. Vui lòng thử lại.');
        }

        // ── Step 2: Upload từng ảnh tuần tự theo thứ tự page_number
        setUploadProgress({ current: 0, total: initResult.pages.length });

        // Build file lookup by name
        const fileMap = new Map();
        for (const f of files) {
          fileMap.set(f.displayName, f.file);
        }

        for (let i = 0; i < initResult.pages.length; i++) {
          const page = initResult.pages[i];
          const file = fileMap.get(page.fileName);
          if (file) {
            await comicService.uploadChapterPage(page.pageId, file);
          }
          setUploadProgress({ current: i + 1, total: initResult.pages.length });
        }
      }

      resetForm();
      onSuccess?.();
      onClose();
    } catch (err) {
      const status = err.response?.status;
      if (status === 413) {
        setError('File quá lớn! Vui lòng giảm kích thước hoặc số lượng ảnh.');
      } else {
        const msg = err.response?.data?.message || err.message || 'Đã xảy ra lỗi khi tạo chương. Vui lòng thử lại.';
        setError(msg);
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      <div className="relative bg-dark-900 border border-dark-700/50 rounded-2xl shadow-2xl w-full max-w-xl mx-4 max-h-[90vh] flex flex-col animate-scale-in">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-dark-700/50 shrink-0">
          <h2 className="text-lg font-semibold text-white">Thêm Chương Mới</h2>
          <button
            onClick={onClose}
            className="text-dark-400 hover:text-white transition-colors cursor-pointer p-1"
          >
            <X size={18} />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5 overflow-y-auto flex-1">
          {error && (
            <div className="px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/20 text-sm text-red-400">
              {error}
            </div>
          )}

          {/* Row: chapterNumber + title */}
          <div className="grid grid-cols-3 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300">
                Số chương <span className="text-red-400">*</span>
              </label>
              <input
                type="number"
                name="chapterNumber"
                value={formData.chapterNumber}
                onChange={handleChange}
                placeholder={maxChapterNumber !== null ? String(maxChapterNumber + 1) : '1'}
                step="0.1"
                min="0"
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
              />
              {maxChapterNumber !== null && (
                <p className="flex items-center gap-1 text-[10px] text-dark-500 mt-1">
                  <Info size={10} />
                  Chương mới nhất: <span className="font-semibold text-primary-400">{maxChapterNumber}</span>
                </p>
              )}
            </div>
            <div className="col-span-2 space-y-2">
              <label className="text-sm font-medium text-dark-300">Tiêu đề chương</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="Tên chương (tuỳ chọn)"
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
              />
            </div>
          </div>

          {/* Access Type */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300">Quyền truy cập</label>
            <select
              name="accessType"
              value={formData.accessType}
              onChange={handleChange}
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer"
            >
              <option value="FREE">Miễn phí (Free)</option>
              <option value="VIP">VIP</option>
            </select>
          </div>

          {/* Content */}
          {isNovel ? (
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300">
                Nội dung chương <span className="text-red-400">*</span>
              </label>
              <textarea
                name="content"
                value={formData.content}
                onChange={handleChange}
                placeholder="Viết nội dung chương tiểu thuyết..."
                rows={10}
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all resize-none"
              />
            </div>
          ) : (
            <div className="space-y-3">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <Image size={14} />
                Trang truyện <span className="text-red-400">*</span>
              </label>

              {/* Dropzone (folder-based) */}
              <div
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                className={`
                  border-2 border-dashed rounded-xl py-6 flex flex-col items-center gap-3
                  transition-all duration-200
                  ${isDragOver
                    ? 'border-primary-500 bg-primary-500/5 scale-[1.01]'
                    : 'border-dark-700/50 hover:border-primary-500/40'
                  }
                `}
              >
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center transition-all ${isDragOver ? 'bg-primary-500/15' : 'bg-dark-800'}`}>
                  <FolderOpen size={22} className={isDragOver ? 'text-primary-400' : 'text-dark-500'} />
                </div>
                <div className="text-center">
                  <p className={`text-sm ${isDragOver ? 'text-primary-400' : 'text-dark-400'}`}>
                    {isDragOver ? 'Thả folder vào đây...' : 'Kéo thả hoặc chọn thư mục ảnh'}
                  </p>
                  <p className="text-xs text-dark-600 mt-1">PNG, JPG, WEBP • Auto-sort theo tên file</p>
                </div>
                <div className="flex items-center gap-2 mt-1">
                  <button
                    type="button"
                    onClick={() => folderInputRef.current?.click()}
                    className="px-3 py-1.5 rounded-lg bg-primary-600/80 hover:bg-primary-500 text-white text-xs font-medium transition-all cursor-pointer"
                  >
                    <span className="flex items-center gap-1.5"><FolderOpen size={12} /> Chọn Folder</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="px-3 py-1.5 rounded-lg bg-dark-700 hover:bg-dark-600 text-dark-300 text-xs font-medium transition-all cursor-pointer"
                  >
                    <span className="flex items-center gap-1.5"><Image size={12} /> Chọn Files</span>
                  </button>
                </div>
              </div>

              {/* Hidden inputs */}
              <input
                ref={folderInputRef}
                type="file"
                multiple
                webkitdirectory=""
                directory=""
                onChange={handleFolderSelect}
                className="hidden"
              />
              <input
                ref={fileInputRef}
                type="file"
                multiple
                accept="image/*"
                onChange={handleFileSelect}
                className="hidden"
              />

              {/* Sorted file list with rename */}
              {files.length > 0 && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-dark-500">
                      {files.length} ảnh đã sắp xếp • {formatFileSize(files.reduce((s, f) => s + f.size, 0))}
                    </span>
                    <button
                      type="button"
                      onClick={clearAllFiles}
                      className="text-xs text-red-400 hover:text-red-300 transition-colors cursor-pointer"
                    >
                      Xoá tất cả
                    </button>
                  </div>

                  <div className="max-h-56 overflow-y-auto space-y-1.5 pr-1 custom-scrollbar">
                    {files.map((fileItem, idx) => (
                      <div
                        key={idx}
                        className="flex items-center gap-3 px-3 py-2 rounded-lg bg-dark-800/60 border border-dark-700/30 group hover:border-dark-700/60 transition-all"
                      >
                        {/* Page number */}
                        <span className="text-[10px] text-primary-400 font-mono font-bold w-5 text-center shrink-0">
                          {idx + 1}
                        </span>

                        {/* Thumbnail */}
                        <img
                          src={fileItem.preview}
                          alt={fileItem.displayName}
                          className="w-8 h-10 rounded object-cover border border-dark-700/50 shrink-0"
                        />

                        {/* Name (editable) */}
                        <div className="flex-1 min-w-0">
                          {editingIdx === idx ? (
                            <div className="flex items-center gap-1">
                              <input
                                type="text"
                                value={editValue}
                                onChange={(e) => setEditValue(e.target.value)}
                                onKeyDown={(e) => {
                                  if (e.key === 'Enter') confirmRename();
                                  if (e.key === 'Escape') cancelRename();
                                }}
                                autoFocus
                                className="flex-1 px-2 py-0.5 rounded bg-dark-700 border border-primary-500/50 text-xs text-dark-200 focus:outline-none"
                              />
                              <button type="button" onClick={confirmRename} className="text-emerald-400 hover:text-emerald-300 cursor-pointer">
                                <Check size={12} />
                              </button>
                              <button type="button" onClick={cancelRename} className="text-dark-500 hover:text-dark-300 cursor-pointer">
                                <X size={12} />
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center gap-1">
                              <p className="text-xs text-dark-300 truncate">{fileItem.displayName}</p>
                              <button
                                type="button"
                                onClick={() => startRename(idx)}
                                className="text-dark-600 hover:text-primary-400 opacity-0 group-hover:opacity-100 transition-all cursor-pointer shrink-0"
                                title="Đổi tên"
                              >
                                <Edit3 size={10} />
                              </button>
                            </div>
                          )}
                          <p className="text-[10px] text-dark-600">{formatFileSize(fileItem.size)}</p>
                        </div>

                        {/* Delete */}
                        <button
                          type="button"
                          onClick={() => removeFile(idx)}
                          className="p-1 text-dark-600 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-all cursor-pointer shrink-0"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Upload progress */}
          {submitting && !isNovel && uploadProgress.total > 0 && (
            <div className="px-4 py-3 rounded-xl bg-primary-500/5 border border-primary-500/20">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs text-dark-400">Đang tải lên...</span>
                <span className="text-xs font-mono text-primary-400">
                  {uploadProgress.current}/{uploadProgress.total}
                </span>
              </div>
              <div className="w-full h-1.5 rounded-full bg-dark-800 overflow-hidden">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-primary-600 to-primary-400 transition-all duration-300"
                  style={{ width: `${Math.round((uploadProgress.current / uploadProgress.total) * 100)}%` }}
                />
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2.5 rounded-xl text-sm font-medium text-dark-400 hover:text-dark-200 hover:bg-dark-800 transition-all cursor-pointer disabled:opacity-50"
            >
              Huỷ
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              {submitting ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Đang tải lên...
                </>
              ) : (
                <>
                  <Upload size={16} />
                  Tạo chương
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
