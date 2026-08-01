package com.example.spring_learning.config;

import com.example.spring_learning.auth.PasswordService;
import com.example.spring_learning.auth.Role;
import com.example.spring_learning.entity.StudentProfile;
import com.example.spring_learning.entity.TeacherProfile;
import com.example.spring_learning.entity.UserAccount;
import com.example.spring_learning.repository.StudentProfileRepository;
import com.example.spring_learning.repository.TeacherProfileRepository;
import com.example.spring_learning.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataLoader implements CommandLineRunner {
    private final UserAccountRepository userAccountRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordService passwordService;

    public DemoDataLoader(
            UserAccountRepository userAccountRepository,
            TeacherProfileRepository teacherProfileRepository,
            StudentProfileRepository studentProfileRepository,
            PasswordService passwordService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordService = passwordService;
    }

    /*
     * This creates sample users only when the email does not already exist.
     * You can log in immediately with:
     * - Teacher/admin: admin@school.com / admin123
     * - Student: student@school.com / student123
     */
    @Override
    public void run(String... args) {
        createTeacherIfMissing();
        createStudentIfMissing();
    }

    private void createTeacherIfMissing() {
        if (userAccountRepository.existsByEmail("admin@school.com")) {
            return;
        }

        UserAccount account = new UserAccount();
        account.setDisplayName("Default Teacher Admin");
        account.setEmail("admin@school.com");
        account.setPasswordHash(passwordService.hash("admin123"));
        account.setRole(Role.TEACHER);

        TeacherProfile teacher = new TeacherProfile();
        teacher.setFullName("Default Teacher Admin");
        teacher.setSubject("Administration");
        teacher.setUserAccount(account);

        teacherProfileRepository.save(teacher);
    }

    private void createStudentIfMissing() {
        if (userAccountRepository.existsByEmail("student@school.com")) {
            return;
        }

        UserAccount account = new UserAccount();
        account.setDisplayName("Demo Student");
        account.setEmail("student@school.com");
        account.setPasswordHash(passwordService.hash("student123"));
        account.setRole(Role.STUDENT);

        StudentProfile student = new StudentProfile();
        student.setFullName("Demo Student");
        student.setGrade("Grade 8");
        student.setGuardianPhone("9999999999");
        student.setUserAccount(account);

        studentProfileRepository.save(student);
    }
}
