package com.company.leavemanagement.service.impl;

import com.company.leavemanagement.dto.AuthResponse;
import com.company.leavemanagement.dto.LoginRequest;
import com.company.leavemanagement.dto.RegisterRequest;
import com.company.leavemanagement.entity.User;
import com.company.leavemanagement.exception.DuplicateResourceException;
import com.company.leavemanagement.repository.UserRepository;
import com.company.leavemanagement.security.JwtUtil;
import com.company.leavemanagement.security.UserPrincipal;
import com.company.leavemanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.leave.default-balance}")
    private int defaultLeaveBalance;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment())
                .totalLeaveBalance(defaultLeaveBalance)
                .usedLeaveBalance(0)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId());

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User disappeared after authentication"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
