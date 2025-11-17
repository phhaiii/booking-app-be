# Hướng dẫn cập nhật ảnh khi chỉnh sửa bài viết

## 📝 Tổng quan

Backend đã được cập nhật để hỗ trợ **upload ảnh mới** và **giữ lại ảnh cũ** khi chỉnh sửa bài viết (Post).

## 🔧 Thay đổi kỹ thuật

### 1. UpdatePostRequest
- ✅ Thêm field `existingImages: List<String>` để lưu danh sách ảnh cũ cần giữ lại

### 2. PostController
- ✅ Endpoint `PUT /api/posts/{postId}` đã được cập nhật
- ✅ Hỗ trợ `multipart/form-data` thay vì chỉ JSON
- ✅ Nhận thêm 2 tham số:
  - `existingImages` (JSON string): Danh sách URL ảnh cũ cần giữ
  - `newImages` (files): Ảnh mới cần upload

### 3. PostService
- ✅ Method `updatePost()` có thêm parameter `List<MultipartFile> newImages`
- ✅ Upload ảnh mới vào thư mục `uploads/`
- ✅ Merge ảnh cũ + ảnh mới vào list final

## 📡 API Endpoint

### PUT /api/posts/{postId}

**Content-Type**: `multipart/form-data`

**Authorization**: `Bearer {token}` (Role: ADMIN hoặc VENDOR)

### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `title` | String | No | Tiêu đề bài viết |
| `description` | String | No | Mô tả ngắn |
| `content` | String | No | Nội dung chi tiết |
| `location` | String | No | Địa điểm |
| `price` | BigDecimal | No | Giá |
| `capacity` | Integer | No | Sức chứa |
| `style` | String | No | Phong cách |
| `allowComments` | Boolean | No | Cho phép comment |
| `enableNotifications` | Boolean | No | Bật thông báo |
| `amenities` | String (JSON) | No | Tiện ích (JSON array) |
| `existingImages` | String (JSON) | No | Danh sách ảnh cũ giữ lại |
| `newImages` | File[] | No | Ảnh mới upload |

## 💡 Ví dụ sử dụng

### 1. Frontend (React/JavaScript)

```javascript
const updatePost = async (postId, data) => {
  const formData = new FormData();
  
  // Add basic fields
  if (data.title) formData.append('title', data.title);
  if (data.description) formData.append('description', data.description);
  if (data.content) formData.append('content', data.content);
  if (data.location) formData.append('location', data.location);
  if (data.price) formData.append('price', data.price);
  if (data.capacity) formData.append('capacity', data.capacity);
  if (data.style) formData.append('style', data.style);
  
  // Add amenities as JSON
  if (data.amenities && data.amenities.length > 0) {
    formData.append('amenities', JSON.stringify(data.amenities));
  }
  
  // Add existing images to keep (JSON array of URLs)
  if (data.existingImages && data.existingImages.length > 0) {
    formData.append('existingImages', JSON.stringify(data.existingImages));
  }
  
  // Add new images to upload
  if (data.newImages && data.newImages.length > 0) {
    data.newImages.forEach((file) => {
      formData.append('newImages', file);
    });
  }
  
  const response = await fetch(`/api/posts/${postId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      // Don't set Content-Type, browser will set it automatically with boundary
    },
    body: formData
  });
  
  return response.json();
};

// Usage example
const handleUpdatePost = async () => {
  const postData = {
    title: "Sảnh cưới ABC - Updated",
    description: "Mô tả mới",
    price: 5000000,
    capacity: 300,
    amenities: ["Điều hòa", "Âm thanh", "Ánh sáng"],
    
    // Keep 2 existing images
    existingImages: [
      "abc123.jpg",
      "def456.jpg"
    ],
    
    // Upload 3 new images
    newImages: [
      fileObject1, // File object from <input type="file">
      fileObject2,
      fileObject3
    ]
  };
  
  const result = await updatePost(123, postData);
  console.log(result);
};
```

### 2. Postman Testing

**Request Setup:**

1. **Method**: `PUT`
2. **URL**: `http://localhost:8089/api/posts/1`
3. **Headers**:
   ```
   Authorization: Bearer {your_access_token}
   ```

4. **Body** (form-data):
   ```
   title: "Sảnh cưới mới cập nhật"
   description: "Mô tả đã được cập nhật"
   content: "Nội dung chi tiết..."
   location: "Hà Nội"
   price: 6000000
   capacity: 400
   style: "Hiện đại"
   allowComments: true
   enableNotifications: true
   amenities: ["Điều hòa","Âm thanh","Ánh sáng","Sân khấu"]
   existingImages: ["image1.jpg","image2.jpg"]
   newImages: [file1] (select file)
   newImages: [file2] (select file)
   newImages: [file3] (select file)
   ```

### 3. cURL Example

```bash
curl -X PUT http://localhost:8089/api/posts/1 \
  -H "Authorization: Bearer {token}" \
  -F "title=Sảnh cưới ABC Updated" \
  -F "description=Mô tả mới" \
  -F "price=5000000" \
  -F "capacity=300" \
  -F 'amenities=["Điều hòa","Âm thanh"]' \
  -F 'existingImages=["old1.jpg","old2.jpg"]' \
  -F "newImages=@/path/to/image1.jpg" \
  -F "newImages=@/path/to/image2.jpg"
```

