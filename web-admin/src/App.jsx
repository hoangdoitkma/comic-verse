import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './routes/PrivateRoute';

// Layout
import MainLayout from './layouts/MainLayout';

// Pages
import LoginPage from './pages/auth/LoginPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import RevenuePage from './pages/dashboard/RevenuePage';

import ComicsPage from './pages/content/ComicsPage';
import ComicDetailPage from './pages/content/ComicDetailPage';
import ApprovalQueuePage from './pages/content/ApprovalQueuePage';
import AdminComicApprovalPage from './pages/content/AdminComicApprovalPage';
import GenresPage from './pages/content/GenresPage';
import AuthorsPage from './pages/content/AuthorsPage';
import BulkUploadChaptersPage from './pages/content/BulkUploadChaptersPage';
import BulkUploadNovelPage from './pages/content/BulkUploadNovelPage';

import VipPackagesPage from './pages/monetization/VipPackagesPage';
import TransactionsPage from './pages/monetization/TransactionsPage';

import UsersPage from './pages/community/UsersPage';
import ChapterReportsPage from './pages/community/ChapterReportsPage';
import NotificationsPage from './pages/community/NotificationsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* ===== Public Route ===== */}
        <Route path="/login" element={<LoginPage />} />

        {/* ===== VÙNG BẢO VỆ CHUNG CHO MỌI ROLE ===== */}
        <Route element={<MainLayout />}>
          
          {/* Nhóm Content & Report: Dành cho CẢ Admin & Uploader */}
          <Route element={<PrivateRoute allowedRoles={['ROLE_ADMIN', 'ROLE_UPLOADER']} />}>
             <Route path="/content/comics" element={<ComicsPage />} />
             <Route path="/content/comics/:comicId" element={<ComicDetailPage />} />
             <Route path="/content/comics/:comicId/bulk-upload" element={<BulkUploadChaptersPage />} />
             <Route path="/content/comics/:comicId/bulk-upload-novel" element={<BulkUploadNovelPage />} />
             <Route path="/community/reports" element={<ChapterReportsPage />} />
          </Route>

          {/* Các nhóm còn lại: CHỈ Dành cho Admin */}
          <Route element={<PrivateRoute allowedRoles={['ROLE_ADMIN']} />}>
             {/* Dashboard */}
             <Route path="/dashboard" element={<DashboardPage />} />
             <Route path="/revenue" element={<RevenuePage />} />
             
             {/* Quản lý danh mục cốt lõi */}
             <Route path="/content/approval-queue" element={<ApprovalQueuePage />} />
             <Route path="/content/approval-queue/comics/:comicId" element={<AdminComicApprovalPage />} />
             <Route path="/content/genres" element={<GenresPage />} />
             <Route path="/content/authors" element={<AuthorsPage />} />
             
             {/* Monetization */}
             <Route path="/monetization/vip-packages" element={<VipPackagesPage />} />
             <Route path="/monetization/transactions" element={<TransactionsPage />} />
             
             {/* Users */}
             <Route path="/community/users" element={<UsersPage />} />
             <Route path="/community/notifications" element={<NotificationsPage />} />
          </Route>

        </Route>
        
        {/* ===== Redirect Fallback ===== */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
