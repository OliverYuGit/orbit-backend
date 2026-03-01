package com.orbit.mission.auth;

import com.orbit.mission.user.UserEntity;
import com.orbit.mission.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        log.debug("Processing request to {} with Authorization header: {}", 
                  request.getRequestURI(), header != null ? "present" : "absent");
        
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long userId = jwtService.getUserId(token);
                int tokenVersion = jwtService.getTokenVersion(token);
                log.debug("Token valid for userId={}, tokenVersion={}", userId, tokenVersion);

                // Validate token_version to support logout/password-change revocation
                Optional<UserEntity> userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    log.warn("User not found for userId={}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                UserEntity user = userOpt.get();
                if (user.getTokenVersion() != tokenVersion) {
                    log.warn("Token version mismatch: token={}, user={}", tokenVersion, user.getTokenVersion());
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = jwtService.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authentication set for user: {}", username);
            } catch (Exception e) {
                log.error("Error processing JWT token", e);
            }
        } else {
            log.debug("Token invalid or authentication already set");
        }

        filterChain.doFilter(request, response);
    }
}
