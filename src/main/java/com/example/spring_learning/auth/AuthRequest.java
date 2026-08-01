package com.example.spring_learning.auth;

public record AuthRequest(
        String email,
        String password
) {
}
