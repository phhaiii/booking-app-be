package com.myapp.booking.dtos.reponses;

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
    private String address;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String roleName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
