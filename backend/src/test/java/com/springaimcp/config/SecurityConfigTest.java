package com.springaimcp.config;

import com.springaimcp.service.TempleAiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

import com.springaimcp.security.JwtAuthenticationFilter;
import com.springaimcp.security.JwtTokenProvider;

@WebFluxTest
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TempleAiService templeAiService;

    @MockBean
    private com.springaimcp.service.TempleImageService templeImageService;

    @Test
    void testUnauthenticatedAccessToProtectedMeReturns401WithoutBasicAuthHeader() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().doesNotExist("WWW-Authenticate");
    }

    @Test
    void testPublicTemplesEndpointIsPermitted() {
        when(templeAiService.getAllTemples()).thenReturn(List.of());

        webTestClient.get()
                .uri("/api/v1/temples")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAuthenticatedAdminAccessToProtectedMeReturns200() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("admin");
    }
}
