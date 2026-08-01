package com.example.spring_learning.dto;

public record CreateCourseRequest(
        String title,
        String description,
        String teacherId
) {
}
