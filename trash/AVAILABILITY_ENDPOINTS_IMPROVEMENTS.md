# Availability Endpoints Improvements

**Date**: November 17, 2025  
**Updated**: BookingController.java - Availability endpoints

## ✅ Improvements Made

### 1. **Unified Parameter Names**
**Before**:
- `/availability` used `venueId` and `requestedDate`
- `/slot-availability` used `postId` and `date`

**After**:
- Both endpoints now use `postId` and `date` for consistency

### 2. **Enhanced Error Handling**
Added comprehensive try-catch blocks for:
- ✅ **DateTimeParseException**: Invalid date format
- ✅ **IllegalArgumentException**: Invalid date values
- ✅ **Generic Exception**: Unexpected errors

**Error Response Examples**:
```json
{
  "success": false,
  "message": "Invalid date format. Please use yyyy-MM-dd format (e.g., 2025-12-25)",
  "timestamp": "2025-11-17T10:30:00"
}
```

### 3. **Date Validation**
Added validation to prevent checking availability for past dates:

```java
if (requestedDate.isBefore(LocalDate.now())) {
    return ResponseEntity.ok(ApiResponse.success(false, 
        "Cannot check availability for past dates"));
}
```

### 4. **Improved Javadoc Documentation**
Both endpoints now have detailed Javadoc comments including:
- Parameter descriptions
- Return value descriptions
- Usage examples
- Response structure documentation

### 5. **Better Response Messages**
- **Available**: "Venue has available slots" (instead of "Venue is available")
- **Not Available**: "Venue is fully booked" (instead of "Venue is not available")
- More descriptive error messages

## 📋 API Documentation

### Endpoint 1: Basic Availability Check

```http
GET /api/bookings/availability?postId={postId}&date={date}
Authorization: Bearer {token}
```

**Parameters**:
- `postId` (required): Long - ID of the venue/post
- `date` (required): String - Date in yyyy-MM-dd format

**How it works**:
- The endpoint converts the date to midnight (00:00) for whole-day availability checking
- Checks if there are any available slots (out of 4 total) on that date
- Does NOT validate specific time slots - this is for general day availability

**Success Response**:
```json
{
  "success": true,
  "data": true,
  "message": "Venue has available slots",
  "timestamp": "2025-11-17T10:30:00"
}
```

**Error Responses**:

1. Invalid date format:
```json
{
  "success": false,
  "message": "Invalid date format. Please use yyyy-MM-dd format (e.g., 2025-12-25)",
  "timestamp": "2025-11-17T10:30:00"
}
```

2. Past date:
```json
{
  "success": true,
  "data": false,
  "message": "Cannot check availability for past dates",
  "timestamp": "2025-11-17T10:30:00"
}
```

---

### Endpoint 2: Detailed Slot Availability

```http
GET /api/bookings/slot-availability?postId={postId}&date={date}
Authorization: Bearer {token}
```

**Parameters**:
- `postId` (required): Long - ID of the venue/post
- `date` (required): String - Date in yyyy-MM-dd format

**Success Response**:
```json
{
  "success": true,
  "data": {
    "postId": 1,
    "postTitle": "Luxury Wedding Venue",
    "bookingDate": "2025-12-25",
    "totalSlots": 4,
    "availableSlots": 3,
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
        "isAvailable": true,
        "status": "AVAILABLE"
      }
    ]
  },
  "message": "Slot availability retrieved successfully",
  "timestamp": "2025-11-17T10:30:00"
}
```

**Error Responses**:

1. Invalid date format:
```json
{
  "success": false,
  "message": "Invalid date format. Please use yyyy-MM-dd format (e.g., 2025-12-25)",
  "timestamp": "2025-11-17T10:30:00"
}
```

2. Past date:
```json
{
  "success": false,
  "message": "Cannot check availability for past dates",
  "timestamp": "2025-11-17T10:30:00"
}
```

3. Invalid date value:
```json
{
  "success": false,
  "message": "Invalid date value: ...",
  "timestamp": "2025-11-17T10:30:00"
}
```

## 🔄 Before vs After Comparison

| Aspect | Before | After |
|--------|--------|-------|
| **Parameter Names** | venueId/postId mixed | postId (consistent) |
| **Date Parameter** | requestedDate/date mixed | date (consistent) |
| **Error Handling** | None (throws exception) | Comprehensive try-catch |
| **Date Validation** | None | Prevents past dates |
| **Error Messages** | Generic/unclear | Specific and helpful |
| **Documentation** | Minimal | Detailed Javadoc |
| **HTTP Status Codes** | Always 200 | 200/400/500 appropriate |

