package com.example.spring_learning.service;

import com.example.spring_learning.dto.CourseResponse;
import com.example.spring_learning.dto.StudentResponse;
import com.example.spring_learning.dto.TeacherResponse;
import com.example.spring_learning.entity.Course;
import com.example.spring_learning.entity.StudentProfile;
import com.example.spring_learning.entity.TeacherProfile;
import org.springframework.stereotype.Component;

@Component
public class SchoolMapper {
    public TeacherResponse toTeacherResponse(TeacherProfile teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getUserAccount().getEmail(),
                teacher.getSubject()
        );
    }

    public StudentResponse toStudentResponse(StudentProfile student) {
        return new StudentResponse(
                student.getId(),
                student.getFullName(),
                student.getUserAccount().getEmail(),
                student.getGrade(),
                student.getGuardianPhone()
        );
    }

    public CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                toTeacherResponse(course.getTeacher()),
                course.getStudents().stream()
                        .map(this::toStudentResponse)
                        .toList()
        );
    }
}
