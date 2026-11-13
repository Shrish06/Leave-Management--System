package com.company.leavemanagement.service;

import com.company.leavemanagement.dto.AuthResponse;
import com.company.leavemanagement.dto.LoginRequest;
import com.company.leavemanagement.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
