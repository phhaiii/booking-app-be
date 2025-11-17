# Comment Images Deserialization Fix

## Problem
The application was throwing a `JpaSystemException: could not deserialize` error when fetching comments from the database. 

### Root Cause
The `Comment` entity had a `List<String> images` field annotated with `@Column(columnDefinition = "JSON")`, but no JPA converter was configured to handle the JSON serialization/deserialization. Hibernate attempted to use default Java object serialization which failed with an `EOFException`.

### Error Stack Trace
```
org.springframework.orm.jpa.JpaSystemException: could not deserialize
...
Caused by: org.hibernate.type.SerializationException: could not deserialize
...
Caused by: java.io.EOFException
```

The error occurred in:
- `CommentService.getCommentsByPost()` at line 132
- `CommentRepository.findByPostIdAndIsActiveTrue()`

## Solution

### 1. Created JsonListConverter
Created a new JPA AttributeConverter at:
`src/main/java/com/myapp/booking/configurations/JsonListConverter.java`

This converter handles the conversion between:
- **Database → Java**: JSON string → `List<String>`
- **Java → Database**: `List<String>` → JSON string

Features:
- Uses Jackson ObjectMapper for JSON processing
- Returns empty list `[]` for null/empty values
- Includes error handling and logging
- Properly handles both serialization and deserialization

### 2. Updated Comment Entity
Added `@Convert` annotation to the `images` field in the `Comment` entity:

```java
@Column(columnDefinition = "JSON")
@Convert(converter = com.myapp.booking.configurations.JsonListConverter.class)
@Builder.Default
private List<String> images = new ArrayList<>();
```

## Files Modified
1. **Created**: `src/main/java/com/myapp/booking/configurations/JsonListConverter.java`
2. **Modified**: `src/main/java/com/myapp/booking/models/Comment.java`

## Verification
- ✅ Converter implements `AttributeConverter<List<String>, String>`
- ✅ Converter is annotated with `@Converter`
- ✅ Comment entity uses `@Convert` annotation with the converter
- ✅ No compilation errors

## Other Entities Checked
- **Post**: Uses `@ElementCollection` for List fields ✅ (correct approach)
- **Menu**: Uses `@ElementCollection` for List fields ✅ (correct approach)
- **WeddingVenues**: Uses `String` type for JSON fields ✅ (no issue)
- **Booking**: Uses `String` type for JSON fields ✅ (no issue)

## Testing
After deploying this fix, the following operations should work correctly:
- ✅ Fetching comments with images
- ✅ Creating comments with images
- ✅ Updating comment images
- ✅ Deserializing existing JSON data from the database

## Notes
- The converter handles edge cases like null/empty values
- Includes proper error logging for debugging
- Compatible with existing database JSON format
- No database migration needed - works with existing data
