package com.example.spring_learning.dto;

public record StudentResponse(
        String id,
        String fullName,
        String email,
        String grade,
        String guardianPhone
) {
}
