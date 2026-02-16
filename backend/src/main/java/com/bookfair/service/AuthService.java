package com.bookfair.service;

import com.bookfair.dto.AuthResponse;
import com.bookfair.dto.LoginRequest;
import com.bookfair.dto.RegisterRequest;
import com.bookfair.model.User;
import com.bookfair.repository.UserRepository;
import com.bookfair.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${security.passwordreset.ttlSeconds:900}")
    private long resetTtlSeconds;

    private final Map<String, ResetRecord> resetTokens = new ConcurrentHashMap<>();

    public AuthResponse register(RegisterRequest request, User.UserType userType) {
        validateRegister(request);

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            log.warn("AUTH_REGISTER_FAIL emailExists={}", email);
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .businessName(request.getBusinessName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .address(request.getAddress())
                .userType(userType)
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), userType.name());

        log.info("AUTH_REGISTER_SUCCESS email={} type={}", user.getEmail(), userType.name());

        return AuthResponse.builder()
                .token(accessToken)
                .email(user.getEmail())
                .userType(userType.name())
                .businessName(user.getBusinessName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        validateLogin(request);

        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (Exception e) {
            log.warn("AUTH_LOGIN_FAIL email={}", email);
            throw e;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserType().name());

        log.info("AUTH_LOGIN_SUCCESS email={} type={}", user.getEmail(), user.getUserType().name());

        return AuthResponse.builder()
                .token(accessToken)
                .email(user.getEmail())
                .userType(user.getUserType().name())
                .businessName(user.getBusinessName())
                .build();
    }

    public String issueRefreshTokenForUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtUtil.generateRefreshToken(user.getEmail(), user.getUserType().name());
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("AUTH_REFRESH_FAIL reason=missing_token");
            throw new RuntimeException("Missing refresh token");
        }

        String email = jwtUtil.extractUsername(refreshToken);
        if (email == null) {
            log.warn("AUTH_REFRESH_FAIL reason=invalid_token");
            throw new RuntimeException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtUtil.validateRefreshToken(refreshToken, user.getEmail())) {
            log.warn("AUTH_REFRESH_FAIL email={}", user.getEmail());
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccess = jwtUtil.generateAccessToken(user.getEmail(), user.getUserType().name());

        log.info("AUTH_REFRESH_SUCCESS email={}", user.getEmail());

        return AuthResponse.builder()
                .token(newAccess)
                .email(user.getEmail())
                .userType(user.getUserType().name())
                .businessName(user.getBusinessName())
                .build();
    }

    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return;
        jwtUtil.blacklistToken(accessToken);
        try {
            log.info("AUTH_LOGOUT email={}", jwtUtil.extractUsername(accessToken));
        } catch (Exception e) {
            log.info("AUTH_LOGOUT email=unknown");
        }
    }

    public void forgotPassword(String email) {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            log.warn("AUTH_PW_FORGOT_FAIL reason=invalid_email");
            throw new RuntimeException("Invalid email");
        }

        String normalized = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateSecureRandomToken(32);
        Instant expiresAt = Instant.now().plusSeconds(resetTtlSeconds);

        resetTokens.put(token, new ResetRecord(user.getEmail(), expiresAt));

        log.info("AUTH_PW_FORGOT_ISSUED email={} exp={}", user.getEmail(), expiresAt);

        if (mailSender != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("Password Reset");
            msg.setText("Use this token to reset your password: " + token);
            mailSender.send(msg);
        }
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            log.warn("AUTH_PW_RESET_FAIL reason=missing_token");
            throw new RuntimeException("Missing reset token");
        }
        if (newPassword == null || newPassword.length() < 8) {
            log.warn("AUTH_PW_RESET_FAIL reason=weak_password");
            throw new RuntimeException("Password must be at least 8 characters");
        }

        ResetRecord record = resetTokens.get(token);
        if (record == null) {
            log.warn("AUTH_PW_RESET_FAIL reason=invalid_token");
            throw new RuntimeException("Invalid reset token");
        }
        if (Instant.now().isAfter(record.expiresAt)) {
            resetTokens.remove(token);
            log.warn("AUTH_PW_RESET_FAIL reason=expired_token email={}", record.email);
            throw new RuntimeException("Reset token expired");
        }

        User user = userRepository.findByEmail(record.email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token);

        log.info("AUTH_PW_RESET_SUCCESS email={}", user.getEmail());
    }

    private void validateRegister(RegisterRequest r) {
        if (r == null) throw new RuntimeException("Invalid request");
        if (isBlank(r.getEmail()) || !r.getEmail().contains("@")) throw new RuntimeException("Invalid email");
        if (isBlank(r.getPassword()) || r.getPassword().length() < 8) throw new RuntimeException("Password must be at least 8 characters");
        if (isBlank(r.getBusinessName())) throw new RuntimeException("Business name required");
        if (isBlank(r.getContactPerson())) throw new RuntimeException("Contact person required");
        if (isBlank(r.getPhone())) throw new RuntimeException("Phone required");
        if (isBlank(r.getAddress())) throw new RuntimeException("Address required");
    }

    private void validateLogin(LoginRequest r) {
        if (r == null) throw new RuntimeException("Invalid request");
        if (isBlank(r.getEmail()) || !r.getEmail().contains("@")) throw new RuntimeException("Invalid email");
        if (isBlank(r.getPassword())) throw new RuntimeException("Password required");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    record ResetRecord(String email, Instant expiresAt) {}
}
