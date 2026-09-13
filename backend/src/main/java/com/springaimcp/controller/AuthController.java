package com.springaimcp.controller;

import com.springaimcp.dto.AuthRequest;
import com.springaimcp.dto.AuthResponse;
import com.springaimcp.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for JWT login, token verification, and user profile")
public class AuthController {

    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Authenticate user and get JWT Bearer token", description = "Validates username and password, returns signed JWT token")
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return userDetailsService.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .map(user -> {
                    List<String> roles = user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());
                    String token = tokenProvider.generateToken(user.getUsername(), roles);
                    AuthResponse response = new AuthResponse(
                            token,
                            "Bearer",
                            user.getUsername(),
                            roles,
                            tokenProvider.getExpirationMs()
                    );
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Get current authenticated user info", description = "Returns username and roles extracted from JWT")
    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    List<String> roles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());
                    Map<String, Object> userProfile = Map.of(
                            "username", auth.getName(),
                            "roles", roles,
                            "authenticated", true
                    );
                    return ResponseEntity.ok(userProfile);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}