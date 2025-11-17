# API Quick Reference - Slot Booking System

## 🎯 Endpoints mới

### 1. GET /api/bookings/slot-availability
**Mục đích**: Lấy thông tin chi tiết về slot availability cho một venue vào ngày cụ thể

**Parameters**:
- `postId` (required): ID của venue/post
- `date` (required): Ngày cần check (format: `yyyy-MM-dd`)

**Example Request**:
```javascript
fetch('/api/bookings/slot-availability?postId=1&date=2025-12-25', {
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN'
  }
})
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "postId": 1,
    "postTitle": "Luxury Wedding Venue",
    "bookingDate": "2025-12-25",
    "totalSlots": 2,
    "availableSlots": 1,
    "bookedSlots": 1,
    "timeSlots": [
      {
        "slotId": "MORNING",
        "startTime": "10:00:00",
        "endTime": "14:00:00",
        "isAvailable": true,
        "status": "AVAILABLE"
      },
      {
        "slotId": "AFTERNOON",
        "startTime": "14:00:00",
        "endTime": "18:00:00",
        "isAvailable": false,
        "status": "BOOKED"
      }
    ]
  },
  "message": "Slot availability retrieved successfully"
}
```

---

### 2. POST /api/bookings (UPDATED)
**Thay đổi**: Thêm validation cho slots

**New Validations**:
- ✅ Check số lượng slot còn trống
- ✅ Check thời gian trong khung 10:00-18:00
- ✅ Check không trùng với booking khác

**Example Request**:
```javascript
fetch('/api/bookings', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_TOKEN'
  },
  body: JSON.stringify({
    "postId": 1,
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0123456789",
    "customerEmail": "email@example.com",
    "bookingDate": "2025-12-25",
    "startTime": "10:00",
    "endTime": "14:00",
    "numberOfGuests": 100
  })
})
```

**Possible Error Responses**:
```json
// Slot đã full
{
  "success": false,
  "message": "No available slots for this date. All 2 slots are booked."
}

// Ngoài giờ làm việc
{
  "success": false,
  "message": "Booking time must be within working hours (10:00-18:00)"
}

// Trùng giờ
{
  "success": false,
  "message": "This time slot overlaps with an existing booking. Please choose a different time."
}
```

---

## 💡 Frontend Integration Examples

### React Example - Check & Display Slots

