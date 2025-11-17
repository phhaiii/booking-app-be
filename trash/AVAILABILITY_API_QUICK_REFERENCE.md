# Availability API Quick Reference

## Endpoint
```
GET /api/bookings/availability
```

## Parameters
- `postId` (Long, required): The ID of the venue/post to check
- `date` (String, required): The date or datetime to check availability

## Supported Date Formats

### 1. Date Only (General Day Availability)
```
Format: yyyy-MM-dd
Example: 2025-11-20
```
Checks if the venue has ANY available slots on that day.

**Request:**
```
GET /api/bookings/availability?postId=8&date=2025-11-20
```

**Behavior:**
- Parses as `2025-11-20T00:00:00`
- Checks general availability for the entire day
- Returns `true` if any of the daily slots are available

### 2. DateTime (Specific Time Check)
```
Format: yyyy-MM-ddTHH:mm:ss
Example: 2025-11-20T10:00:00
```
Checks if the venue is available at that specific time.

**Request:**
```
GET /api/bookings/availability?postId=8&date=2025-11-20T10:00:00
```

**Behavior:**
- Validates the time is within working hours (10:00-18:00)
- Returns `false` if time is outside working hours
- Returns availability status for that specific time

## Response Format

### Success Response (Available)
```json
{
  "success": true,
  "message": "Venue has available slots",
  "data": true,
  "timestamp": "2025-11-17T18:09:21.187"
}
```

### Success Response (Not Available)
```json
{
  "success": true,
  "message": "Venue is fully booked",
  "data": false,
  "timestamp": "2025-11-17T18:09:21.187"
}
```

### Error Response (Invalid Format)
```json
{
  "success": false,
  "message": "Invalid date format. Please use yyyy-MM-dd or ISO datetime format (e.g., 2025-12-25 or 2025-12-25T10:00:00)",
  "data": null,
  "timestamp": "2025-11-17T18:09:21.187"
}
```

### Error Response (Past Date)
```json
{
  "success": true,
  "message": "Cannot check availability for past dates",
  "data": false,
  "timestamp": "2025-11-17T18:09:21.187"
}
```

## Working Hours
- Start: 10:00 AM
- End: 6:00 PM (18:00)

Requests for times outside this range will return `false`.

## Examples

### ✅ Valid Requests

```bash
# Check any day in November 2025
GET /api/bookings/availability?postId=8&date=2025-11-20

# Check specific time (10:00 AM)
GET /api/bookings/availability?postId=8&date=2025-11-20T10:00:00

# Check afternoon slot (2:00 PM)
GET /api/bookings/availability?postId=8&date=2025-11-20T14:00:00

# Check near closing (5:30 PM)
GET /api/bookings/availability?postId=8&date=2025-11-20T17:30:00
```

### ❌ Invalid Requests

```bash
# Wrong format
GET /api/bookings/availability?postId=8&date=11/20/2025
# Returns: Invalid date format error

# Before working hours
GET /api/bookings/availability?postId=8&date=2025-11-20T08:00:00
# Returns: false (outside working hours)

# After working hours
GET /api/bookings/availability?postId=8&date=2025-11-20T19:00:00
# Returns: false (outside working hours)

# Past date
GET /api/bookings/availability?postId=8&date=2025-11-10
# Returns: Cannot check availability for past dates (if today is after Nov 10)
```

## Business Logic

1. **Daily Slots**: Each venue has a configurable number of slots per day (default: 4)
2. **Slot Counting**: Only active bookings are counted (CANCELLED bookings are excluded)
3. **Availability**: Available if `bookedCount < totalSlots`
4. **Time Validation**: Only enforced for non-midnight times
5. **Midnight Special Case**: Time `00:00` is treated as "check entire day" request

## Related Endpoints

For more detailed slot information, use:
```
GET /api/bookings/slot-availability?postId=8&date=2025-11-20
```

This returns detailed information about:
- Total slots
- Available slots
- Booked slots
- Individual time slot statuses (MORNING, AFTERNOON)

