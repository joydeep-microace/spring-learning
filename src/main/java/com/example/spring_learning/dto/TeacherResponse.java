package com.example.spring_learning.dto;

public record TeacherResponse(
        String id,
        String fullName,
        String email,
        String subject
) {
}
