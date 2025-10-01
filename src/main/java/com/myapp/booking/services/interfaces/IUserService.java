package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.reponses.UserResponse;
import com.myapp.booking.dtos.requests.ChangePasswordRequest;
import com.myapp.booking.dtos.requests.UpdateProfileRequest;
import org.apache.coyote.BadRequestException;

public interface IUserService {
    UserResponse getCurrentUser(String email);
    UserResponse getUserById(Long id);
    UserResponse updateProfile(String email, UpdateProfileRequest request) throws BadRequestException;
    void changePassword(String email, ChangePasswordRequest request) throws BadRequestException;
    void deleteAccount(String email);
}
