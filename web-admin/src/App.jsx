import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './routes/PrivateRoute';
import LoginPage from './pages/LoginPage';

// Uploader
import UploaderLayout from './layouts/UploaderLayout';
import ComicsPage from './pages/uploader/ComicsPage';
import ComicDetailPage from './pages/uploader/ComicDetailPage';
import BulkUploadChaptersPage from './pages/uploader/BulkUploadChaptersPage';
import BulkUploadNovelPage from './pages/uploader/BulkUploadNovelPage';

// Admin
import AdminLayout from './layouts/AdminLayout';
import DashboardPage from './pages/admin/DashboardPage';
import ApprovalQueuePage from './pages/admin/ApprovalQueuePage';
import UsersPage from './pages/admin/UsersPage';
import GenresPage from './pages/admin/GenresPage';
import AuthorsPage from './pages/admin/AuthorsPage';
import VipPackagesPage from './pages/admin/VipPackagesPage';
import AdminComicApprovalPage from './pages/admin/AdminComicApprovalPage';
import NotificationsPage from './pages/admin/NotificationsPage';
import AdminComicsPage from './pages/admin/AdminComicsPage';

// Shared
import ChapterReportsPage from './pages/shared/ChapterReportsPage';

// Revenue
import RevenuePage from './pages/admin/RevenuePage';
import TransactionsPage from './pages/admin/TransactionsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* ===== Public Routes ===== */}
        <Route path="/login" element={<LoginPage />} />

        {/* ===== Admin Routes (ROLE_ADMIN) ===== */}
        <Route
          element={<PrivateRoute allowedRoles={['ROLE_ADMIN']} />}
        >
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="approval-queue" element={<ApprovalQueuePage />} />
            <Route path="approval-queue/comics/:comicId" element={<AdminComicApprovalPage />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="genres" element={<GenresPage />} />
            <Route path="authors" element={<AuthorsPage />} />
            <Route path="comics" element={<AdminComicsPage />} />
            <Route path="vip-packages" element={<VipPackagesPage />} />
            <Route path="notifications" element={<NotificationsPage />} />
            <Route path="revenue" element={<RevenuePage />} />
            <Route path="transactions" element={<TransactionsPage />} />
            <Route path="reports" element={<ChapterReportsPage />} />
          </Route>
        </Route>

        {/* ===== Uploader Routes (ROLE_UPLOADER hoặc ROLE_ADMIN) ===== */}
        <Route
          element={
            <PrivateRoute allowedRoles={['ROLE_UPLOADER', 'ROLE_ADMIN']} />
          }
        >
          <Route path="/uploader" element={<UploaderLayout />}>
            <Route index element={<Navigate to="/uploader/comics" replace />} />
            <Route path="comics" element={<ComicsPage />} />
            <Route path="comics/:comicId" element={<ComicDetailPage />} />
            <Route path="comics/:comicId/bulk-upload" element={<BulkUploadChaptersPage />} />
            <Route path="comics/:comicId/bulk-upload-novel" element={<BulkUploadNovelPage />} />
            <Route path="reports" element={<ChapterReportsPage />} />
          </Route>
        </Route>

        {/* ===== Redirect mặc định ===== */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
