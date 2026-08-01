package com.example.spring_learning.auth;

public record AuthResponse(
        String message,
        String token,
        String role,
        String displayName,
        String cookieHeaderExample
) {
}
