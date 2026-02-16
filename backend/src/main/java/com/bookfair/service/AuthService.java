package com.bookfair.service;

import com.bookfair.dto.*;
import com.bookfair.model.User;
import com.bookfair.repository.UserRepository;
import com.bookfair.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request, User.UserType userType) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .businessName(request.getBusinessName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .address(request.getAddress())
                .userType(userType)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), userType.name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userType(userType.name())
                .businessName(user.getBusinessName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserType().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userType(user.getUserType().name())
                .businessName(user.getBusinessName())
                .build();
    }
}