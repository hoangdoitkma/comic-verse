import { useState, useRef, useEffect } from 'react';
import { X, Upload, Image, FileText, Palette, Link as LinkIcon, Tag, User, ChevronDown, Lock } from 'lucide-react';
import adminService from '../services/adminService';
import comicService from '../services/comicService';

function toSlug(str) {
  if (!str) return '';
  const from = 'àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđ';
  const to   = 'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd';
  let result = str.toLowerCase().trim();
  for (let i = 0; i < from.length; i++) {
    result = result.replaceAll(from[i], to[i]);
  }
  return result
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/[\s]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

export default function AdminComicModal({ isOpen, onClose, onSuccess, initialData = null }) {
  const isEditing = !!initialData;

  const [formData, setFormData] = useState({
    title: '',
    slug: '',
    synopsis: '',
    contentType: 'COMIC',
    comicFormat: 'COLOR',
    accessType: 'FREE',
    authorId: '',
    ageRatingId: '',
  });

  const [slugEdited, setSlugEdited] = useState(false);
  const [thumbnail, setThumbnail] = useState(null);
  const [thumbnailPreview, setThumbnailPreview] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [authors, setAuthors] = useState([]);
  const [loadingAuthors, setLoadingAuthors] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      fetchAuthors();
      if (initialData) {
        setFormData({
          title: initialData.title || '',
          slug: initialData.slug || '',
          synopsis: initialData.synopsis || '',
          contentType: initialData.contentType || 'COMIC',
          comicFormat: initialData.comicFormat || 'COLOR',
          accessType: initialData.accessType || 'FREE',
          authorId: initialData.author?.id || initialData.authorId || '',
          ageRatingId: initialData.ageRating?.id || initialData.ageRatingId || '',
        });
        setThumbnailPreview(initialData.thumbnailUrl || null);
        setSlugEdited(true); // Don't auto-regen slug when editing
      } else {
        resetForm();
      }
    }
  }, [isOpen, initialData]);

  const fetchAuthors = async () => {
    try {
      setLoadingAuthors(true);
      const res = await adminService.getAuthors();
      if (res && res.data) {
          setAuthors(res.data);
      } else {
          setAuthors(res || []);
      }
    } catch (err) {
      console.error('Lỗi khi tải danh sách tác giả:', err);
      // Fallback if adminService.getAuthors not correctly implemented maybe it's in comicService
      try {
          const res2 = await comicService.getAuthors();
          setAuthors(res2 || []);
      } catch(e) {}
    } finally {
      setLoadingAuthors(false);
    }
  };

  useEffect(() => {
    if (!slugEdited && formData.title && !isEditing) {
      setFormData((prev) => ({ ...prev, slug: toSlug(prev.title) }));
    }
  }, [formData.title, slugEdited, isEditing]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSlugChange = (e) => {
    setSlugEdited(true);
    const value = e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, '');
    setFormData((prev) => ({ ...prev, slug: value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setThumbnail(file);
      setThumbnailPreview(URL.createObjectURL(file));
    }
  };

  const removeThumbnail = () => {
    setThumbnail(null);
    if (!isEditing && thumbnailPreview) {
        URL.revokeObjectURL(thumbnailPreview);
    }
    setThumbnailPreview(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const resetForm = () => {
    setFormData({ title: '', slug: '', synopsis: '', contentType: 'COMIC', comicFormat: 'COLOR', accessType: 'FREE', authorId: '', ageRatingId: '' });
    setSlugEdited(false);
    setThumbnail(null);
    setThumbnailPreview(null);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.title.trim()) {
      setError('Vui lòng nhập tiêu đề truyện');
      return;
    }
    if (!formData.slug.trim()) {
      setError('Vui lòng nhập tên không dấu (slug)');
      return;
    }

    setSubmitting(true);
    try {
      if (isEditing) {
        // Edit mode (assuming no image update support or sending as json without image for now)
        // If image update is needed, the backend PUT /api/admin/comics/{id} should support Multipart
        // For simplicity, we just send as JSON
        const payload = {
          title: formData.title.trim(),
          slug: formData.slug.trim(),
          synopsis: formData.synopsis.trim(),
          contentType: formData.contentType,
          comicFormat: formData.comicFormat,
          accessType: formData.accessType,
          authorId: formData.authorId ? parseInt(formData.authorId) : null,
          ageRatingId: formData.ageRatingId ? parseInt(formData.ageRatingId) : null,
        };
        await adminService.updateComic(initialData.id, payload);
      } else {
        // Create mode
        if (!thumbnail) {
            setError('Bắt buộc phải tải ảnh bìa khi tạo truyện mới');
            setSubmitting(false);
            return;
        }
        const data = new FormData();
        data.append('title', formData.title.trim());
        data.append('slug', formData.slug.trim());
        if (formData.synopsis.trim()) data.append('synopsis', formData.synopsis.trim());
        data.append('contentType', formData.contentType);
        data.append('comicFormat', formData.comicFormat);
        data.append('accessType', formData.accessType);
        if (formData.authorId) data.append('authorId', formData.authorId);
        if (formData.ageRatingId) data.append('ageRatingId', formData.ageRatingId);
        data.append('thumbnail', thumbnail);
  
        await adminService.createComic(data);
      }
      
      resetForm();
      onSuccess?.();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || `Đã xảy ra lỗi khi ${isEditing ? 'cập nhật' : 'tạo'} truyện. Vui lòng thử lại.`;
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      <div className="relative bg-dark-900 border border-dark-700/50 rounded-2xl shadow-2xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto animate-scale-in custom-scrollbar">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-dark-700/50 sticky top-0 bg-dark-900 z-10">
          <h2 className="text-lg font-semibold text-white">
              {isEditing ? 'Sửa thông tin truyện' : 'Thêm truyện mới'}
          </h2>
          <button onClick={onClose} className="text-dark-400 hover:text-white transition-colors cursor-pointer p-1">
            <X size={18} />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {error && (
            <div className="px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/20 text-sm text-red-400">
              {error}
            </div>
          )}

          {/* Title */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
              <FileText size={14} />
              Tiêu đề truyện <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="Ví dụ: Đấu La Đại Lục"
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
              <LinkIcon size={14} />
              Tên không dấu (Slug) <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={formData.slug}
              onChange={handleSlugChange}
              disabled={isEditing}
              placeholder="dau-la-dai-luc"
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all font-mono disabled:opacity-50"
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300">Tóm tắt nội dung</label>
            <textarea
              name="synopsis"
              value={formData.synopsis}
              onChange={handleChange}
              placeholder="Viết mô tả ngắn gọn về truyện..."
              rows={3}
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all resize-none custom-scrollbar"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <FileText size={14} />
                Loại nội dung
              </label>
              <select
                name="contentType"
                value={formData.contentType}
                onChange={handleChange}
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer"
              >
                <option value="COMIC">Truyện tranh (Comic)</option>
                <option value="NOVEL">Tiểu thuyết (Novel)</option>
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <Palette size={14} />
                Định dạng
              </label>
              <select
                name="comicFormat"
                value={formData.comicFormat}
                onChange={handleChange}
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer"
              >
                <option value="COLOR">Màu (Color)</option>
                <option value="BLACK_WHITE">Đen trắng (B&W)</option>
              </select>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
              <Lock size={14} />
              Quyền truy cập
            </label>
            <select
              name="accessType"
              value={formData.accessType}
              onChange={handleChange}
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer"
            >
              <option value="FREE">Miễn phí (Free)</option>
              <option value="VIP">Thu phí (VIP)</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <User size={14} />
                Tác giả
              </label>
              <div className="relative">
                <select
                  name="authorId"
                  value={formData.authorId}
                  onChange={handleChange}
                  disabled={loadingAuthors}
                  className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer appearance-none disabled:opacity-50"
                >
                  <option value="">
                    {loadingAuthors ? 'Đang tải...' : '— Chọn tác giả —'}
                  </option>
                  {authors.map((author) => (
                    <option key={author.id} value={author.id}>
                      {author.name}{author.studio ? ` (${author.studio})` : ''}
                    </option>
                  ))}
                </select>
                <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-dark-500 pointer-events-none" />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <Tag size={14} />
                Giới hạn tuổi (ID)
              </label>
              <input
                type="number"
                name="ageRatingId"
                value={formData.ageRatingId}
                onChange={handleChange}
                placeholder="Tuỳ chọn"
                min="1"
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all"
              />
            </div>
          </div>

          {!isEditing && (
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <Image size={14} />
                Ảnh bìa (Thumbnail)
              </label>

              {!thumbnailPreview ? (
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="w-full border-2 border-dashed border-dark-700/50 hover:border-primary-500/40 rounded-xl py-8 flex flex-col items-center gap-3 transition-all duration-200 group cursor-pointer"
                >
                  <div className="w-12 h-12 rounded-xl bg-dark-800 group-hover:bg-primary-500/10 flex items-center justify-center transition-all">
                    <Upload size={22} className="text-dark-500 group-hover:text-primary-400 transition-colors" />
                  </div>
                  <div>
                    <p className="text-sm text-dark-400 group-hover:text-dark-300 transition-colors">Click để chọn ảnh bìa</p>
                    <p className="text-xs text-dark-600 mt-1">PNG, JPG, WEBP (tối đa 5MB)</p>
                  </div>
                </button>
              ) : (
                <div className="relative group">
                  <img src={thumbnailPreview} alt="Preview" className="w-full h-44 object-cover rounded-xl border border-dark-700/50" />
                  <button
                    type="button"
                    onClick={removeThumbnail}
                    className="absolute top-2 right-2 w-8 h-8 rounded-lg bg-dark-900/80 hover:bg-red-500/80 flex items-center justify-center text-dark-300 hover:text-white transition-all cursor-pointer opacity-0 group-hover:opacity-100"
                  >
                    <X size={16} />
                  </button>
                  <p className="text-xs text-dark-500 mt-2 truncate">{thumbnail?.name}</p>
                </div>
              )}

              <input ref={fileInputRef} type="file" accept="image/*" onChange={handleFileChange} className="hidden" />
            </div>
          )}

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
              disabled={submitting}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-500 text-white text-sm font-semibold transition-all duration-200 shadow-lg shadow-primary-600/25 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              {submitting ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Đang lưu...
                </>
              ) : (
                <>
                  {isEditing ? <FileText size={16} /> : <Upload size={16} />}
                  {isEditing ? 'Lưu thay đổi' : 'Tạo truyện'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
