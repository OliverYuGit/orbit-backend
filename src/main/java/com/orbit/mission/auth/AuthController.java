package com.orbit.mission.auth;

import com.orbit.mission.common.ApiResponse;
import com.orbit.mission.user.UserDto;
import com.orbit.mission.user.UserEntity;
import com.orbit.mission.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal);

        // update last_login_at
        userRepository.findById(principal.getId()).ifPresent(u -> {
            u.setLastLoginAt(Instant.now());
            userRepository.save(u);
        });

        return ResponseEntity.ok(ApiResponse.ok(new LoginResponse(token, "Bearer", 86400L,
                userRepository.findById(principal.getId()).map(UserDto::new).orElse(null))));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok(new UserDto(user)));
    }

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Getter
    public static class LoginResponse {
        private final String accessToken;
        private final String tokenType;
        private final long expiresIn;
        private final UserDto user;

        public LoginResponse(String accessToken, String tokenType, long expiresIn, UserDto user) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.user = user;
        }
    }
}
