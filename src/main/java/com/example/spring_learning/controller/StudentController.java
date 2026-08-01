package com.example.spring_learning.controller;

import com.example.spring_learning.auth.AuthenticatedUser;
import com.example.spring_learning.auth.PasswordService;
import com.example.spring_learning.auth.RequireRole;
import com.example.spring_learning.auth.Role;
import com.example.spring_learning.dto.CreateStudentRequest;
import com.example.spring_learning.dto.StudentResponse;
import com.example.spring_learning.entity.StudentProfile;
import com.example.spring_learning.entity.UserAccount;
import com.example.spring_learning.repository.StudentProfileRepository;
import com.example.spring_learning.repository.UserAccountRepository;
import com.example.spring_learning.service.SchoolMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentProfileRepository studentProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordService passwordService;
    private final SchoolMapper schoolMapper;

    public StudentController(
            StudentProfileRepository studentProfileRepository,
            UserAccountRepository userAccountRepository,
            PasswordService passwordService,
            SchoolMapper schoolMapper
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordService = passwordService;
        this.schoolMapper = schoolMapper;
    }

    /*
     * TEACHER-only endpoint.
     * Teachers/admins create student accounts. Students should not create users.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(Role.TEACHER)
    public StudentResponse createStudent(@RequestBody CreateStudentRequest request) {
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserAccount account = new UserAccount();
        account.setDisplayName(request.fullName());
        account.setEmail(request.email());
        account.setPasswordHash(passwordService.hash(request.password()));
        account.setRole(Role.STUDENT);

        StudentProfile student = new StudentProfile();
        student.setFullName(request.fullName());
        student.setGrade(request.grade());
        student.setGuardianPhone(request.guardianPhone());
        student.setUserAccount(account);

        return schoolMapper.toStudentResponse(studentProfileRepository.save(student));
    }

    @GetMapping
    @RequireRole(Role.TEACHER)
    public List<StudentResponse> getStudents() {
        return studentProfileRepository.findAll().stream()
                .map(schoolMapper::toStudentResponse)
                .toList();
    }

    /*
     * STUDENT-only endpoint.
     * A student can see only their own profile.
     */
    @GetMapping("/me")
    @RequireRole(Role.STUDENT)
    public StudentResponse getMyProfile(@RequestAttribute("authUser") AuthenticatedUser user) {
        StudentProfile student = studentProfileRepository.findByUserAccountId(user.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
        return schoolMapper.toStudentResponse(student);
    }
}
