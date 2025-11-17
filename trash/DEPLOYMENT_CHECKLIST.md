# Deployment Checklist - Booking API Path Fix

## ✅ Pre-Deployment Verification

- [x] All files compile without errors
- [x] Service interface updated with rejectBooking method
- [x] Service implementation added for rejectBooking
- [x] Controller endpoints added (vendor, reject)
- [x] Fallback controller created for /bookings/** forwarding
- [x] Path constraints added (/{id:\\d+})
- [x] Documentation created (BOOKING_API_PATH_FIX_SUMMARY.md)

## 🔧 Deployment Steps

### Step 1: Build the Application
```bash
cd C:\Developer\do-an\backend\booking-app-be
.\mvnw.cmd clean package -DskipTests
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
```

### Step 2: Run the Application
```bash
.\mvnw.cmd spring-boot:run
```

Expected startup logs:
```
Tomcat started on port(s): 8089 (http)
Mapped "{[/api/bookings/vendor/statistics]}" onto ...
Mapped "{[/api/bookings/vendor]}" onto ...
Mapped "{[/api/bookings/vendor/{id}/reject]}" onto ...
Mapped "{[/bookings/**]}" onto ... (fallback)
```

### Step 3: Verify Endpoints

#### Test 1: Vendor Statistics (Main Issue)
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8089/api/bookings/vendor/statistics
```

**Expected:** 200 OK with JSON response containing booking statistics

#### Test 2: Vendor Statistics (Fallback Path)
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8089/bookings/vendor/statistics
```

**Expected:** 200 OK (forwarded to /api/bookings/vendor/statistics)
**Log:** Warning message about forwarding

#### Test 3: Reject Booking
```bash
curl -X POST -H "Authorization: Bearer YOUR_TOKEN" \
  "http://localhost:8089/api/bookings/vendor/2/reject?reason=Test+rejection"
```

**Expected:** 200 OK with updated booking status = CANCELLED

#### Test 4: Get Vendor Bookings
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8089/api/bookings/vendor
```

**Expected:** 200 OK with paginated booking list

#### Test 5: Numeric ID Not Confused with "vendor"
```bash
# This should fetch booking ID 123
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8089/api/bookings/123

# This should fetch vendor bookings (not treat "vendor" as ID)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8089/api/bookings/vendor
```

**Expected:** Both return 200 OK with different responses

## ⚠️ Known Warnings (Non-Critical)

### Warning 1: Unused Parameter
- **File:** BookingController.java:103
- **Issue:** `currentUser` parameter in checkAvailability not used
- **Impact:** None (parameter can be removed if not needed for future logic)

### Warning 2: Redundant null initializers
- **File:** BookingService.java:74-75
- **Issue:** `Time startTime = null;` redundant initialization
- **Impact:** None (style preference)

### Warning 3: Inverted boolean method
- **File:** BookingService.java:440
- **Issue:** `isAdmin()` always inverted
- **Impact:** None (could rename to `isNotAdmin()` for clarity)

## 🧪 Testing Matrix

| Endpoint | Method | Auth | Expected | Status |
|----------|--------|------|----------|--------|
| /api/bookings/vendor/statistics | GET | VENDOR/ADMIN | Stats JSON | ⏳ Pending |
| /bookings/vendor/statistics | GET | VENDOR/ADMIN | Stats JSON (forwarded) | ⏳ Pending |
| /api/bookings/vendor | GET | VENDOR/ADMIN | Bookings page | ⏳ Pending |
| /api/bookings/{id}/reject | POST | VENDOR/ADMIN | Rejected booking | ⏳ Pending |
| /api/bookings/vendor/{id}/reject | POST | VENDOR/ADMIN | Rejected booking | ⏳ Pending |
| /api/bookings/{id} | GET | USER/VENDOR/ADMIN | Single booking | ⏳ Pending |
| /api/bookings/vendor | GET | USER | 403 Forbidden | ⏳ Pending |

## 📊 Success Criteria

### Critical (Must Pass)
- [ ] GET /api/bookings/vendor/statistics returns 200 OK
- [ ] GET /bookings/vendor/statistics forwards and returns 200 OK
- [ ] POST /api/bookings/vendor/{id}/reject successfully rejects booking
- [ ] Numeric IDs work without collision with "vendor" literal

### Important (Should Pass)
- [ ] Unauthorized users get 403 for vendor endpoints
- [ ] Reject booking validates status (only PENDING/CONFIRMED)
- [ ] Reject booking records reason in cancellationReason field
- [ ] Admin can override vendorId parameter

### Nice to Have
- [ ] Forwarding logs warning message for monitoring
- [ ] Frontend updated to use /api prefix directly
- [ ] Integration tests added for new endpoints

## 🐛 Troubleshooting

### Issue: Still getting NoResourceFoundException
**Solution:** 
1. Ensure application was rebuilt: `.\mvnw.cmd clean package`
2. Ensure application was restarted
3. Check startup logs for endpoint mappings
4. Verify request includes correct Authorization header

### Issue: 403 Forbidden
**Solution:**
1. Verify JWT token is valid and not expired
2. Check user has ROLE_VENDOR or ROLE_ADMIN
3. For vendor endpoints, verify user owns the resource or is admin

### Issue: 400 Bad Request on reject
**Solution:**
1. Check booking status is PENDING or CONFIRMED
2. Verify booking exists and is not deleted
3. Check user authorization (must own venue or be admin)

### Issue: Forwarding not working
**Solution:**
1. Verify BookingForwardController is registered (check startup logs)
2. Ensure no conflicting static resource mappings
3. Check forward path in logs

## 🔄 Rollback Plan

If issues occur, rollback by:

1. **Restore original BookingController.java**
   ```bash
   git checkout HEAD -- src/main/java/com/myapp/booking/controllers/BookingController.java
   ```

2. **Remove new files**
   ```bash
   rm src/main/java/com/myapp/booking/controllers/BookingForwardController.java
   git checkout HEAD -- src/main/java/com/myapp/booking/services/BookingService.java
   git checkout HEAD -- src/main/java/com/myapp/booking/services/interfaces/IBookingService.java
   ```

3. **Rebuild and restart**
   ```bash
   .\mvnw.cmd clean package -DskipTests
   .\mvnw.cmd spring-boot:run
   ```

## 📝 Post-Deployment Tasks

- [ ] Update API documentation with new endpoints
- [ ] Update frontend to use /api prefix consistently
- [ ] Add monitoring for forwarding warnings
- [ ] Create integration tests for reject workflow
- [ ] Document reject booking business logic
- [ ] Consider fixing /venue/{venueId} semantic mismatch

## 📧 Deployment Sign-off

**Deployed by:** _________________  
**Date:** _________________  
**Version:** 1.0.1  
**Git commit:** _________________  
**All tests passed:** [ ] Yes [ ] No  
**Production ready:** [ ] Yes [ ] No  

## 🎯 Expected Outcomes

After successful deployment:
1. ✅ Frontend can call /bookings/vendor/statistics (with or without /api)
2. ✅ Vendors can reject bookings with optional reason
3. ✅ No more "NoResourceFoundException" errors
4. ✅ No more "MethodArgumentTypeMismatchException" for 'vendor'
5. ✅ Clean separation between numeric IDs and literal strings in paths
6. ✅ Complete CRUD operations for vendor booking management

---

**Next Review:** [Date] - Check forwarding logs and consider removing fallback after frontend update

