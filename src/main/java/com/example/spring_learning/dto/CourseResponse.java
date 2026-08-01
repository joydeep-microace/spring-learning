package com.example.spring_learning.dto;

import java.util.List;

public record CourseResponse(
        String id,
        String title,
        String description,
        TeacherResponse teacher,
        List<StudentResponse> students
) {
}
