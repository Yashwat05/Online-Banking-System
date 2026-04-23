package com.bankapp.backend.api;

import com.bankapp.backend.services.AuthService;
import com.bankapp.backend.dto.LoginRequest;
import com.bankapp.backend.dto.OTPRequest;
import com.bankapp.backend.model.User;

public class AuthAPI {

    private final AuthService authService = new AuthService();

    public User login(LoginRequest req) {
        return authService.login(req);
    }

    public String verifyOTP(OTPRequest req) {
        return authService.verifyOTP(req);
    }
}