```jsx
import { useState, useEffect } from 'react';

function BookingCalendar({ postId }) {
  const [selectedDate, setSelectedDate] = useState('2025-12-25');
  const [slotInfo, setSlotInfo] = useState(null);
  const [loading, setLoading] = useState(false);

  // Load slot availability khi chọn ngày
  useEffect(() => {
    if (selectedDate && postId) {
      checkSlotAvailability();
    }
  }, [selectedDate, postId]);

  const checkSlotAvailability = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `/api/bookings/slot-availability?postId=${postId}&date=${selectedDate}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      const result = await response.json();
      
      if (result.success) {
        setSlotInfo(result.data);
      }
    } catch (error) {
      console.error('Error checking slots:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleBooking = async (timeSlot) => {
    if (!timeSlot.isAvailable) {
      alert('Slot này đã được đặt!');
      return;
    }

    const bookingData = {
      postId: postId,
      customerName: "Nguyễn Văn A",
      customerPhone: "0123456789",
      bookingDate: selectedDate,
      startTime: timeSlot.startTime,
      endTime: timeSlot.endTime,
      numberOfGuests: 100
    };

    try {
      const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(bookingData)
      });

      const result = await response.json();

      if (result.success) {
        alert('Đặt chỗ thành công!');
        checkSlotAvailability(); // Refresh slots
      } else {
        alert(result.message);
      }
    } catch (error) {
      console.error('Booking error:', error);
      alert('Có lỗi xảy ra khi đặt chỗ!');
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="booking-calendar">
      <h3>{slotInfo?.postTitle}</h3>
      
      <input 
        type="date" 
        value={selectedDate}
        onChange={(e) => setSelectedDate(e.target.value)}
        min={new Date().toISOString().split('T')[0]}
      />

      {slotInfo && (
        <div className="slot-info">
          <p>
            Còn trống: <strong>{slotInfo.availableSlots}</strong> / {slotInfo.totalSlots} slots
          </p>

          <div className="time-slots">
            {slotInfo.timeSlots.map((slot) => (
              <div 
                key={slot.slotId}
                className={`slot ${slot.isAvailable ? 'available' : 'booked'}`}
              >
                <h4>{slot.slotId}</h4>
                <p>{slot.startTime} - {slot.endTime}</p>
                <button 
                  onClick={() => handleBooking(slot)}
                  disabled={!slot.isAvailable}
                >
                  {slot.isAvailable ? 'Đặt chỗ' : 'Đã đặt'}
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
```

### Vue.js Example

```vue
<template>
  <div class="booking-calendar">
    <h3>{{ slotInfo?.postTitle }}</h3>
    
    <input 
      type="date" 
      v-model="selectedDate"
      :min="today"
    />

    <div v-if="slotInfo" class="slot-info">
      <p>
        Còn trống: <strong>{{ slotInfo.availableSlots }}</strong> / {{ slotInfo.totalSlots }} slots
      </p>

      <div class="time-slots">
        <div 
          v-for="slot in slotInfo.timeSlots" 
          :key="slot.slotId"
          :class="['slot', slot.isAvailable ? 'available' : 'booked']"
        >
          <h4>{{ slot.slotId }}</h4>
          <p>{{ slot.startTime }} - {{ slot.endTime }}</p>
          <button 
            @click="handleBooking(slot)"
            :disabled="!slot.isAvailable"
          >
            {{ slot.isAvailable ? 'Đặt chỗ' : 'Đã đặt' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, computed } from 'vue';

export default {
  props: ['postId'],
  setup(props) {
    const selectedDate = ref(new Date().toISOString().split('T')[0]);
    const slotInfo = ref(null);
    const loading = ref(false);
    
    const today = computed(() => new Date().toISOString().split('T')[0]);

    const checkSlotAvailability = async () => {
      loading.value = true;
      try {
        const response = await fetch(
          `/api/bookings/slot-availability?postId=${props.postId}&date=${selectedDate.value}`,
          {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );
        const result = await response.json();
        
        if (result.success) {
          slotInfo.value = result.data;
        }
      } catch (error) {
        console.error('Error:', error);
      } finally {
        loading.value = false;
      }
    };

    const handleBooking = async (timeSlot) => {
      if (!timeSlot.isAvailable) {
        alert('Slot này đã được đặt!');
        return;
      }

      const bookingData = {
        postId: props.postId,
        customerName: "Nguyễn Văn A",
        customerPhone: "0123456789",
        bookingDate: selectedDate.value,
        startTime: timeSlot.startTime,
        endTime: timeSlot.endTime,
        numberOfGuests: 100
      };

      try {
        const response = await fetch('/api/bookings', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(bookingData)
        });

        const result = await response.json();

        if (result.success) {
          alert('Đặt chỗ thành công!');
          checkSlotAvailability();
        } else {
          alert(result.message);
        }
      } catch (error) {
        console.error('Error:', error);
        alert('Có lỗi xảy ra!');
      }
    };

    watch(() => selectedDate.value, checkSlotAvailability);
    watch(() => props.postId, checkSlotAvailability);

    return {
      selectedDate,
      slotInfo,
      loading,
      today,
      handleBooking
    };
  }
}
</script>
```

---

## 🎨 UI/UX Recommendations

### 1. Hiển thị slot availability
```
📅 Ngày: 25/12/2025
🏛️ Venue: Luxury Wedding Venue

Tình trạng: ⭕ Còn 3/4 slots

⏰ Khung giờ sáng (10:00 - 14:00)    [✅ Còn trống]  [Đặt ngay]
⏰ Khung giờ chiều (14:00 - 18:00)   [❌ Đã đặt]     [Disabled]
```

### 2. Color coding
- 🟢 Còn trống: Green (#4CAF50)
- 🔴 Đã đặt: Red (#F44336)
- 🟡 Đang chọn: Yellow (#FFC107)

### 3. Validation messages
- Show warning nếu chọn ngoài giờ 10:00-18:00
- Show error nếu slot đã full
- Show success khi đặt thành công

---

## 🐛 Common Issues & Solutions

### Issue 1: "Cannot resolve table/column" warnings
**Solution**: Warnings này chỉ từ IDE, không ảnh hưởng runtime. Run migration để update database.

### Issue 2: 401 Unauthorized
**Solution**: Kiểm tra token trong localStorage còn valid không.

### Issue 3: Booking bị reject
**Solution**: 
- Check slot availability trước
- Validate time trong range 10:00-18:00
- Đảm bảo không overlap với booking khác

---

## 📚 Related Documentation
- Full documentation: `SLOT_BOOKING_SYSTEM.md`
- Implementation details: `SLOT_BOOKING_IMPLEMENTATION_SUMMARY.md`
- Database migration: `database/migration_add_available_slots.sql`

