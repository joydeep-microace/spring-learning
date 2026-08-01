package com.example.spring_learning.dto;

public record CreateStudentRequest(
        String fullName,
        String email,
        String password,
        String grade,
        String guardianPhone
) {
}
