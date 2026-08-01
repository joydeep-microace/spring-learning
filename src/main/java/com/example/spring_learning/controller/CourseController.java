package com.example.spring_learning.controller;

import com.example.spring_learning.auth.AuthenticatedUser;
import com.example.spring_learning.auth.RequireRole;
import com.example.spring_learning.auth.Role;
import com.example.spring_learning.dto.CourseResponse;
import com.example.spring_learning.dto.CreateCourseRequest;
import com.example.spring_learning.dto.MessageResponse;
import com.example.spring_learning.entity.Course;
import com.example.spring_learning.entity.StudentProfile;
import com.example.spring_learning.entity.TeacherProfile;
import com.example.spring_learning.repository.CourseRepository;
import com.example.spring_learning.repository.StudentProfileRepository;
import com.example.spring_learning.repository.TeacherProfileRepository;
import com.example.spring_learning.service.SchoolMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseRepository courseRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SchoolMapper schoolMapper;

    public CourseController(
            CourseRepository courseRepository,
            TeacherProfileRepository teacherProfileRepository,
            StudentProfileRepository studentProfileRepository,
            SchoolMapper schoolMapper
    ) {
        this.courseRepository = courseRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.schoolMapper = schoolMapper;
    }

    /*
     * Any logged-in user can list courses.
     * Teachers see the class roster in the response; students can also use /courses/my.
     */
    @GetMapping
    @RequireRole({Role.TEACHER, Role.STUDENT})
    public List<CourseResponse> getCourses() {
        return courseRepository.findAll().stream()
                .map(schoolMapper::toCourseResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(Role.TEACHER)
    public CourseResponse createCourse(@RequestBody CreateCourseRequest request) {
        TeacherProfile teacher = teacherProfileRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setTeacher(teacher);

        return schoolMapper.toCourseResponse(courseRepository.save(course));
    }

    @PostMapping("/{courseId}/students/{studentId}")
    @RequireRole(Role.TEACHER)
    public MessageResponse enrollStudent(@PathVariable String courseId, @PathVariable String studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        StudentProfile student = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        boolean alreadyEnrolled = course.getStudents().stream()
                .anyMatch(existingStudent -> existingStudent.getId().equals(student.getId()));
        if (!alreadyEnrolled) {
            course.getStudents().add(student);
            courseRepository.save(course);
        }

        return new MessageResponse("Student enrolled in course");
    }

    @GetMapping("/my")
    @RequireRole(Role.STUDENT)
    public List<CourseResponse> getMyCourses(@RequestAttribute("authUser") AuthenticatedUser user) {
        return courseRepository.findByStudentsUserAccountId(user.userId()).stream()
                .map(schoolMapper::toCourseResponse)
                .toList();
    }
}
