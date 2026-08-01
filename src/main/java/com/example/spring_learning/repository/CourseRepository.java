package com.example.spring_learning.repository;

import com.example.spring_learning.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByStudentsUserAccountId(String userAccountId);
}
