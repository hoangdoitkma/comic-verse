import axiosClient from '../utils/axiosClient';

const authService = {
  /**
   * Đăng nhập: POST /api/auth/login
   * Response data: { token, type, id, email, roles }
   */
  login: async (email, password) => {
    const response = await axiosClient.post('/auth/login', { email, password });

    if (response.data) {
      const { token, id, email: userEmail, roles } = response.data;

      // Lưu token và thông tin user vào localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({
        id,
        email: userEmail,
        roles,
      }));
    }

    return response.data;
  },

  /**
   * Đăng xuất: Xóa token và user info
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },

  /**
   * Lấy thông tin user hiện tại từ localStorage
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  },

  /**
   * Kiểm tra user đã đăng nhập chưa
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  /**
   * Kiểm tra user có role cụ thể không
   */
  hasRole: (role) => {
    const user = authService.getCurrentUser();
    return user?.roles?.includes(role) || false;
  },

  /**
   * Lấy role chính của user (ưu tiên ADMIN > UPLOADER > USER)
   */
  getPrimaryRole: () => {
    const user = authService.getCurrentUser();
    if (!user?.roles) return null;

    if (user.roles.includes('ROLE_ADMIN')) return 'ROLE_ADMIN';
    if (user.roles.includes('ROLE_UPLOADER')) return 'ROLE_UPLOADER';
    return 'ROLE_USER';
  },
};

export default authService;
