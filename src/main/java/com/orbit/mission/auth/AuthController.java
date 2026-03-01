package com.orbit.mission.auth;

import com.orbit.mission.common.ApiResponse;
import com.orbit.mission.user.UserDto;
import com.orbit.mission.user.UserEntity;
import com.orbit.mission.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginRateLimiter rateLimiter;

    @Value("${app.jwt.refresh-expiration-ms:2592000000}")
    private long refreshExpirationMs;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        if (!rateLimiter.isAllowed(ip, req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Please try again later.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        UserEntity user = userRepository.findById(principal.getId()).orElseThrow();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        rateLimiter.reset(ip, req.getUsername());

        String accessToken = jwtService.generateToken(principal, user.getTokenVersion());
        String refreshToken = issueRefreshToken(user.getId());

        return ResponseEntity.ok(ApiResponse.ok(new LoginResponse(
                accessToken, "Bearer",
                jwtService.getExpirationMs() / 1000,
                refreshToken,
                new UserDto(user))));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        String hash = sha256(req.getRefreshToken());
        RefreshTokenEntity rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        UserEntity user = userRepository.findById(rt.getUserId()).orElseThrow();
        UserPrincipal principal = new UserPrincipal(user);

        // Rotate refresh token
        refreshTokenRepository.delete(rt);
        String newRefreshToken = issueRefreshToken(user.getId());
        String accessToken = jwtService.generateToken(principal, user.getTokenVersion());

        return ResponseEntity.ok(ApiResponse.ok(new LoginResponse(
                accessToken, "Bearer",
                jwtService.getExpirationMs() / 1000,
                newRefreshToken,
                new UserDto(user))));
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        if (authentication != null) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            // Increment token_version to invalidate all existing access tokens
            userRepository.findById(principal.getId()).ifPresent(u -> {
                u.setTokenVersion(u.getTokenVersion() + 1);
                userRepository.save(u);
            });
            // Revoke all refresh tokens for this user
            refreshTokenRepository.deleteByUserId(principal.getId());
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok(new UserDto(user)));
    }

    // --- helpers ---

    private String issueRefreshToken(Long userId) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshTokenEntity rt = new RefreshTokenEntity();
        rt.setUserId(userId);
        rt.setTokenHash(sha256(raw));
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(rt);
        return raw;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // --- DTOs ---

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Getter @Setter
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    @Getter
    public static class LoginResponse {
        private final String accessToken;
        private final String tokenType;
        private final long expiresIn;
        private final String refreshToken;
        private final UserDto user;

        public LoginResponse(String accessToken, String tokenType, long expiresIn,
                             String refreshToken, UserDto user) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }
}
