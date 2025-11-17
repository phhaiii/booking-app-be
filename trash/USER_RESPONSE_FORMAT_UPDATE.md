# UserResponse API Format Update

## Thay đổi định dạng trả về

Backend đã được cập nhật để trả về dữ liệu User theo định dạng mới:

### Format mới (sau khi cập nhật):
```json
{
  "id": 1,
  "email": "admin@test.com",
  "fullName": "Nguyen Hai",
  "phone": "0988776655",
  "role_name": "ADMIN",
  "avatar": null
}
```

### Các thay đổi chính:

1. **`roleName` → `role_name`** (trong JSON)
   - Field trong Java vẫn là `roleName` để tuân thủ naming convention
   - Sử dụng `@JsonProperty("role_name")` để serialize thành `role_name` trong JSON response
   - Giá trị: tên role dạng String (VD: "ADMIN", "USER", "VENDOR")

2. **`avatarUrl` → `avatar`**
   - Field đổi từ `avatarUrl` thành `avatar`
   - Chứa URL của avatar hoặc `null` nếu chưa có

### Files đã được cập nhật:

1. **UserResponse.java**
   - Thêm `@JsonProperty("role_name")` cho field `roleName`
   - Đổi `avatarUrl` thành `avatar`

2. **AuthService.java**
   - Cập nhật method `buildAuthResponse()` 
   - Sử dụng `.roleName()` và `.avatar()`

3. **UserService.java**
   - Cập nhật method `mapToUserResponse()`
   - Sử dụng `.roleName()` và `.avatar()`

4. **AdminService.java**
   - Cập nhật method `mapToUserResponse()`
   - Sử dụng `.roleName()` và `.avatar()`

### API Endpoints trả về format mới:

- `POST /api/auth/register` - Đăng ký user mới
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/refresh-token` - Refresh token
- `GET /api/admin/users` - Danh sách users (admin)
- `GET /api/admin/users/{id}` - Chi tiết user (admin)
- Tất cả các endpoint khác trả về UserResponse

### Ví dụ response đầy đủ:

```json
{
  "success": true,
  "message": "Đăng nhập thành công!",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "admin@test.com",
      "fullName": "Nguyen Hai",
      "phone": "0988776655",
      "role_name": "ADMIN",
      "avatar": null,
      "address": "Ha Noi",
      "dateOfBirth": "1990-01-01",
      "isActive": true,
      "isLocked": false,
      "createdAt": "2025-11-17T10:00:00",
      "updatedAt": "2025-11-17T14:00:00"
    }
  },
  "timestamp": "2025-11-17T14:30:00"
}
```

### Testing:

1. Khởi động lại ứng dụng
2. Test endpoint login:
   ```bash
   POST http://localhost:8089/api/auth/login
   Content-Type: application/json
   
   {
     "email": "admin@test.com",
     "password": "your_password"
   }
   ```

3. Kiểm tra response có chứa `role_name` và `avatar` thay vì `roleName` và `avatarUrl`

### Backward Compatibility:

- Các field khác vẫn giữ nguyên format
- Frontend cần cập nhật để sử dụng `role_name` thay vì `roleName`
- Frontend cần cập nhật để sử dụng `avatar` thay vì `avatarUrl`

