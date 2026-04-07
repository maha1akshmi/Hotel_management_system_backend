package com.example.hms.service;

import com.example.hms.dto.UpdateUserRequest;
import com.example.hms.dto.UserResponse;

public interface UserService {
    UserResponse getCurrentUser(String email);
    UserResponse updateCurrentUser(String email, UpdateUserRequest request);
}
