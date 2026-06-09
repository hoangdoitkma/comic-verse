import axios from 'axios';

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const axiosClient = axios.create({
  baseURL: apiBaseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Gắn JWT Token vào mỗi request
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Xử lý lỗi tập trung
axiosClient.interceptors.response.use(
  (response) => {
    // Trả về data bên trong wrapper ApiResponse
    return response.data;
  },
  (error) => {
    const status = error.response?.status;

    // 401 Unauthorized -> Token hết hạn hoặc không hợp lệ
    if (status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }

    // 403 Forbidden -> Không có quyền truy cập
    if (status === 403) {
      console.error('Bạn không có quyền truy cập tài nguyên này.');
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
