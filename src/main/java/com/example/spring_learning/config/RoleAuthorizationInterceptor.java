package com.example.spring_learning.config;

import com.example.spring_learning.auth.AuthenticatedUser;
import com.example.spring_learning.auth.RequireRole;
import com.example.spring_learning.auth.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
    /*
     * This runs after JWT authentication and before the controller method.
     * It checks our custom @RequireRole annotation.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole methodRule = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequireRole classRule = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        RequireRole rule = methodRule != null ? methodRule : classRule;

        if (rule == null) {
            return true;
        }

        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute("authUser");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT was not authenticated");
            return false;
        }

        boolean allowed = Arrays.stream(rule.value())
                .map(Role::name)
                .anyMatch(role -> role.equals(user.role().name()));

        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your role cannot access this endpoint");
            return false;
        }

        return true;
    }
}
