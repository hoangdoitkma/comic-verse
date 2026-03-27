# ComicVerse Design Plan

## 1. Architecture
- **Mục tiêu**: Clean Architecture + MVVM với Java, RxJava, ViewModel, LiveData.
- **Các bước chính**:
  1. Tách `DomainLayer`, `DataLayer`, `PresentationLayer` theo Clean Architecture.
  2. Xác định contract chuẩn cho `UseCase`, `Repository`, `DataSource`.
  3. Chuẩn hóa error/state handling sử dụng RxJava (Single/Observable) cùng ViewModel scope.
- **Deliverables**: sơ đồ class/sequence mô t��� luồng `UI → ViewModel → UseCase → Repository → API/DB`.
- **Phụ thuộc**: constraint và plugin trong `build.gradle`, yêu cầu từ design document.

## 2. Modules
- **Mục tiêu**: Module hóa `core`, `ui-shared`, feature (Auth, Discover, ComicDetail, Reader, VIP, Notification), integration (VNPay, FCM).
- **Các bước chính**:
  1. Map module boundaries và dependency graph ở `settings.gradle`.
  2. Thiết lập Gradle config riêng cho từng module (dependency, proguard, packaging).
  3. Định nghĩa interface contract giữa module feature và core.
- **Deliverables**: module contract + dependency map.
- **Phụ thuộc**: blueprint kiến trúc tổng thể.

## 3. Data Layer
- **Mục tiêu**: Retrofit + Room + Paging 3 + RxJava chuẩn hoá cache và sync.
- **Các bước chính**:
  1. Đặc tả API interface, DTO, mapper, error handling.
  2. Thiết kế Room entity/DAO cho ReadingHistory, Comic cache, User profile.
  3. Xây repository kết hợp Remote + Local, hỗ trợ Paging, offline.
- **Deliverables**: data flow spec, schema diagram, API list.
- **Phụ thuộc**: module contract, yêu cầu tích hợp VNPay/FCM.

## 4. Navigation
- **Mục tiêu**: Navigation Component flow bao trùm Auth → Discover → ComicDetail → Reader → VIP, deeplink, guard.
- **Các bước chính**:
  1. Vẽ nav graph XML, xác định startDestination theo auth state.
  2. Định nghĩa deeplink từ Notification/Share và guard cho route VIP.
  3. Thiết kế transition và back stack cho Reader/Full-screen.
- **Deliverables**: flowchart + `nav_graph.xml` spec.
- **Phụ thuộc**: UI wireframe, requirement từ marketing/notification.

## 5. UI & Presentation
- **Mục tiêu**: Fragment/Activity + ViewModel + ViewBinding, Glide cho ảnh, state-driven UI.
- **Các bước chính**:
  1. Xác định contract `UiState`, `UiEvent` cho mỗi screen.
  2. Thiết kế layout XML, component reuse (ComicCard, Toolbar, BottomSheet, Reader controls).
  3. Chuẩn hoá Loading/Empty/Error states, skeleton + offline indicator.
- **Deliverables**: UI spec cho từng màn chính, component library.
- **Phụ thuộc**: navigation map, data contracts.

## 6. Dependency Injection
- **Mục tiêu**: Hilt modules cho network/database/repository/usecase.
- **Các bước chính**:
  1. Tạo `NetworkModule`, `DatabaseModule`, `RepositoryModule`, `UseCaseModule` với scope phù hợp.
  2. Định nghĩa qualifier cho API chính/phụ, logging/interceptor.
  3. Thiết lập AssistedInject nếu ViewModel cần runtime args (ComicId, ChapterId).
- **Deliverables**: DI blueprint + sample module code.
- **Phụ thuộc**: data layer ổn định, module dependency.

## 7. Testing Strategy
- **Mục tiêu**: Coverage ≥80% cho domain/data/ui critical flow.
- **Các bước chính**:
  1. Unit test UseCase với RxJava `TestScheduler`, repository với fake API/DB.
  2. Instrumented test cho Room migration + DAO.
  3. UI test Espresso cho navigation, Paging diff, Reader gestures; snapshot layout quan trọng.
- **Deliverables**: test matrix, testcase mẫu, CI config.
- **Phụ thuộc**: API stable, mock server.

## 8. Release & Ops
- **Mục tiêu**: Sẵn sàng Play Store, VNPay compliance, FCM keys, analytics/crash.
- **Các bước chính**:
  1. Thiết lập build variant (dev/stage/prod), signing config, ProGuard, app bundle.
  2. Tích hợp VNPay SDK flow + sandbox/prod config; đảm bảo logs + error handling.
  3. Cấu hình FCM topic, notification payload, analytics (Firebase Analytics), crash (Crashlytics).
- **Deliverables**: release checklist, environment matrix, integration test results.
- **Phụ thuộc**: QA sign-off, legal/compliance approvals.

