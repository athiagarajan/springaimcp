package com.springaimcp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "c3ByaW5nYWltY3Atc2VjcmV0LWtleS0yNTYtYml0cy1sb25nLWZvci1qd3QtYXV0aC1zZWN1cml0eQ==";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = tokenProvider.generateToken("admin", List.of("ROLE_ADMIN", "ROLE_USER"));
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("admin", tokenProvider.getUsernameFromToken(token));

        List<String> roles = tokenProvider.getRolesFromToken(token);
        assertThat(roles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void testGenerateTokenFromUserDetails() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .roles("USER")
                .build();

        String token = tokenProvider.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("testuser", tokenProvider.getUsernameFromToken(token));
        assertThat(tokenProvider.getRolesFromToken(token)).contains("ROLE_USER");
    }

    @Test
    void testGetAuthenticationFromToken() {
        String token = tokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
        Authentication auth = tokenProvider.getAuthentication(token);

        assertNotNull(auth);
        assertEquals("admin", auth.getName());
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void testInvalidTokenReturnsFalse() {
        assertFalse(tokenProvider.validateToken("invalid.token.string"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void testExpiredTokenValidationFails() {
        // Create token provider with negative expiration so token is immediately expired
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(TEST_SECRET, -1000);
        String expiredToken = shortLivedProvider.generateToken("admin", List.of("ROLE_ADMIN"));

        assertFalse(shortLivedProvider.validateToken(expiredToken));
    }
}