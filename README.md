# Java Spring Boot RESTful API - JobHunter (Dự án cuối khóa)

Chào mừng bạn đến với **JobHunter** - Hệ thống RESTful API hoàn chỉnh dành cho nền tảng kết nối tuyển dụng giữa Ứng viên (Candidates) và Nhà tuyển dụng (Recruiters). Dự án được phát triển dựa trên kiến trúc Spring Boot hiện đại, tích hợp Trí tuệ nhân tạo (AI Chatbot), truyền thông thời gian thực (Real-time Socket.io), phân quyền động (RBAC) và hệ thống thanh toán tự động (Sepay QR).

---

## 🚀 Các Tính Năng Nổi Bật

### 1. Quản lý Người Dùng & Phân Quyền Động (RBAC)
- **Xác thực**: Sử dụng Spring Security, OAuth2 Resource Server và JWT (Json Web Token) với cơ chế Refresh Token và Access Token dài hạn.
- **Phân quyền**: Role-based Access Control (RBAC) chặt chẽ. Hệ thống quản lý động các Roles (Vai trò) và Permissions (Quyền hạn).
- **Security Interceptor**: Tự động kiểm tra quyền truy cập của người dùng đối với từng API endpoint dựa trên cấu hình phân quyền trong cơ sở dữ liệu.

### 2. Quản lý Tuyển Dụng & Nộp Hồ Sơ
- **Công ty (Companies)**: Quản lý thông tin công ty, logo, mô tả, địa chỉ.
- **Tin tuyển dụng (Jobs)**: CRUD tin tuyển dụng kèm các yêu cầu về kỹ năng (Skills), mức lương, số lượng, cấp bậc (Level), hình thức làm việc (Work Mode - Remote/Hybrid/Onsite).
- **Hồ sơ ứng tuyển (Resumes/CVs)**: Ứng viên tải lên CV (PDF, Docx...). Nhà tuyển dụng có quyền duyệt/từ chối hồ sơ (`APPROVED`, `REJECTED`, `REVIEWING`).
- **Gửi Email tự động**: Hệ thống gửi email tự động tới ứng viên khi trạng thái CV của họ thay đổi (ví dụ: được chấp nhận hoặc chuyển sang vòng phỏng vấn).

### 3. Tích Hợp AI Chatbot & Tìm Kiếm Thông Minh (Vector Database)
- **AI Chatbot**: Kết nối với Python AI Service (FastAPI/Flask) tại `localhost:8000`. Hỗ trợ tải lên CV định dạng PDF để AI tự động phân tích và đưa ra đánh giá, gợi ý công việc.
- **Tìm kiếm Vector**: Khi tin tuyển dụng được tạo hoặc cập nhật, Spring Boot tự động gửi dữ liệu sang Python Vector DB để phục vụ tính năng tìm kiếm ngữ nghĩa (Semantic Search) và gợi ý việc làm tối ưu.

### 4. Hệ Thống Thanh Toán & Đăng Ký Gói Thành Viên (Sepay QR Webhook)
- **Mô hình Subscription**: Quản lý các gói dịch vụ (Plans) và giới hạn quyền lợi (Entitlements - ví dụ: giới hạn số lượng bài đăng tuyển dụng).
- **Thanh toán tự động**: Tích hợp cổng thanh toán **Sepay**. Tự động tạo mã QR động ngân hàng (VietQR - MBBank,...) chứa mã nội dung chuyển khoản riêng biệt (`SUB_{transactionId}`).
- **Webhook xử lý thời gian thực**: Sepay gửi webhook ngay khi có giao dịch thành công. Hệ thống tự động kiểm tra chữ ký số bảo mật (Verify Signature), IP Whitelist và lập tức kích hoạt gói dịch vụ cho người dùng.
- **Scheduler**: Tự động quét và đóng các giao dịch hết hạn (Pending Timeout) sau thời gian cấu hình (ví dụ: 5 phút).

### 5. Chat & Thông Báo Thời Gian Thực (Real-time Netty Socket.io)
- **Chat trực tiếp**: Tích hợp thư viện Netty Socket.IO hiệu năng cao, cho phép ứng viên và nhà tuyển dụng chat trực tiếp thời gian thực.
- **Trạng thái Hoạt động (Presence)**: Theo dõi trạng thái Online/Offline của người dùng.
- **Thông báo tuyển dụng**: Gửi thông báo tức thì khi có việc làm mới phù hợp với kỹ năng của người dùng đăng ký.

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

- **Ngôn ngữ**: Java 17
- **Framework**: Spring Boot 3.2.4 (Spring Web, JPA, Security, Actuator, Mail, Thymeleaf)
- **Cơ sở dữ liệu**: MySQL
- **Thư viện ánh xạ**: MapStruct (Chuyển đổi Entity <=> DTO nhanh chóng)
- **Bộ lọc tìm kiếm nâng cao**: `com.turkraft.springfilter` (Hỗ trợ lọc, sắp xếp, phân trang động từ API)
- **Tài liệu hóa API**: Springdoc OpenAPI / Swagger UI 2.5.0
- **Real-time**: Netty Socket.IO 2.0.13
- **Tích hợp API ngoài**: Spring Cloud OpenFeign & RestTemplate

---