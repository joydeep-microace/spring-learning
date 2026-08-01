package com.example.spring_learning.config;

import com.example.spring_learning.auth.AuthenticatedUser;
import com.example.spring_learning.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /*
     * This filter runs before controllers.
     * It allows login without a token and protects every other endpoint.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = readToken(request);
        if (token == null || token.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing JWT. Login first, then send Cookie: SCHOOL_JWT=<token>");
            return;
        }

        try {
            AuthenticatedUser user = jwtService.verifyToken(token);
            request.setAttribute("authUser", user);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || "/auth/login".equals(path)
                || "/error".equals(path);
    }

    private String readToken(HttpServletRequest request) {
        /*
         * Main way for your Postman testing:
         * Cookie: SCHOOL_JWT=<token>
         */
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> JwtService.COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        /*
         * Optional fallback:
         * Authorization: Bearer <token>
         */
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return null;
    }
}
