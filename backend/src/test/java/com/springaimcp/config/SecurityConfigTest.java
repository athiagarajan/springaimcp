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

@WebFluxTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TempleAiService templeAiService;

    @MockBean
    private com.springaimcp.service.TempleImageService templeImageService;

    @Test
    void testUnauthenticatedAccessToSwaggerReturns401() {
        webTestClient.get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAuthenticatedAdminAccessToApiReturns200() {
        when(templeAiService.getAllTemples()).thenReturn(List.of());

        webTestClient.get()
                .uri("/api/v1/temples")
                .exchange()
                .expectStatus().isOk();
    }
}
