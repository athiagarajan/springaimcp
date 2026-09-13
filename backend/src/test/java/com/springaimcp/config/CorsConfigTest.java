package com.springaimcp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private CorsConfig corsConfig;
    private CorsConfigurationSource configurationSource;
    private DefaultCorsProcessor corsProcessor;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000,https://springaimcp-frontend.onrender.com");
        configurationSource = corsConfig.corsConfigurationSource();
        corsProcessor = new DefaultCorsProcessor();
    }

    @Test
    void testAllowedLocalhostOrigin() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/v1/temples")
                .header("Origin", "http://localhost:3000")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = configurationSource.getCorsConfiguration(exchange);
        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("http://localhost:3000")).isEqualTo("http://localhost:3000");

        boolean result = corsProcessor.process(config, exchange);
        assertThat(result).isTrue();
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin")).isEqualTo("http://localhost:3000");
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Credentials")).isEqualTo("true");
    }

    @Test
    void testAllowedRenderFrontendOrigin() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/v1/temples")
                .header("Origin", "https://springaimcp-frontend.onrender.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = configurationSource.getCorsConfiguration(exchange);
        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("https://springaimcp-frontend.onrender.com")).isEqualTo("https://springaimcp-frontend.onrender.com");

        boolean result = corsProcessor.process(config, exchange);
        assertThat(result).isTrue();
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin")).isEqualTo("https://springaimcp-frontend.onrender.com");
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Credentials")).isEqualTo("true");
    }

    @Test
    void testDisallowedOriginIsRejected() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/v1/temples")
                .header("Origin", "http://malicious-domain.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = configurationSource.getCorsConfiguration(exchange);
        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("http://malicious-domain.com")).isNull();

        boolean result = corsProcessor.process(config, exchange);
        assertThat(result).isFalse();
        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(403);
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void testPreflightOptionsRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.options("http://localhost:8080/api/v1/temples")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = configurationSource.getCorsConfiguration(exchange);
        boolean result = corsProcessor.process(config, exchange);
        assertThat(result).isTrue();
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin")).isEqualTo("http://localhost:3000");
        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Credentials")).isEqualTo("true");
    }
}
