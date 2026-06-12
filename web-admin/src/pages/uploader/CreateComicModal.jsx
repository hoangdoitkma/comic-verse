import { useState, useRef, useEffect } from 'react';
import { X, Upload, Image, FileText, Palette, Link as LinkIcon, Tag, User, ChevronDown, Lock } from 'lucide-react';
import comicService from '../../services/comicService';

/**
 * Chuyển đổi tiếng Việt có dấu thành không dấu, lowercase, dùng - ngăn cách
 * Ví dụ: "Đấu La Đại Lục" -> "dau-la-dai-luc"
 */
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

export default function CreateComicModal({ isOpen, onClose, onSuccess }) {
  const [formData, setFormData] = useState({
    title: '',
    slug: '',
    synopsis: '',
    contentType: 'COMIC',
    comicFormat: 'COLOR',
    accessType: 'FREE',
    authorId: '',
    ageRatingId: '',
    publishStatus: 'ONGOING',
    originCountry: 'KOREA',
    genreIds: [],
  });
  const [slugEdited, setSlugEdited] = useState(false);
  const [thumbnail, setThumbnail] = useState(null);
  const [thumbnailPreview, setThumbnailPreview] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [authors, setAuthors] = useState([]);
  const [loadingAuthors, setLoadingAuthors] = useState(false);
  const [genres, setGenres] = useState([]);
  const [loadingGenres, setLoadingGenres] = useState(false);
  const fileInputRef = useRef(null);

  // Fetch authors and genres when modal opens
  useEffect(() => {
    if (isOpen) {
      fetchAuthors();
      fetchGenres();
    }
  }, [isOpen]);

  const fetchAuthors = async () => {
    try {
      setLoadingAuthors(true);
      const data = await comicService.getAuthors();
      setAuthors(data || []);
    } catch (err) {
      console.error('Lỗi khi tải danh sách tác giả:', err);
      setAuthors([]);
    } finally {
      setLoadingAuthors(false);
    }
  };

  const fetchGenres = async () => {
    try {
      setLoadingGenres(true);
      const data = await comicService.getGenres();
      setGenres(data || []);
    } catch (err) {
      console.error('Lỗi khi tải danh sách thể loại:', err);
      setGenres([]);
    } finally {
      setLoadingGenres(false);
    }
  };

  // Auto-generate slug khi title thay đổi (nếu user chưa tự sửa slug)
  useEffect(() => {
    if (!slugEdited && formData.title) {
      setFormData((prev) => ({ ...prev, slug: toSlug(prev.title) }));
    }
  }, [formData.title, slugEdited]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const newData = { ...prev, [name]: value };
      if (name === 'contentType' && value === 'NOVEL') {
        newData.comicFormat = ''; // Xóa comicFormat nếu chọn C.Thuyết
      }
      return newData;
    });
  };

  const handleGenreToggle = (genreId) => {
    setFormData((prev) => {
      const currentIds = prev.genreIds || [];
      const newIds = currentIds.includes(genreId)
        ? currentIds.filter((id) => id !== genreId)
        : [...currentIds, genreId];
      return { ...prev, genreIds: newIds };
    });
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
    if (thumbnailPreview) URL.revokeObjectURL(thumbnailPreview);
    setThumbnailPreview(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const resetForm = () => {
    setFormData({ 
      title: '', slug: '', synopsis: '', contentType: 'COMIC', 
      comicFormat: 'COLOR', accessType: 'FREE', authorId: '', ageRatingId: '',
      publishStatus: 'ONGOING', originCountry: 'KOREA', genreIds: [] 
    });
    setSlugEdited(false);
    removeThumbnail();
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
      const data = new FormData();
      data.append('title', formData.title.trim());
      data.append('slug', formData.slug.trim());
      if (formData.synopsis.trim()) data.append('synopsis', formData.synopsis.trim());
      data.append('contentType', formData.contentType);
      if (formData.contentType !== 'NOVEL' && formData.comicFormat) {
          data.append('comicFormat', formData.comicFormat);
      }
      data.append('accessType', formData.accessType);
      data.append('publishStatus', formData.publishStatus);
      data.append('originCountry', formData.originCountry);
      if (formData.genreIds && formData.genreIds.length > 0) {
        formData.genreIds.forEach(id => data.append('genreIds', id));
      }
      if (formData.authorId) data.append('authorId', formData.authorId);
      if (formData.ageRatingId) data.append('ageRatingId', formData.ageRatingId);
      if (thumbnail) data.append('thumbnail', thumbnail);

      await comicService.createComic(data);
      resetForm();
      onSuccess?.();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || 'Đã xảy ra lỗi khi tạo truyện. Vui lòng thử lại.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      <div className="relative bg-dark-900 border border-dark-700/50 rounded-2xl shadow-2xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto animate-scale-in">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-dark-700/50 sticky top-0 bg-dark-900 z-10">
          <h2 className="text-lg font-semibold text-white">Thêm truyện mới</h2>
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

          {/* Slug */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
              <LinkIcon size={14} />
              Tên không dấu (Slug) <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={formData.slug}
              onChange={handleSlugChange}
              placeholder="dau-la-dai-luc"
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all font-mono"
            />
            <p className="text-[11px] text-dark-600">
              Tự động tạo từ tiêu đề. Dùng làm tên thư mục trên S3.
            </p>
          </div>

          {/* Synopsis */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-dark-300">Tóm tắt nội dung</label>
            <textarea
              name="synopsis"
              value={formData.synopsis}
              onChange={handleChange}
              placeholder="Viết mô tả ngắn gọn về truyện..."
              rows={3}
              className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 placeholder:text-dark-500 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all resize-none"
            />
          </div>

          {/* Row: contentType + comicFormat */}
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

            {formData.contentType !== 'NOVEL' && (
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
            )}
          </div>

          {/* Row: Status + Origin */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <FileText size={14} />
                Tiến độ xuất bản
              </label>
              <select
                name="publishStatus"
                value={formData.publishStatus}
                onChange={handleChange}
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer"
              >
                <option value="ONGOING">Đang tiến hành (Ongoing)</option>
                <option value="COMPLETED">Đã hoàn thành (Completed)</option>
                <option value="HIATUS">Tạm ngưng (Hiatus)</option>
                <option value="DROPPED">Hủy bỏ (Dropped)</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-dark-300 flex items-center gap-2">
                <LinkIcon size={14} />
                Quốc gia Origin
              </label>
              <select
                name="originCountry"
                value={formData.originCountry}
                onChange={handleChange}
                className="w-full px-4 py-2.5 rounded-xl bg-dark-800 border border-dark-700/50 text-sm text-dark-200 focus:outline-none focus:border-primary-500/50 focus:ring-1 focus:ring-primary-500/20 transition-all cursor-pointer"
              >
                <option value="KOREA">Hàn Quốc (Manhwa)</option>
                <option value="JAPAN">Nhật Bản (Manga)</option>
                <option value="CHINA">Trung Quốc (Manhua)</option>
                <option value="VIETNAM">Việt Nam</option>
                <option value="GLOBAL">Quốc tế (Global)</option>
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

          {/* Row: Author (dropdown) + Age Rating */}
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

          {/* Genres Multiselect */}
          <div className="space-y-2 border border-dark-700/50 p-4 rounded-xl bg-dark-800/50">
            <label className="text-sm font-medium text-dark-300 flex items-center gap-2 mb-2">
              <Tag size={14} />
              Thể loại (Genres)
            </label>
            {loadingGenres ? (
              <p className="text-sm text-dark-500">Đang tải danh sách thể loại...</p>
            ) : (
              <div className="flex flex-wrap gap-2 max-h-40 overflow-y-auto custom-scrollbar p-1">
                {genres.map(g => (
                  <label key={g.id} className="inline-flex items-center gap-2 bg-dark-800 border border-dark-700 hover:border-primary-500/40 px-3 py-1.5 rounded-lg cursor-pointer transition-colors">
                    <input 
                      type="checkbox" 
                      className="accent-primary-500 w-4 h-4 cursor-pointer"
                      checked={formData.genreIds.includes(g.id)}
                      onChange={() => handleGenreToggle(g.id)}
                    />
                    <span className="text-sm text-dark-200 select-none">{g.name}</span>
                  </label>
                ))}
                {genres.length === 0 && <p className="text-sm text-dark-500">Chưa có thể loại nào.</p>}
              </div>
            )}
          </div>

          {/* Thumbnail Upload */}
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
                  Đang tạo...
                </>
              ) : (
                <>
                  <Upload size={16} />
                  Tạo truyện
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
