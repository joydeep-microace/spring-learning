package com.example.spring_learning.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Put this annotation on a controller class or controller method when only
 * specific roles should be allowed to call that API.
 *
 * Example:
 * @RequireRole(Role.TEACHER)
 * public StudentResponse createStudent(...) { ... }
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Role[] value();
}
