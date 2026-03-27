import axiosClient from '../utils/axiosClient';

const notificationService = {
  // === User Endpoints ===
  getUserNotifications: async () => {
    const response = await axiosClient.get('/notifications');
    return response;
  },

  getUnreadCount: async () => {
    const response = await axiosClient.get('/notifications/unread-count');
    return response;
  },

  markAsRead: async (id) => {
    const response = await axiosClient.put(`/notifications/${id}/read`);
    return response;
  },

  markAllAsRead: async () => {
    const response = await axiosClient.put('/notifications/read-all');
    return response;
  },

  // === Admin Endpoints ===
  sendNotification: async (data) => {
    const response = await axiosClient.post('/admin/notifications/send', data);
    return response;
  },

  getNotificationHistory: async () => {
    const response = await axiosClient.get('/admin/notifications/history');
    return response;
  }
};

export default notificationService;
