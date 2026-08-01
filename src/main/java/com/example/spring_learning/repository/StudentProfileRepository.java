package com.example.spring_learning.repository;

import com.example.spring_learning.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {
    Optional<StudentProfile> findByUserAccountId(String userAccountId);
}
