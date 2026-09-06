package com.onlineshoppingcart.controller;

import com.onlineshoppingcart.dto.LoginRequest;
import com.onlineshoppingcart.dto.RegisterRequest;
import com.onlineshoppingcart.entity.User;
import com.onlineshoppingcart.security.JwtService;
import com.onlineshoppingcart.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return "User registered successfully: " + user.getEmail();
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {

        User user = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }
}
