import axiosClient from '../utils/axiosClient';

const chapterReportService = {
  getPendingReports: async (isAdmin, page = 0, size = 10) => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.get(`${basePath}/chapter-reports`, {
      params: { status: 'PENDING', page, size }
    });
    return response.data;
  },

  getHandledReports: async (isAdmin, page = 0, size = 10) => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.get(`${basePath}/chapter-reports`, {
      params: { status: 'HANDLED', page, size }
    });
    return response.data;
  },

  getAllReports: async (isAdmin, page = 0, size = 10, status = '') => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.get(`${basePath}/chapter-reports`, {
      params: { page, size, status }
    });
    return response.data;
  },

  resolveReport: async (isAdmin, reportId, data) => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.put(`${basePath}/chapter-reports/${reportId}/status`, { ...data, action: 'RESOLVED' });
    return response.data;
  },

  rejectReport: async (isAdmin, reportId, data) => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.put(`${basePath}/chapter-reports/${reportId}/status`, { ...data, action: 'REJECTED' });
    return response.data;
  },

  getChapterDetail: async (chapterId) => {
    const response = await axiosClient.get(`/uploader/chapters/view/${chapterId}/detail`);
    return response.data;
  }
};

export default chapterReportService;
