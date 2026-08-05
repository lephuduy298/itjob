# Dashboard Statistics API

## Endpoint
```
GET /api/v1/admin/dashboard/statistics
```

## Mô tả
Endpoint này trả về các thống kê tổng quan cho trang dashboard của admin.

## Response
```json
{
  "totalJobs": 150,
  "activeJobs": 120,
  "totalCompanies": 45,
  "totalUsers": 1250,
  "newCandidates": 23,
  "scheduleToday": 8,
  "messagesReceived": 56,
  "jobViews": 2342,
  "jobApplied": 654,
  "jobViewsTrend": 6.4,
  "jobAppliedTrend": 0.5
}
```

## Các trường dữ liệu

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| totalJobs | number | Tổng số công việc trong hệ thống |
| activeJobs | number | Số công việc đang active |
| totalCompanies | number | Tổng số công ty |
| totalUsers | number | Tổng số người dùng |
| newCandidates | number | Số ứng viên mới trong 7 ngày gần đây |
| scheduleToday | number | Số lượng CV được nộp hôm nay |
| messagesReceived | number | Số tin nhắn nhận được hôm nay |
| jobViews | number | Tổng số lượt xem công việc |
| jobApplied | number | Tổng số CV đã được nộp |
| jobViewsTrend | number | Xu hướng tăng/giảm lượt xem (%) |
| jobAppliedTrend | number | Xu hướng tăng/giảm số CV nộp (%) |

## Cách hoạt động

### Metrics tính toán:
1. **totalJobs**: Đếm tất cả jobs trong database
2. **activeJobs**: Đếm jobs có `active = true`
3. **totalCompanies**: Đếm tất cả companies
4. **totalUsers**: Đếm tất cả users
5. **newCandidates**: Đếm users được tạo trong 7 ngày gần đây
6. **scheduleToday**: Đếm resumes được tạo hôm nay
7. **messagesReceived**: Đếm chat messages được tạo hôm nay
8. **jobApplied**: Tổng số resumes trong hệ thống
9. **Trend metrics**: So sánh với tuần trước để tính phần trăm tăng/giảm

## Files liên quan

### Response DTO
- `DashboardStatisticsResponse.java` - Response model chứa các metrics

### Service Layer
- `DashboardService.java` - Business logic tính toán các metrics

### Controller
- `DashboardController.java` - REST endpoint

### Repositories đã cập nhật
- `JobRepository.java` - Thêm `countByActive(boolean active)`
- `UserRepository.java` - Thêm `countByCreatedAtAfter()`, `countByCreatedAtBetween()`
- `ResumeRepository.java` - Thêm `countByCreatedAtBetween()`
- `ChatMessageRepository.java` - Thêm `countByCreatedAtBetween()`

## Cải tiến trong tương lai

1. **Job Views Tracking**: 
   - Hiện tại `jobViews` đang dùng giá trị mặc định
   - Nên tạo bảng `job_views` để track lượt xem thực tế

2. **Real-time Trend Calculation**:
   - Cải thiện cách tính trend bằng cách so sánh với dữ liệu thực tế của các khoảng thời gian trước

3. **Caching**:
   - Có thể cache kết quả trong 5-10 phút để giảm tải database

4. **Permission**:
   - Thêm annotation security để chỉ admin mới truy cập được endpoint này

## Ví dụ sử dụng

```javascript
// React/Angular Frontend
const fetchDashboardStats = async () => {
  const response = await axios.get('/api/v1/admin/dashboard/statistics');
  console.log(response.data);
};
```

```bash
# cURL
curl -X GET http://localhost:8080/api/v1/admin/dashboard/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```
