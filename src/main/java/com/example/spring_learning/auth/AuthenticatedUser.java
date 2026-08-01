package com.example.spring_learning.auth;

/*
 * This object represents the user that was proven by the JWT.
 * Controllers can read it from the request after JwtAuthenticationFilter runs.
 */
public record AuthenticatedUser(
        String userId,
        String email,
        String displayName,
        Role role
) {
}
