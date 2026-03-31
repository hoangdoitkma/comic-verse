import axiosClient from '../utils/axiosClient';

const chapterReportService = {
  getPendingReports: async (isAdmin, page = 0, size = 10) => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.get(`${basePath}/chapter-reports`, {
      params: { status: 'PENDING', page, size }
    });
    return response.data;
  },

  // Because backend filters by an exact status, and we have both RESOLVED and REJECTED,
  // we could just fetch without status on history tab and manually filter. Or just GET all and filter on frontend for history?
  // Actually, we'll fetch all without status and filter out PENDING in frontend for the History tab 
  // or add a way to query multiple statuses in Backend. For simplicity, we just won't pass status for history in backend and then filter frontend.
  // Wait, if we fetch all, page size is skewed... Let's just pass status=RESOLVED for history.
  getResolvedReports: async (isAdmin, page = 0, size = 10) => {
    const basePath = isAdmin ? '/admin' : '/uploader';
    const response = await axiosClient.get(`${basePath}/chapter-reports`, {
      params: { status: 'RESOLVED', page, size }
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
  }
};

export default chapterReportService;
