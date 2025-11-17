package com.myapp.booking.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    
    private RoleResponse role;         // Nested role object with id and name
    
    private String avatar;             // Avatar URL

    // Additional fields (optional)
    private String address;
    private LocalDate dateOfBirth;
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
