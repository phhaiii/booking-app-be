# Slot-Based Booking System Implementation Guide

## Overview
This document describes the implementation of a slot-based booking system for wedding venues with time slot management (10:00-18:00) and configurable slot capacity.

## Features Implemented

### 1. Slot Capacity Management
- **Field**: `available_slots` in Post model
- **Default**: 2 slots per day per venue
- **Configurable**: Vendors can set custom slot capacity for each post/venue

### 2. Time Slot Validation
- **Working Hours**: 10:00 AM - 6:00 PM (10h-18h)
- **Slot Division**: 
  - Morning Slot: 10:00 - 14:00
  - Afternoon Slot: 14:00 - 18:00
- **Validation**: Bookings outside working hours are rejected

### 3. Availability Checking
The system now properly validates:
- ✅ Total slot capacity per venue
- ✅ Number of active bookings on a specific date
- ✅ Time slot overlaps
- ✅ Working hours compliance

## API Endpoints

### Check Basic Availability
```
GET /api/bookings/availability
Parameters:
  - venueId: Long (Post ID)
  - requestedDate: String (ISO DateTime format)

Response:
{
  "success": true,
  "data": true/false,
  "message": "Venue is available" | "Venue is not available"
}
```

### Get Detailed Slot Availability
```
GET /api/bookings/slot-availability
Parameters:
  - postId: Long
  - date: String (yyyy-MM-dd format)

Response:
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

### Create Booking with Slot Validation
```
POST /api/bookings
Body:
{
  "postId": 1,
  "customerName": "John Doe",
  "customerPhone": "0123456789",
  "customerEmail": "john@example.com",
  "bookingDate": "2025-12-25",
  "startTime": "10:00",
  "endTime": "14:00",
  "numberOfGuests": 100
}
```

## Validation Rules

### When Creating a Booking:

1. **Slot Capacity Check**
   - System counts active bookings (excluding CANCELLED status)
   - If booked count >= total slots → Reject with error

2. **Working Hours Check**
   - Start time must be >= 10:00
   - End time must be <= 18:00
   - Otherwise → Reject with error

3. **Time Slot Overlap Check**
   - System checks if requested time overlaps with existing bookings
   - If overlap detected → Reject with error

4. **Status Exclusion**
   - Only counts bookings with status: PENDING, CONFIRMED, COMPLETED
   - CANCELLED bookings are ignored in availability calculations

## Database Changes

### New Column in `posts` table:
```sql
ALTER TABLE posts 
ADD COLUMN available_slots INT DEFAULT 2 
COMMENT 'Number of booking slots available per day';
```

### Migration Applied:
See `database/migration_add_available_slots.sql`

## Code Changes Summary

### 1. Model Changes
**File**: `Post.java`
```java
@Column(name = "available_slots")
@Builder.Default
private Integer availableSlots = 2; // 2 slots per day (default)
```

### 2. Repository Changes
**File**: `BookingRepository.java`
- Added `countByPostIdAndBookingDateAndStatusNotIn()` - Count bookings excluding certain statuses
- Added `findByPostIdAndBookingDateAndStatusNotIn()` - Get bookings for slot analysis

### 3. Service Changes
**File**: `BookingService.java`
- Updated `createBooking()` - Added slot validation logic
- Updated `isTimeSlotAvailable()` - Proper slot-based availability check
- Added `getSlotAvailability()` - Detailed slot information retrieval
- Added `timeSlotsOverlap()` - Helper method for time range overlap detection

### 4. Controller Changes
**File**: `BookingController.java`
- Added `/api/bookings/slot-availability` endpoint

### 5. New DTO
**File**: `SlotAvailabilityResponse.java`
- Response model for detailed slot information

## Usage Examples

### Frontend Integration

```javascript
// Check if a venue has available slots on a specific date
async function checkSlotAvailability(postId, date) {
  const response = await fetch(
    `/api/bookings/slot-availability?postId=${postId}&date=${date}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  const result = await response.json();
  
  if (result.success) {
    const { availableSlots, timeSlots } = result.data;
    
    // Show available time slots to user
    timeSlots.forEach(slot => {
      if (slot.isAvailable) {
        console.log(`${slot.slotId}: ${slot.startTime} - ${slot.endTime}`);
      }
    });
  }
}

// Create a booking with validation
async function createBooking(bookingData) {
  try {
    const response = await fetch('/api/bookings', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(bookingData)
    });
    
    const result = await response.json();
    
    if (!result.success) {
      // Handle validation errors
      alert(result.message);
    }
  } catch (error) {
    console.error('Booking failed:', error);
  }
}
```

## Error Messages

The system provides clear error messages:

- `"No available slots for this date. All X slots are booked."`
- `"Booking time must be within working hours (10:00-18:00)"`
- `"This time slot overlaps with an existing booking. Please choose a different time."`

## Testing Scenarios

### Scenario 1: Full Capacity
- Venue has 4 slots
- 4 bookings already exist for the date
- New booking attempt → **Rejected**

### Scenario 2: Partial Capacity
- Venue has 4 slots
- 1 booking exists (10:00-14:00)
- New booking for 14:00-18:00 → **Accepted**
- New booking for 10:00-14:00 → **Rejected (overlap)**

### Scenario 3: Outside Working Hours
- Booking attempt for 08:00-12:00 → **Rejected**
- Booking attempt for 16:00-20:00 → **Rejected**

## Configuration

### Changing Default Slots
To change the default number of slots for all venues, update:
1. `Post.java` - Change `@Builder.Default` value
2. `database.sql` - Change `DEFAULT 2` value

### Custom Slots Per Venue
Vendors can set custom slot capacity when creating/editing a post through the admin panel.

## Future Enhancements

Potential improvements:
- [ ] Dynamic time slot configuration per venue
- [ ] Different slot capacities for weekdays vs weekends
- [ ] Seasonal slot adjustments
- [ ] Real-time slot reservation with timeout
- [ ] Waiting list for fully booked dates

## Support

For questions or issues, please contact the development team or refer to the main project documentation.

