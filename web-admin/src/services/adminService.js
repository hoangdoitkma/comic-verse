import axiosClient from '../utils/axiosClient';

const adminService = {
  // ==================== MODERATION ====================

  /**
   * Lấy danh sách upload logs đang chờ duyệt
   * GET /api/admin/moderation/logs/pending
   */
  getPendingLogs: async () => {
    const response = await axiosClient.get('/admin/moderation/logs/pending');
    return response.data;
  },

  /**
   * Lấy thông tin chi tiết của một comic (có thể dùng cho moderation)
   * GET /api/admin/moderation/comics/{id}
   */
  getComicById: async (id) => {
    const response = await axiosClient.get(`/admin/moderation/comics/${id}`);
    return response.data;
  },

  /**
   * Duyệt hoặc từ chối upload log
   * PUT /api/admin/moderation/logs/{logId}
   * @param {number} logId
   * @param {Object} data - { status: "APPROVED"|"REJECTED", reason?: "..." }
   */
  reviewLog: async (logId, data) => {
    const response = await axiosClient.put(`/admin/moderation/logs/${logId}`, data);
    return response.data;
  },

  // ==================== USERS ====================

  /**
   * Lấy danh sách users (phân trang + filter)
   * GET /api/admin/users?role=&status=&page=0&size=10
   */
  getUsers: async (params = {}) => {
    const response = await axiosClient.get('/admin/users', { params });
    return response.data;
  },

  /**
   * Cập nhật trạng thái user
   * PUT /api/admin/users/{userId}/status
   * @param {number} userId
   * @param {Object} data - { status: "ACTIVE"|"BANNED"|"SUSPENDED" }
   */
  updateUserStatus: async (userId, data) => {
    const response = await axiosClient.put(`/admin/users/${userId}/status`, data);
    return response.data;
  },

  // ==================== GENRES ====================

  getGenres: async () => {
    const response = await axiosClient.get('/admin/data/genres');
    return response.data;
  },

  getGenreById: async (id) => {
    const response = await axiosClient.get(`/admin/data/genres/${id}`);
    return response.data;
  },

  createGenre: async (data) => {
    const response = await axiosClient.post('/admin/data/genres', data);
    return response.data;
  },

  updateGenre: async (id, data) => {
    const response = await axiosClient.put(`/admin/data/genres/${id}`, data);
    return response.data;
  },

  deleteGenre: async (id) => {
    const response = await axiosClient.delete(`/admin/data/genres/${id}`);
    return response.data;
  },

  // ==================== AUTHORS ====================

  getAuthors: async () => {
    const response = await axiosClient.get('/admin/data/authors');
    return response.data;
  },

  getAuthorById: async (id) => {
    const response = await axiosClient.get(`/admin/data/authors/${id}`);
    return response.data;
  },

  createAuthor: async (data) => {
    const response = await axiosClient.post('/admin/data/authors', data);
    return response.data;
  },

  updateAuthor: async (id, data) => {
    const response = await axiosClient.put(`/admin/data/authors/${id}`, data);
    return response.data;
  },

  deleteAuthor: async (id) => {
    const response = await axiosClient.delete(`/admin/data/authors/${id}`);
    return response.data;
  },

  // ==================== VIP PACKAGES ====================

  getVipPackages: async () => {
    const response = await axiosClient.get('/admin/data/vip-packages');
    return response.data;
  },

  getVipPackageById: async (id) => {
    const response = await axiosClient.get(`/admin/data/vip-packages/${id}`);
    return response.data;
  },

  createVipPackage: async (data) => {
    const response = await axiosClient.post('/admin/data/vip-packages', data);
    return response.data;
  },

  updateVipPackage: async (id, data) => {
    const response = await axiosClient.put(`/admin/data/vip-packages/${id}`, data);
    return response.data;
  },

  deleteVipPackage: async (id) => {
    const response = await axiosClient.delete(`/admin/data/vip-packages/${id}`);
    return response.data;
  },

  // ==================== DASHBOARD ====================

  getDashboardSummary: async () => {
    const response = await axiosClient.get('/admin/dashboard/summary');
    return response.data;
  },

  // ==================== TRANSACTIONS ====================

  /**
   * Lấy danh sách giao dịch VIP
   * GET /api/admin/transactions?page=0&size=10
   */
  getTransactions: async (params = {}) => {
    const response = await axiosClient.get('/admin/transactions', { params });
    return response.data;
  },
};

export default adminService;
