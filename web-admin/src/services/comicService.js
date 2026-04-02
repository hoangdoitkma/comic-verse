import axiosClient from '../utils/axiosClient';

const comicService = {
  /**
   * Lấy danh sách truyện của Uploader hiện tại
   * GET /api/uploader/comics
   */
  getMyComics: async () => {
    const response = await axiosClient.get('/uploader/comics');
    return response.data;
  },

  /**
   * Lấy danh sách tác giả
   * GET /api/data/authors
   */
  getAuthors: async () => {
    const response = await axiosClient.get('/data/authors');
    return response.data;
  },

  /**
   * Tạo truyện mới (multipart/form-data)
   * POST /api/uploader/comics
   * @param {FormData} formData - Bao gồm: title, synopsis, authorId, ageRatingId, contentType, comicFormat, thumbnail (File)
   */
  createComic: async (formData) => {
    const response = await axiosClient.post('/uploader/comics', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  /**
   * Lấy danh sách chương của một truyện
   * GET /api/uploader/chapters/{comicId}
   */
  getChapters: async (comicId) => {
    const response = await axiosClient.get(`/uploader/chapters/${comicId}`);
    return response.data;
  },

  /**
   * Lấy số chương lớn nhất hiện có
   * GET /api/uploader/chapters/{comicId}/max-chapter-number
   */
  getMaxChapterNumber: async (comicId) => {
    const response = await axiosClient.get(`/uploader/chapters/${comicId}/max-chapter-number`);
    return response.data;
  },

  /**
   * Init single chapter: tạo Chapter + ChapterPage records (imageUrl=null)
   * POST /api/uploader/chapters/{comicId}/init-single
   * @param {string|number} comicId
   * @param {Object} data - {chapterNumber, title, accessType, pageFileNames: string[]}
   * @returns {Object} ChapterInitResult with pages mapping [{pageId, pageNumber, fileName}]
   */
  initSingleChapter: async (comicId, data) => {
    const response = await axiosClient.post(`/uploader/chapters/${comicId}/init-single`, data);
    return response.data;
  },

  /**
   * Tạo chương truyện tranh (multipart/form-data)
   * POST /api/uploader/chapters/{comicId}/comic
   * @param {string|number} comicId
   * @param {FormData} formData - Bao gồm: chapterNumber, title, accessType, pages (File[])
   */
  createComicChapter: async (comicId, formData) => {
    const response = await axiosClient.post(`/uploader/chapters/${comicId}/comic`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  /**
   * Tạo chương tiểu thuyết (JSON)
   * POST /api/uploader/chapters/{comicId}/novel
   */
  createNovelChapter: async (comicId, data) => {
    const response = await axiosClient.post(`/uploader/chapters/${comicId}/novel`, data);
    return response.data;
  },

  /**
   * Step 1: Init bulk chapters - tạo Chapter + ChapterPage records trong DB
   * POST /api/uploader/chapters/{comicId}/bulk-init
   * @param {string|number} comicId
   * @param {Object} metadata - { chapters: [{folderName, title, accessType, pageFileNames}] }
   * @returns {Object} BulkInitResponse with chapter IDs and page mappings
   */
  initBulkChapters: async (comicId, metadata) => {
    const response = await axiosClient.post(`/uploader/chapters/${comicId}/bulk-init`, metadata);
    return response.data;
  },

  /**
   * Step 2: Upload a single page file
   * POST /api/uploader/chapters/bulk-upload-page/{pageId}
   * @param {number} pageId - ChapterPage ID from initBulkChapters response
   * @param {File} file - image file to upload
   */
  uploadChapterPage: async (pageId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axiosClient.post(`/uploader/chapters/bulk-upload-page/${pageId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
  /**
   * Lấy danh sách ảnh trang của chương (sorted by pageNumber)
   * GET /api/uploader/chapters/view/{chapterId}/pages
   */
  getChapterPages: async (chapterId) => {
    const response = await axiosClient.get(`/uploader/chapters/view/${chapterId}/pages`);
    return response.data;
  },

  /**
   * Lấy chi tiết nội dung chương (bao gồm content text và pages)
   * GET /api/uploader/chapters/view/{chapterId}/detail
   */
  getChapterDetail: async (chapterId) => {
    const response = await axiosClient.get(`/uploader/chapters/view/${chapterId}/detail`);
    return response.data;
  },

  /**
   * Xóa bản nháp chương bị từ chối
   * DELETE /api/uploader/chapters/{chapterId}/rejected-draft
   */
  deleteRejectedDraft: async (chapterId) => {
    const response = await axiosClient.delete(`/uploader/chapters/${chapterId}/rejected-draft`);
    return response.data;
  },
};

export default comicService;
