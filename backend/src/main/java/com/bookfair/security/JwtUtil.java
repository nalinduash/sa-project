package com.bookfair.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:bookfairsecretkeybookfairsecretkey}")
    private String secret;

    @Value("${jwt.accessExpiration:${jwt.expiration:86400000}}")
    private Long accessExpirationMs;

    @Value("${jwt.refreshExpiration:604800000}")
    private Long refreshExpirationMs;

    private final Set<String> blacklistedJti = ConcurrentHashMap.newKeySet();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUserType(String token) {
        return extractAllClaims(token).get("userType", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateAccessToken(String email, String userType) {
        return createToken(email, userType, "ACCESS", accessExpirationMs);
    }

    public String generateRefreshToken(String email, String userType) {
        return createToken(email, userType, "REFRESH", refreshExpirationMs);
    }

    public String generateToken(String email, String userType) {
        return generateAccessToken(email, userType);
    }

    private String createToken(String subject, String userType, String tokenType, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        claims.put("tokenType", tokenType);

        String jti = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .id(jti)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        if (token == null) return false;
        if (isBlacklisted(token)) return false;
        if (!"ACCESS".equals(extractTokenType(token))) return false;

        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean validateRefreshToken(String token, String expectedEmail) {
        if (token == null) return false;
        if (isBlacklisted(token)) return false;
        if (!"REFRESH".equals(extractTokenType(token))) return false;
        if (isTokenExpired(token)) return false;

        return expectedEmail != null && expectedEmail.equals(extractUsername(token));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return validateAccessToken(token, userDetails);
    }

    public void blacklistToken(String token) {
        if (token == null) return;
        try {
            String jti = extractJti(token);
            if (jti != null) blacklistedJti.add(jti);
        } catch (Exception ignored) {
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            String jti = extractJti(token);
            return jti != null && blacklistedJti.contains(jti);
        } catch (Exception e) {
            return true;
        }
    }

    public String generateSecureRandomToken(int bytes) {
        byte[] buf = new byte[bytes];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
