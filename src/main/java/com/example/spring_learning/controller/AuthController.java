package com.example.spring_learning.controller;

import com.example.spring_learning.auth.AuthRequest;
import com.example.spring_learning.auth.AuthResponse;
import com.example.spring_learning.auth.AuthenticatedUser;
import com.example.spring_learning.auth.JwtService;
import com.example.spring_learning.auth.PasswordService;
import com.example.spring_learning.entity.UserAccount;
import com.example.spring_learning.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserAccountRepository userAccountRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthController(
            UserAccountRepository userAccountRepository,
            PasswordService passwordService,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    /*
     * POST /api/auth/login
     * Body:
     * {
     *   "email": "admin@school.com",
     *   "password": "admin123"
     * }
     *
     * The response sets a cookie. In Postman you can also copy the token and send:
     * Cookie: SCHOOL_JWT=<token>
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpServletResponse response) {
        UserAccount account = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is wrong"));

        if (!account.isEnabled() || !passwordService.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is wrong");
        }

        AuthenticatedUser user = new AuthenticatedUser(
                account.getId(),
                account.getEmail(),
                account.getDisplayName(),
                account.getRole()
        );
        String token = jwtService.createToken(user);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.cookieValue(token));

        return new AuthResponse(
                "Login successful. Send the cookie on protected requests.",
                token,
                account.getRole().name(),
                account.getDisplayName(),
                "Cookie: " + JwtService.COOKIE_NAME + "=" + token
        );
    }

    /*
     * GET /api/auth/me
     * This proves that the JWT filter decoded the cookie and attached the user.
     */
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticatedUser me(@RequestAttribute("authUser") AuthenticatedUser user) {
        return user;
    }
}
