package com.example.spring_learning.controller;

import com.example.spring_learning.entity.Test;
import com.example.spring_learning.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test") // This combines with your context path to make: /api/test
public class TestController {

    @Autowired
    private TestRepository testRepository;

    // 1. CREATE: Save a new test record
    @PostMapping
    public Test createRecord(@RequestBody Test testData) {
        return testRepository.save(testData);
    }

    // 2. READ: Get all test records from the Aiven database
    @GetMapping
    public List<Test> getAllRecords() {
        return testRepository.findAll();
    }
}
