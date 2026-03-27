# Lộ trình phát triển ComicVerse

## Phase 1: Discovery & Alignment (Tuần 1)
- **Mục tiêu:** Bảo đảm team thống nhất với vision sản phẩm, phạm vi và yêu cầu nghiệp vụ.
- **Công việc chính:**
  - Review tài liệu `android_design_document.md`, phỏng vấn stakeholder để xác nhận business rules.
  - Phân tích user persona, flow đọc truyện, VIP, notification.
  - Xác định KPI, tiêu chí thành công, constraint kỹ thuật.
- **Deliverables:** Vision statement, scope doc, backlog cấp cao, risk register.
- **Phụ thuộc:** None (khởi đầu dự án).

## Phase 2: Architecture & Planning (Tuần 2)
- **Mục tiêu:** Đóng khung Clean Architecture + MVVM, xác định module và chuẩn hóa coding guideline.
- **Công việc chính:**
  - Từ `plan.md`, cụ thể hóa layer (Domain/Data/Presentation) và package structure.
  - Thiết kế module split (core, feature, integration).
  - Định nghĩa contract UseCase/Repository/DataSource, chuẩn hóa error/state handling RxJava.
  - Lập kế hoạch sprint, assign dev lead từng module.
- **Deliverables:** Architecture blueprint, module diagram, coding guidelines, sprint plan.
- **Phụ thuộc:** Phase 1 hoàn tất.

## Phase 3: Foundation Implementation (Tuần 3-4)
- **Mục tiêu:** Dựng nền tảng kỹ thuật tái dùng cho toàn dự án.
- **Công việc chính:**
  - Cấu hình project Gradle, Hilt, Retrofit, Room, Paging 3, Glide.
  - Implement core modules: `NetworkModule`, `DatabaseModule`, `RepositoryModule`, logging/interceptor, base ViewModel.
  - Thiết lập CI (lint, unit test), feature flags, môi trường dev/stage.
- **Deliverables:** Chạy được skeleton app với dependency injection hoàn chỉnh, CI pipeline cơ bản.
- **Phụ thuộc:** Blueprint từ Phase 2.

## Phase 4: Feature Development (Tuần 5-10)
- **Mục tiêu:** Hoàn thiện các tính năng chính theo ưu tiên.
- **Công việc chính (theo module):**
  1. **Auth (Tuần 5):** Login/Register, token refresh interceptor, onboarding.
  2. **Discover (Tuần 6):** Home feed với Paging, search debounce RxJava, trending slider.
  3. **Comic Detail (Tuần 7):** Detail screen, chapter list, AI summary, comment bottom sheet.
  4. **Reader (Tuần 8):** Comic & Novel reader, Glide preload, reading history sync.
  5. **VIP & Payment (Tuần 9):** VNPay flow, subscription state, paywall.
  6. **Notification & Profile (Tuần 10):** FCM, deep link, profile settings.
- **Deliverables:** Mỗi module có spec, code, unit test, integration test tối thiểu.
- **Phụ thuộc:** Foundation ổn định; module có thể song song nhưng phải tuân dependency.

## Phase 5: Integration & Performance (Tuần 11)
- **Mục tiêu:** Ghép module, tối ưu hiệu năng đọc truyện và xử lý lỗi.
- **Công việc chính:**
  - End-to-end data flow test (API ↔ Repository ↔ ViewModel ↔ UI).
  - Optimize Glide cache, Room query, Paging prefetch.
  - Bổ sung analytics hook, crash reporting, log chuẩn.
- **Deliverables:** Integration test report, performance benchmark (fps, memory), error handling checklist.
- **Phụ thuộc:** Các module Phase 4 hoàn thành.

## Phase 6: Testing & QA (Tuần 12)
- **Mục tiêu:** Đảm bảo chất lượng thông qua test tự động và QA manual.
- **Công việc chính:**
  - Unit test coverage ≥80% cho UseCase/Repository, UI test với Espresso.
  - Regression test scenario (auth flow, payment, offline read).
  - Security review (token storage, deeplink).
- **Deliverables:** Test report, bug triage, sign-off từ QA.
- **Phụ thuộc:** Integration ổn định.

## Phase 7: Release Preparation (Tuần 13)
- **Mục tiêu:** Chuẩn bị phát hành Play Store và rollout nội bộ.
- **Công việc chính:**
  - Build variant dev/stage/prod, signing config, ProGuard/R8, app bundle.
  - VNPay sandbox → production checklist, FCM key rotation.
  - Viết release note, onboarding material, dữ liệu seed.
- **Deliverables:** Release candidate build, Play Console artifacts, release checklist.
- **Phụ thuộc:** QA sign-off.

## Phase 8: Post-launch Ops (Tuần 14+)
- **Mục tiêu:** Theo dõi, hỗ trợ và cải tiến sau khi phát hành.
- **Công việc chính:**
  - Monitor crash/analytics, xử lý hotfix nếu có.
  - Thu thập feedback, lập backlog cải tiến (Recommendation AI, community features...).
  - Lập lịch cập nhật định kỳ, bảo trì SDK (VNPay, Firebase).
- **Deliverables:** Ops runbook, metrics dashboard, roadmap iteration.
- **Phụ thuộc:** Release thành công.