## 📤 Response Format

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Post updated successfully",
  "data": {
    "id": 1,
    "title": "Sảnh cưới ABC Updated",
    "description": "Mô tả mới",
    "content": "Nội dung chi tiết...",
    "location": "Hà Nội",
    "price": 6000000,
    "capacity": 400,
    "style": "Hiện đại",
    "images": [
      "old1.jpg",
      "old2.jpg",
      "uuid-new1.jpg",
      "uuid-new2.jpg",
      "uuid-new3.jpg"
    ],
    "amenities": ["Điều hòa", "Âm thanh", "Ánh sáng"],
    "allowComments": true,
    "enableNotifications": true,
    "status": "PUBLISHED",
    "viewCount": 150,
    "likeCount": 25,
    "commentCount": 10,
    "bookingCount": 3,
    "vendor": {
      "id": 2,
      "fullName": "Vendor Name",
      "email": "vendor@example.com"
    },
    "createdAt": "2025-11-15T10:00:00",
    "updatedAt": "2025-11-17T14:30:00",
    "publishedAt": "2025-11-15T10:00:00"
  },
  "timestamp": "2025-11-17T14:30:00"
}
```

### Error Response (401 Unauthorized)

```json
{
  "success": false,
  "message": "You don't have permission to update this post",
  "data": null,
  "timestamp": "2025-11-17T14:30:00"
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Post not found",
  "data": null,
  "timestamp": "2025-11-17T14:30:00"
}
```

## 🔄 Flow xử lý ảnh

1. **Frontend gửi request** với:
   - `existingImages`: ["image1.jpg", "image2.jpg"] → Ảnh cũ giữ lại
   - `newImages`: [file1, file2, file3] → Ảnh mới upload

2. **Backend xử lý**:
   ```
   Step 1: Khởi tạo list rỗng: finalImages = []
   
   Step 2: Thêm ảnh cũ:
           finalImages = ["image1.jpg", "image2.jpg"]
   
   Step 3: Upload ảnh mới → nhận về URLs:
           newUrls = ["uuid-abc.jpg", "uuid-def.jpg", "uuid-ghi.jpg"]
   
   Step 4: Merge:
           finalImages = ["image1.jpg", "image2.jpg", "uuid-abc.jpg", "uuid-def.jpg", "uuid-ghi.jpg"]
   
   Step 5: Lưu vào database
   ```

3. **Kết quả**: Post có tổng 5 ảnh (2 cũ + 3 mới)

## 📁 Cấu trúc thư mục uploads

```
uploads/
  ├── abc123-def456-ghi789.jpg  (ảnh mới upload)
  ├── xyz789-abc123-def456.png
  └── ...
```

- Mỗi ảnh có tên unique: `UUID + extension`
- Truy cập qua: `http://localhost:8089/uploads/{filename}`

## ⚠️ Lưu ý quan trọng

### 1. Xóa ảnh
- Nếu muốn **xóa 1 ảnh cũ**: Không đưa URL của ảnh đó vào `existingImages`
- Nếu muốn **xóa tất cả ảnh cũ**: Không gửi `existingImages` hoặc gửi array rỗng `[]`

### 2. Chỉ upload ảnh mới
```javascript
{
  // Không gửi existingImages → xóa tất cả ảnh cũ
  newImages: [file1, file2, file3]
}
```

### 3. Chỉ giữ ảnh cũ (không upload mới)
```javascript
{
  existingImages: ["image1.jpg", "image2.jpg"],
  // Không gửi newImages
}
```

### 4. Giới hạn số lượng ảnh
- Maximum: 10 ảnh (đã validate trong UpdatePostRequest)
- Nếu vượt quá: Backend sẽ trả về lỗi validation

### 5. File size & type
- Cấu hình trong `application.properties`:
  ```properties
  spring.servlet.multipart.max-file-size=10MB
  spring.servlet.multipart.max-request-size=10MB
  ```

## 🧪 Testing Checklist

- [ ] Upload ảnh mới (không có ảnh cũ)
- [ ] Giữ ảnh cũ (không upload mới)
- [ ] Giữ 2 ảnh cũ + upload 3 ảnh mới
- [ ] Xóa 1 ảnh cũ + giữ lại 1 ảnh + upload 2 ảnh mới
- [ ] Xóa tất cả ảnh cũ + upload ảnh mới
- [ ] Update không đụng đến ảnh (cập nhật chỉ title, description, etc.)
- [ ] Kiểm tra authorization (chỉ vendor sở hữu mới update được)
- [ ] Kiểm tra validation (max 10 ảnh)

## 🎯 Files đã cập nhật

1. ✅ **PostController.java**
   - Đổi endpoint từ `@RequestBody` sang `multipart/form-data`
   - Thêm xử lý `newImages` và `existingImages`

2. ✅ **UpdatePostRequest.java**
   - Thêm field `existingImages`

3. ✅ **IPostService.java**
   - Cập nhật signature `updatePost()` thêm parameter `List<MultipartFile>`

4. ✅ **PostService.java**
   - Implement logic upload ảnh mới
   - Merge ảnh cũ + ảnh mới
   - Helper method `uploadImages()`

## 🚀 Deployment

Không cần thay đổi gì về database schema. Chỉ cần:

1. Khởi động lại application
2. Đảm bảo thư mục `uploads/` có quyền write
3. Test API endpoints

---

**Ngày cập nhật**: 2025-11-17
**Version**: 1.0.0