## 🧪 Testing Examples

### 1. Valid Request
```bash
curl -X GET "http://localhost:8080/api/bookings/availability?postId=1&date=2025-12-25" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. Invalid Date Format
```bash
curl -X GET "http://localhost:8080/api/bookings/availability?postId=1&date=25-12-2025" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: 400 Bad Request
# "Invalid date format. Please use yyyy-MM-dd format"
```

### 3. Past Date
```bash
curl -X GET "http://localhost:8080/api/bookings/availability?postId=1&date=2024-01-01" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: 200 OK
# data: false, message: "Cannot check availability for past dates"
```

### 4. Detailed Slot Info
```bash
curl -X GET "http://localhost:8080/api/bookings/slot-availability?postId=1&date=2025-12-25" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: Detailed slot information with time slots
```

## 📱 Frontend Integration

### React/JavaScript Example

```javascript
// Check basic availability
async function checkAvailability(postId, date) {
  try {
    const response = await fetch(
      `/api/bookings/availability?postId=${postId}&date=${date}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    
    const result = await response.json();
    
    if (!response.ok) {
      // Handle error (400, 500)
      console.error(result.message);
      showError(result.message);
      return;
    }
    
    if (result.success) {
      if (result.data) {
        console.log('Venue is available!');
      } else {
        console.log('Venue is not available:', result.message);
      }
    }
  } catch (error) {
    console.error('Network error:', error);
  }
}

// Get detailed slot information
async function getSlotDetails(postId, date) {
  try {
    const response = await fetch(
      `/api/bookings/slot-availability?postId=${postId}&date=${date}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    
    const result = await response.json();
    
    if (!response.ok) {
      showError(result.message);
      return null;
    }
    
    if (result.success) {
      const { totalSlots, availableSlots, timeSlots } = result.data;
      
      console.log(`Available: ${availableSlots}/${totalSlots} slots`);
      
      // Show available time slots
      timeSlots.forEach(slot => {
        console.log(`${slot.slotId}: ${slot.status}`);
      });
      
      return result.data;
    }
  } catch (error) {
    console.error('Error:', error);
    return null;
  }
}

// Usage with date validation
function checkVenueAvailability() {
  const postId = document.getElementById('postId').value;
  const dateInput = document.getElementById('bookingDate').value;
  
  // Validate date format on frontend
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
  if (!dateRegex.test(dateInput)) {
    showError('Please use yyyy-MM-dd format');
    return;
  }
  
  // Validate not in past
  const selectedDate = new Date(dateInput);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  if (selectedDate < today) {
    showError('Cannot select past dates');
    return;
  }
  
  // Proceed with API call
  checkAvailability(postId, dateInput);
}
```

## 🎯 Benefits

1. **Better User Experience**: Clear error messages guide users to fix issues
2. **Consistent API**: Same parameter names across related endpoints
3. **Robust Error Handling**: Prevents crashes from invalid inputs
4. **Date Validation**: Prevents booking in the past
5. **Developer Friendly**: Detailed documentation and examples
6. **Production Ready**: Proper HTTP status codes for different scenarios

## 🚀 Migration Guide

### Frontend Changes Needed

If your frontend was using the old `/availability` endpoint with `venueId`:

**Before**:
```javascript
fetch(`/api/bookings/availability?venueId=${id}&requestedDate=${date}`)
```

**After**:
```javascript
fetch(`/api/bookings/availability?postId=${id}&date=${date}`)
```

### Error Handling Updates

Add error handling for new response formats:

```javascript
const response = await fetch(url);
const result = await response.json();

// Check HTTP status
if (!response.ok) {
  // Handle 400 or 500 errors
  handleError(result.message);
  return;
}

// Check success flag
if (!result.success) {
  handleError(result.message);
  return;
}

// Use data
const isAvailable = result.data;
```

## ✅ Checklist

- [x] Unified parameter names (postId, date)
- [x] Added error handling for date parsing
- [x] Added validation for past dates
- [x] Improved Javadoc documentation
- [x] Better response messages
- [x] Proper HTTP status codes
- [x] Added imports (LocalDate, DateTimeParseException)
- [x] Tested compilation (no errors)
- [x] Created documentation

---

**Status**: ✅ Complete and ready for use  
**Breaking Changes**: Parameter names changed from `venueId` to `postId` and `requestedDate` to `date`  
**Backward Compatibility**: None - frontend needs to update parameter names

