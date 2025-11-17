# Availability Check Date/DateTime Parsing Fix

## Problem
The availability check API had two issues:

1. **Midnight Time Warning**: When checking availability for a date (e.g., `2025-11-20`), the system was incorrectly warning that the time `00:00` is outside working hours (10:00-18:00), even though midnight should indicate a general availability check for the entire day.

2. **DateTime Format Rejection**: When the client sent a datetime string like `2025-11-20T10:00:00`, the API threw a parsing error because it only accepted date format `yyyy-MM-dd`.

### Error Logs
```
# First issue (midnight warning):
2025-11-17T18:01:03.186+07:00  WARN 16128 --- [booking-app] [io-8089-exec-10] c.myapp.booking.services.BookingService  : Requested time 00:00 is outside working hours (10:00-18:00)

# Second issue (datetime format):
2025-11-17T18:09:21.028+07:00 ERROR 22888 --- [booking-app] [nio-8089-exec-2] c.m.b.controllers.BookingController      : Invalid date format: 2025-11-20T10:00:00
java.time.format.DateTimeParseException: Text '2025-11-20T10:00:00' could not be parsed, unparsed text found at index 10
```

## Root Causes

### Issue 1: Midnight Detection
The `isTimeSlotAvailable` method in `BookingService.java` was using `LocalTime.MIDNIGHT` constant for comparison:
```java
if (!requestedTime.equals(LocalTime.MIDNIGHT)) {
    // validate working hours
}
```
This comparison was not working correctly, causing the validation logic to execute even when the time was midnight.

### Issue 2: Date Format Inflexibility
The `checkAvailability` controller endpoint only accepted date format:
```java
LocalDate requestedDate = LocalDate.parse(date); // Only accepts yyyy-MM-dd
```
This failed when clients sent ISO datetime format like `2025-11-20T10:00:00`.

## Solutions

### Fix 1: Midnight Detection (BookingService.java)
Changed from using `LocalTime.MIDNIGHT` constant comparison to explicit hour/minute value checking:

#### Before
```java
if (!requestedTime.equals(LocalTime.MIDNIGHT)) {
    // Validate time is within 10:00-18:00
    LocalTime startWorkingHour = LocalTime.of(10, 0);
    LocalTime endWorkingHour = LocalTime.of(18, 0);

    if (requestedTime.isBefore(startWorkingHour) || requestedTime.isAfter(endWorkingHour)) {
        log.warn("Requested time {} is outside working hours (10:00-18:00)", requestedTime);
        return false;
    }
}
```

#### After
```java
if (requestedTime.getHour() != 0 || requestedTime.getMinute() != 0) {
    // Validate time is within 10:00-18:00
    LocalTime startWorkingHour = LocalTime.of(10, 0);
    LocalTime endWorkingHour = LocalTime.of(18, 0);

    if (requestedTime.isBefore(startWorkingHour) || requestedTime.isAfter(endWorkingHour)) {
        log.warn("Requested time {} is outside working hours (10:00-18:00)", requestedTime);
        return false;
    }
} else {
    log.debug("Checking general availability for the whole day (time: {})", requestedTime);
}
```

### Fix 2: Flexible Date/DateTime Parsing (BookingController.java)
Updated the controller to accept both date and datetime formats:

#### Before
```java
try {
    // Validate and parse date
    LocalDate requestedDate = LocalDate.parse(date);
    
    // Convert to LocalDateTime (start of day for checking)
    LocalDateTime dateTime = requestedDate.atStartOfDay();
    
    // Check availability
    boolean isAvailable = bookingService.checkAvailability(postId, dateTime);
    // ...
} catch (DateTimeParseException e) {
    // ...error handling
}
```

#### After
```java
try {
    LocalDateTime dateTime;
    LocalDate requestedDate;
    
    // Try to parse as LocalDateTime first (ISO format: 2025-11-20T10:00:00)
    // If that fails, try parsing as LocalDate (yyyy-MM-dd)
    try {
        dateTime = LocalDateTime.parse(date);
        requestedDate = dateTime.toLocalDate();
    } catch (DateTimeParseException e1) {
        // Try parsing as LocalDate only
        requestedDate = LocalDate.parse(date);
        dateTime = requestedDate.atStartOfDay();
    }
    
    // Check availability
    boolean isAvailable = bookingService.checkAvailability(postId, dateTime);
    // ...
} catch (DateTimeParseException e) {
    // ...error handling with updated message
}
```

## Benefits
1. **More Reliable**: Explicit value checking for midnight detection is more reliable than object equality
2. **Better Logging**: Added debug log to confirm when midnight detection works
3. **Flexible Input**: API now accepts both date (`2025-11-20`) and datetime (`2025-11-20T10:00:00`) formats
4. **Better UX**: Clients can send specific times for availability checks (e.g., check if 10:00 AM is available)
5. **Backward Compatible**: Still accepts the original `yyyy-MM-dd` format

## Expected Behavior

### Example 1: Date Only (General Availability)
```
GET /api/bookings/availability?postId=8&date=2025-11-20
```

The system will:
1. Parse date as `2025-11-20`
2. Convert to `2025-11-20T00:00`
3. Detect hour=0 and minute=0 (midnight)
4. Skip working hours validation
5. Log: "Checking general availability for the whole day (time: 00:00)"
6. Check if any of the 4 daily slots are available
7. Return availability status without false warnings

### Example 2: DateTime (Specific Time Check)
```
GET /api/bookings/availability?postId=8&date=2025-11-20T10:00:00
```

The system will:
1. Parse datetime as `2025-11-20T10:00:00`
2. Extract time as `10:00`
3. Validate time is within working hours (10:00-18:00) ✓
4. Check if any slots are available at that time
5. Return availability status

### Example 3: Outside Working Hours
```
GET /api/bookings/availability?postId=8&date=2025-11-20T08:00:00
```

The system will:
1. Parse datetime as `2025-11-20T08:00:00`
2. Extract time as `08:00`
3. Validate time is within working hours (10:00-18:00) ✗
4. Log warning: "Requested time 08:00 is outside working hours (10:00-18:00)"
5. Return `false` (not available)

## Files Changed
- `src/main/java/com/myapp/booking/services/BookingService.java`
  - Line 467-480: Updated midnight detection logic
  
- `src/main/java/com/myapp/booking/controllers/BookingController.java`
  - Line 118-137: Updated date parsing to accept both date and datetime formats

## Testing
To test the fixes:

1. Restart the application
2. Test with date only:
   ```
   GET /api/bookings/availability?postId=8&date=2025-11-20
   ```
   - Should work without warnings
   - Should check general day availability
   
3. Test with datetime (within hours):
   ```
   GET /api/bookings/availability?postId=8&date=2025-11-20T10:00:00
   ```
   - Should work and check specific time availability
   
4. Test with datetime (outside hours):
   ```
   GET /api/bookings/availability?postId=8&date=2025-11-20T08:00:00
   ```
   - Should return false with appropriate message

## Related Files
- `BookingController.java` - Calls `checkAvailability()` with parsed datetime
- `BookingService.java` - Implements availability check logic
- `IBookingService.java` - Interface definition for `isTimeSlotAvailable()`

