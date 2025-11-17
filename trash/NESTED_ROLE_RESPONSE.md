# UserResponse Nested Role Object - Implementation Guide

## Định dạng trả về mới

Backend đã được cập nhật để trả về `role` như một **nested object** thay vì chỉ là string.

## 📋 Format JSON Response

### ✅ Format mới (sau khi cập nhật):

```json
{
  "success": true,
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
      "role": {
        "id": 1,
        "name": "ADMIN"
      },
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

## 🔧 Thay đổi kỹ thuật

### 1. Tạo RoleResponse DTO mới

**File**: `RoleResponse.java`

```java
@Getter
@Setter
@Builder
public class RoleResponse {
    private Long id;      // ID của role từ database
    private String name;  // Tên role: ADMIN, USER, VENDOR
}
```

### 2. Cập nhật UserResponse

**File**: `UserResponse.java`

- **Trước**: `private String roleName;`
- **Sau**: `private RoleResponse role;`

```java
@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    
    private RoleResponse role;  // ← Nested object
    
    private String avatar;
    // ... other fields
}
```

### 3. Cập nhật các Service mappers

Tất cả các service đều được cập nhật để build RoleResponse:

**Pattern sử dụng trong tất cả services**:

```java
private UserResponse mapToUserResponse(User user) {
    // Build role response
    RoleResponse roleResponse = null;
    if (user.getRole() != null) {
        roleResponse = RoleResponse.builder()
                .id(user.getRole().getId())
                .name(user.getRole().getRoleName() != null 
                        ? user.getRole().getRoleName().name() 
                        : null)
                .build();
    }
    
    return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .role(roleResponse)  // ← Nested object
            .avatar(user.getAvatarUrl())
            // ... other fields
            .build();
}
```

## 📁 Files đã được cập nhật

1. **RoleResponse.java** (NEW) - DTO mới cho role object
2. **UserResponse.java** - Đổi từ `String roleName` sang `RoleResponse role`
3. **AuthService.java** - Build RoleResponse trong `buildAuthResponse()`
4. **UserService.java** - Build RoleResponse trong `mapToUserResponse()`
5. **AdminService.java** - Build RoleResponse trong `mapToUserResponse()`

## 🚀 API Endpoints trả về format mới

Tất cả các endpoint trả về UserResponse đều có nested role object:

### Authentication Endpoints:
- `POST /api/auth/register` - Đăng ký user mới
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/refresh-token` - Refresh token

### Admin Endpoints:
- `GET /api/admin/users` - Danh sách users
- `GET /api/admin/users/{id}` - Chi tiết user
- `PUT /api/admin/users/{id}/role` - Cập nhật role

### User Profile Endpoints:
- `GET /api/users/profile` - Lấy thông tin profile
- `PUT /api/users/profile` - Cập nhật profile

## 💡 Ví dụ sử dụng

### Login Request:
```bash
POST http://localhost:8089/api/auth/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "123456"
}
```

### Login Response:
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
      "role": {
        "id": 1,
        "name": "ADMIN"
      },
      "avatar": null
    }
  },
  "timestamp": "2025-11-17T14:30:00"
}
```

## 📱 Frontend Integration

### Accessing role data:

```javascript
// Login response
const response = await login(credentials);

// Access user info
const user = response.data.user;
console.log(user.id);           // 1
console.log(user.email);        // "admin@test.com"
console.log(user.fullName);     // "Nguyen Hai"

// Access role info (nested object)
console.log(user.role.id);      // 1
console.log(user.role.name);    // "ADMIN"
console.log(user.avatar);       // null or URL string
```

### TypeScript Interface:

```typescript
interface RoleResponse {
  id: number;
  name: string;
}

interface UserResponse {
  id: number;
  email: string;
  fullName: string;
  phone: string;
  role: RoleResponse;  // Nested object
  avatar: string | null;
  address?: string;
  dateOfBirth?: string;
  isActive?: boolean;
  isLocked?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}
```

## ✅ Testing

1. **Khởi động lại application**
   ```bash
   mvn spring-boot:run
   ```

2. **Test login endpoint**
   ```bash
   curl -X POST http://localhost:8089/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@test.com","password":"123456"}'
   ```

3. **Verify response structure**
   - ✅ `user.role` là object (không phải string)
   - ✅ `user.role.id` chứa ID của role
   - ✅ `user.role.name` chứa tên role (ADMIN/USER/VENDOR)
   - ✅ `user.avatar` thay vì `user.avatarUrl`

## 🎯 Benefits

1. **Structured Data**: Role được trả về dưới dạng object có cấu trúc rõ ràng
2. **Extensible**: Dễ dàng thêm các field khác vào RoleResponse nếu cần (permissions, description, etc.)
3. **Type Safety**: Frontend có thể định nghĩa interface rõ ràng
4. **Consistency**: Tất cả endpoints đều trả về format giống nhau
5. **Database Ready**: ID và name đều được lấy từ database

## 📝 Notes

- Role ID được lấy từ `role.getId()`
- Role name được convert từ enum sang string: `roleName.name()`
- Null safe: Nếu user không có role, trả về `role: null`
- Avatar field đã được đổi từ `avatarUrl` thành `avatar`

