package com.example.spring_learning.controller;

import com.example.spring_learning.auth.RequireRole;
import com.example.spring_learning.auth.Role;
import com.example.spring_learning.dto.CreateTeacherRequest;
import com.example.spring_learning.dto.TeacherResponse;
import com.example.spring_learning.entity.TeacherProfile;
import com.example.spring_learning.entity.UserAccount;
import com.example.spring_learning.repository.TeacherProfileRepository;
import com.example.spring_learning.repository.UserAccountRepository;
import com.example.spring_learning.service.SchoolMapper;
import com.example.spring_learning.auth.PasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequireRole(Role.TEACHER)
public class TeacherController {
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordService passwordService;
    private final SchoolMapper schoolMapper;

    public TeacherController(
            TeacherProfileRepository teacherProfileRepository,
            UserAccountRepository userAccountRepository,
            PasswordService passwordService,
            SchoolMapper schoolMapper
    ) {
        this.teacherProfileRepository = teacherProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordService = passwordService;
        this.schoolMapper = schoolMapper;
    }

    /*
     * TEACHER-only endpoint.
     * Creates another teacher login. This is an admin-style feature.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherResponse createTeacher(@RequestBody CreateTeacherRequest request) {
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserAccount account = new UserAccount();
        account.setDisplayName(request.fullName());
        account.setEmail(request.email());
        account.setPasswordHash(passwordService.hash(request.password()));
        account.setRole(Role.TEACHER);

        TeacherProfile teacher = new TeacherProfile();
        teacher.setFullName(request.fullName());
        teacher.setSubject(request.subject());
        teacher.setUserAccount(account);

        return schoolMapper.toTeacherResponse(teacherProfileRepository.save(teacher));
    }

    @GetMapping
    public List<TeacherResponse> getTeachers() {
        return teacherProfileRepository.findAll().stream()
                .map(schoolMapper::toTeacherResponse)
                .toList();
    }
}
