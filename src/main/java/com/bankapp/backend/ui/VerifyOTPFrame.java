package com.bankapp.backend.ui;

import javax.swing.*;
import java.awt.*;

import com.bankapp.backend.api.AuthAPI;
import com.bankapp.backend.model.User;

public class VerifyOTPFrame extends JFrame {

    private final User currentUser;
    private JTextField otpField;

    private final AuthAPI authAPI;

    public VerifyOTPFrame(User user) {
        this.currentUser = user;
        this.authAPI = new AuthAPI();

        setTitle("Two-Factor Authentication (2FA)");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Enter the OTP sent to your email:");
        otpField = new JTextField(10);
        JButton verifyButton = new JButton("Verify");

        verifyButton.addActionListener(e -> verifyOTP());

        setLayout(new FlowLayout());
        add(label);
        add(otpField);
        add(verifyButton);
    }

    private String token; // 🔥 store locally

    private void verifyOTP() {

        String entered = otpField.getText().trim();

        if (entered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter OTP.");
            return;
        }

        com.bankapp.backend.dto.OTPRequest req =
                new com.bankapp.backend.dto.OTPRequest();

        req.email = currentUser.getEmail().trim().toLowerCase();
        req.otp = entered.trim();

        String token = authAPI.verifyOTP(req);

        if (token != null) {

            JOptionPane.showMessageDialog(this, "Login Successful!");

            new Dashboard(currentUser, token).setVisible(true);
            dispose();

        } else {

            JOptionPane.showMessageDialog(this,
                    "Invalid / expired OTP OR too many attempts.");
        }
    }
}
