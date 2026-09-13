package com.springaimcp.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String type,
        String username,
        List<String> roles,
        long expiresIn
) {
}