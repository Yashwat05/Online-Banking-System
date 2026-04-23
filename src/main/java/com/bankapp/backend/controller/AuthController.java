package com.bankapp.backend.controller;

import org.springframework.web.bind.annotation.*;
import com.bankapp.service.AuthService;
import com.bankapp.dto.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/verify-otp")
    public String verify(@RequestBody OTPRequest req) {
        return authService.verifyOTP(req);
    }
}