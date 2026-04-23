package com.bankapp.backend.services;

import org.springframework.stereotype.Service;

import com.bankapp.backend.dao.UserDAO;
import com.bankapp.backend.dto.LoginRequest;
import com.bankapp.backend.dto.OTPRequest;
import com.bankapp.backend.model.User;
import com.bankapp.backend.util.*;
import com.bankapp.backend.security.JwtUtil;

@Service
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(LoginRequest req) {

        String email = req.email.trim().toLowerCase();

        User user = userDAO.login(email, req.password);

        if (user == null) return null;

        String otp = OTPGenerator.generateOTP();

        OTPService.storeOTP(email, otp);

        EmailSender.sendOTP(email, otp);

        return user;
    }

    public String verifyOTP(OTPRequest req) {

        String email = req.email.trim().toLowerCase();

        boolean valid = OTPService.verifyOTP(email, req.otp);

        if (!valid) return null;

        return JwtUtil.generateToken(email);
    }
}