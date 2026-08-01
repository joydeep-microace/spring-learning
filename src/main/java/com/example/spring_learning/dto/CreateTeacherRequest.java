package com.example.spring_learning.dto;

public record CreateTeacherRequest(
        String fullName,
        String email,
        String password,
        String subject
) {
}
