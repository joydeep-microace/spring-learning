package com.example.spring_learning.repository;

import com.example.spring_learning.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<Test, String> {
    /*
     * JpaRepository already gives common database methods:
     * - save(entity)
     * - findAll()
     * - findById(id)
     * - deleteById(id)
     *
     * A method named get() is not a valid Spring Data derived query here,
     * so the controller should call findAll() instead.
     */
}
