# Slot-Based Booking System Implementation

## Overview
The booking system has been updated to use 4 predefined time slots instead of flexible time ranges. This prevents overlapping bookings and provides a cleaner user experience.

## Time Slots
The system now uses 4 fixed time slots per day:

| Slot Index | Time Range | Display Text |
|------------|------------|--------------|
| 0 | 10:00 - 12:00 | 10:00 - 12:00 |
| 1 | 12:00 - 14:00 | 12:00 - 14:00 |
| 2 | 14:00 - 16:00 | 14:00 - 16:00 |
| 3 | 16:00 - 18:00 | 16:00 - 18:00 |

## Database Changes
1. **New column added to `bookings` table**: `slot_index` (INT)
2. **New index**: `idx_booking_slot (post_id, booking_date, slot_index)`

### Migration Script
Run the migration script: `database/migration_add_slot_index_to_bookings.sql`

## API Changes

### 1. Create Booking Request
**Endpoint**: `POST /api/bookings`

**✅ BACKWARD COMPATIBLE**: The API now supports both old and new request formats!

**Legacy Format (Still Supported)**:
```json
{
  "postId": 1,
  "customerName": "John Doe",
  "customerPhone": "0123456789",
  "bookingDate": "2025-12-25",
  "startTime": "10:00",
  "numberOfGuests": 50
}
```

**New Slot-Based Format**:
```json
{
  "postId": 1,
  "customerName": "John Doe", 
  "customerPhone": "0123456789",
  "bookingDate": "2025-12-25",
  "slotIndex": 0,
  "numberOfGuests": 50
}
```

**⚠️ Important**: Legacy `startTime` values are automatically mapped to slots:
- `"10:00"` or `"10"` → Slot 0 (10:00-12:00)
- `"12:00"` or `"12"` → Slot 1 (12:00-14:00)  
- `"14:00"` or `"14"` → Slot 2 (14:00-16:00)
- `"16:00"` or `"16"` → Slot 3 (16:00-18:00)

Any other start times will be rejected with an error message.

### 2. Get Available Time Slots
**New Endpoint**: `GET /api/bookings/time-slots`

**Parameters**:
- `postId`: The venue ID
- `date`: Date in yyyy-MM-dd format

**Example Request**:
```
GET /api/bookings/time-slots?postId=1&date=2025-12-25
```

**Response**:
```json
{
  "success": true,
  "message": "Time slots retrieved successfully",
  "data": [
    {
      "slotIndex": 0,
      "startTime": "10:00",
      "endTime": "12:00", 
      "displayText": "10:00 - 12:00",
      "isAvailable": true
    },
    {
      "slotIndex": 1,
      "startTime": "12:00",
      "endTime": "14:00",
      "displayText": "12:00 - 14:00", 
      "isAvailable": false
    },
    {
      "slotIndex": 2,
      "startTime": "14:00",
      "endTime": "16:00",
      "displayText": "14:00 - 16:00",
      "isAvailable": true
    },
    {
      "slotIndex": 3,
      "startTime": "16:00", 
      "endTime": "18:00",
      "displayText": "16:00 - 18:00",
      "isAvailable": true
    }
  ]
}
```

## Implementation Details

### TimeSlot Enum
- Located at: `com.myapp.booking.enums.TimeSlot`
- Provides constants for the 4 time slots
- Methods: `fromIndex()`, `fromStartTime()`, `containsTime()`, `getAllSlots()`

### Booking Validation
1. **Slot Index Validation**: Must be between 0 and 3
2. **Availability Check**: Verifies the slot is not already booked for that date
3. **Automatic Time Setting**: Start/end times are automatically set based on slot index

### Repository Changes
New methods in `BookingRepository`:
- `existsByPostIdAndBookingDateAndSlotIndex()`: Check if slot is taken
- `findByPostIdAndBookingDateAndSlotIndex()`: Find bookings in specific slot

## Benefits
1. **No More Overlapping**: Fixed time slots eliminate booking conflicts
2. **Simplified Frontend**: Users select from predefined options instead of entering times
3. **Better Performance**: Index on `(post_id, booking_date, slot_index)` for fast queries
4. **Consistent Experience**: All bookings follow the same 2-hour slot pattern

## Migration Guide for Frontend

### Before (Flexible Time Input):
```javascript
// User enters custom start/end times
const bookingData = {
  postId: 1,
  startTime: "10:30",
  endTime: "12:30",
  // ...other fields
};
```

### After (Slot Selection):
```javascript
// 1. Get available slots
const slots = await fetch('/api/bookings/time-slots?postId=1&date=2025-12-25');

// 2. User selects from available slots  
const bookingData = {
  postId: 1,
  slotIndex: 0, // 10:00-12:00 slot
  // ...other fields
};
```

## Error Handling
- **Invalid Slot Index**: Returns 400 with message about valid slot ranges (0-3)
- **Slot Already Booked**: Returns 400 with specific slot time that's unavailable
- **Past Date**: Returns 400 for past dates in time slots endpoint

This implementation ensures a more reliable and user-friendly booking experience while preventing the overlapping booking issues that were occurring with flexible time inputs.
