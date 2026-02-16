package com.bookfair.controller;

import com.bookfair.dto.AuthResponse;
import com.bookfair.dto.LoginRequest;
import com.bookfair.dto.RegisterRequest;
import com.bookfair.model.User;
import com.bookfair.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse body = authService.register(request, User.UserType.VENDOR);
        String refresh = authService.issueRefreshTokenForUser(body.getEmail());
        return ResponseEntity.ok()
                .header("X-Refresh-Token", refresh)
                .body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse body = authService.login(request);
        String refresh = authService.issueRefreshTokenForUser(body.getEmail());
        return ResponseEntity.ok()
                .header("X-Refresh-Token", refresh)
                .body(body);
    }

    @PostMapping("/employee/login")
    public ResponseEntity<AuthResponse> employeeLogin(@RequestBody LoginRequest request) {
        AuthResponse body = authService.login(request);
        if (!"EMPLOYEE".equals(body.getUserType())) {
            throw new RuntimeException("Access denied: Not an employee account");
        }
        String refresh = authService.issueRefreshTokenForUser(body.getEmail());
        return ResponseEntity.ok()
                .header("X-Refresh-Token", refresh)
                .body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader(value = "X-Refresh-Token", required = false) String headerToken,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String token = headerToken;
        if ((token == null || token.isBlank()) && body != null) {
            token = body.get("refreshToken");
        }

        AuthResponse refreshed = authService.refreshAccessToken(token);
        String newRefresh = authService.issueRefreshTokenForUser(refreshed.getEmail());

        return ResponseEntity.ok()
                .header("X-Refresh-Token", newRefresh)
                .body(refreshed);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
