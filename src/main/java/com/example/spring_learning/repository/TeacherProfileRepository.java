package com.example.spring_learning.repository;

import com.example.spring_learning.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, String> {
    Optional<TeacherProfile> findByUserAccountId(String userAccountId);
}
