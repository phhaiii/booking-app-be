# Slot-Based Booking System - Implementation Summary

## 🎯 Mục đích
Cập nhật hệ thống booking để hỗ trợ logic quản lý slot theo khung giờ 10h-18h với 2 slot mỗi bài viết (có thể cấu hình).

## ✅ Các thay đổi đã thực hiện

### 1. **Model Changes (Post.java)**
- ✅ Thêm field `availableSlots` để quản lý số slot có sẵn
- Mặc định: 2 slots/ngày, có thể tùy chỉnh cho từng venue

```java
@Column(name = "available_slots")
@Builder.Default
private Integer availableSlots = 2; // 2 slots per day (default)
```

### 2. **Repository Changes (BookingRepository.java)**
- ✅ Thêm method `countByPostIdAndBookingDateAndStatusNotIn()` - Đếm booking loại trừ status CANCELLED
- ✅ Thêm method `findByPostIdAndBookingDateAndStatusNotIn()` - Lấy danh sách booking cho phân tích slot
- ✅ Xóa import không sử dụng

### 3. **Service Changes (BookingService.java)**
#### Method: `createBooking()`
- ✅ **Kiểm tra slot availability**: Đếm số booking đã có, so sánh với tổng số slot
- ✅ **Validate working hours**: Chỉ chấp nhận booking trong khung 10:00-18:00
- ✅ **Check time overlap**: Không cho phép booking trùng giờ
- ✅ Error messages rõ ràng cho từng trường hợp

#### Method: `isTimeSlotAvailable()`
- ✅ Cập nhật logic kiểm tra slot dựa trên:
  - Số slot tổng của venue
  - Số booking đã có (loại trừ CANCELLED)
  - Thời gian trong working hours

#### Method: `getSlotAvailability()` (NEW)
- ✅ Trả về thông tin chi tiết về slot availability:
  - Total slots
  - Available slots
  - Booked slots
  - Danh sách time slots với trạng thái (MORNING: 10-14h, AFTERNOON: 14-18h)

#### Method: `timeSlotsOverlap()` (NEW)
- ✅ Helper method kiểm tra 2 khoảng thời gian có trùng nhau không

### 4. **Controller Changes (BookingController.java)**
- ✅ Thêm endpoint `/api/bookings/slot-availability` để lấy thông tin chi tiết về slot

### 5. **DTO Changes**
- ✅ Tạo `SlotAvailabilityResponse.java` - Response model cho slot information
  - Bao gồm nested class `TimeSlot` để hiển thị từng time slot

### 6. **Database Changes**
#### File: `database/database.sql`
- ✅ Thêm column `available_slots INT DEFAULT 2` vào bảng `posts`

#### File: `database/migration_add_available_slots.sql` (NEW)
- ✅ Migration script để thêm column vào database hiện tại
- ✅ Update tất cả posts hiện có với giá trị mặc định

### 7. **Documentation**
- ✅ Tạo `SLOT_BOOKING_SYSTEM.md` - Tài liệu hướng dẫn chi tiết về hệ thống
- ✅ Bao gồm: API endpoints, validation rules, usage examples, testing scenarios

## 📊 Logic hoạt động

### Khi tạo booking mới:
```
1. Kiểm tra Post có tồn tại và active không
2. Validate booking date
3. Parse và validate start/end time
4. ✅ KIỂM TRA SLOT CAPACITY:
   - Đếm số booking active trên ngày đó
   - Nếu >= totalSlots → REJECT
5. ✅ KIỂM TRA WORKING HOURS:
   - Start time >= 10:00
   - End time <= 18:00
   - Nếu không → REJECT
6. ✅ KIỂM TRA OVERLAP:
   - Lấy tất cả booking trên ngày đó
   - Check xem có trùng giờ không
   - Nếu có → REJECT
7. Tính toán giá, tạo booking
8. Increment booking count
```

### Khi check availability:
```
1. Lấy thông tin Post
2. Đếm số booking active (loại trừ CANCELLED)
3. So sánh: bookedCount < totalSlots
4. Return true/false
```

### Khi lấy slot availability chi tiết:
```
1. Lấy thông tin Post
2. Đếm booking đã có
3. Tính available slots
4. Phân tích từng time slot (MORNING, AFTERNOON)
5. Return chi tiết về tất cả slots
```

## 🔧 Cấu hình

### Thay đổi số slot mặc định:
1. **Code**: Sửa trong `Post.java` - `@Builder.Default private Integer availableSlots = X;`
2. **Database**: Sửa trong `database.sql` - `DEFAULT X`

### Cấu hình slot cho từng venue:
- Admin/Vendor có thể set `availableSlots` khi tạo/chỉnh sửa post

## 📝 API Usage Examples

### 1. Check slot availability
```bash
GET /api/bookings/slot-availability?postId=1&date=2025-12-25
Authorization: Bearer {token}
```

### 2. Create booking
```bash
POST /api/bookings
Content-Type: application/json
Authorization: Bearer {token}

{
  "postId": 1,
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0123456789",
  "bookingDate": "2025-12-25",
  "startTime": "10:00",
  "endTime": "14:00",
  "numberOfGuests": 100
}
```

## ⚠️ Error Messages

Các lỗi có thể gặp:
- `"No available slots for this date. All X slots are booked."`
- `"Booking time must be within working hours (10:00-18:00)"`
- `"This time slot overlaps with an existing booking. Please choose a different time."`

## 🧪 Testing Scenarios

### Scenario 1: Venue có 4 slots
- Booking 1: 10:00-14:00 (CONFIRMED) ✅
- Booking 2: 14:00-18:00 (PENDING) ✅
- Booking 3: 10:00-18:00 → **REJECTED** (exceed capacity)

### Scenario 2: Time overlap
- Booking 1: 10:00-14:00 (CONFIRMED) ✅
- Booking 2: 12:00-16:00 → **REJECTED** (overlap with Booking 1)

### Scenario 3: Outside working hours
- Booking: 08:00-12:00 → **REJECTED**
- Booking: 16:00-20:00 → **REJECTED**

## 📦 Files Created/Modified

### Created:
1. `src/main/java/com/myapp/booking/dtos/responses/SlotAvailabilityResponse.java`
2. `database/migration_add_available_slots.sql`
3. `SLOT_BOOKING_SYSTEM.md`
4. `SLOT_BOOKING_IMPLEMENTATION_SUMMARY.md` (this file)

### Modified:
1. `src/main/java/com/myapp/booking/models/Post.java`
2. `src/main/java/com/myapp/booking/services/BookingService.java`
3. `src/main/java/com/myapp/booking/repositories/BookingRepository.java`
4. `src/main/java/com/myapp/booking/controllers/BookingController.java`
5. `database/database.sql`

## 🚀 Deployment Steps

1. **Database Migration**:
   ```sql
   -- Run migration script
   source database/migration_add_available_slots.sql;
   ```

2. **Build & Deploy**:
   ```bash
   mvn clean package -DskipTests
   # Deploy the new JAR file
   ```

3. **Verify**:
   - Test slot availability endpoint
   - Create test bookings
   - Verify validation works correctly

## 📞 Support

Nếu có vấn đề, kiểm tra:
1. Database có column `available_slots` chưa
2. Logs của service khi tạo booking
3. Response từ API endpoints

---

**Tác giả**: GitHub Copilot  
**Ngày**: 2025-11-17  
**Version**: 1.0

