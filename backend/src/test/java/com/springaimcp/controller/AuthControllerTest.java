package com.springaimcp.controller;

import com.springaimcp.config.SecurityConfig;
import com.springaimcp.dto.AuthRequest;
import com.springaimcp.security.JwtAuthenticationFilter;
import com.springaimcp.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void testLoginWithValidCredentialsReturns200AndJwtToken() {
        AuthRequest request = new AuthRequest("admin", "adminpassword");

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.type").isEqualTo("Bearer")
                .jsonPath("$.username").isEqualTo("admin")
                .jsonPath("$.roles[0]").isEqualTo("ROLE_ADMIN");
    }

    @Test
    void testLoginWithInvalidCredentialsReturns401() {
        AuthRequest request = new AuthRequest("admin", "wrongpassword");

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginWithNonexistentUserReturns401() {
        AuthRequest request = new AuthRequest("unknownuser", "password");

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testGetCurrentUserWithValidJwtReturnsProfile() {
        String token = tokenProvider.generateToken("admin", java.util.List.of("ROLE_ADMIN", "ROLE_USER"));

        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("admin")
                .jsonPath("$.authenticated").isEqualTo(true)
                .jsonPath("$.roles").isArray();
    }

    @Test
    void testGetCurrentUserWithoutTokenReturns401() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}