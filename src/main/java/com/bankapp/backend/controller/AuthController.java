package com.bankapp.backend.controller;

import org.springframework.web.bind.annotation.*;
import com.bankapp.backend.services.AuthService;
import com.bankapp.backend.dto.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = new AuthService();

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {
        return authService.login(req) != null ? "OTP sent" : "Invalid";
    }

    @PostMapping("/verify")
    public String verify(@RequestBody OTPRequest req) {
        return authService.verifyOTP(req);
    }
}