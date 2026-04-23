package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import com.bankapp.model.User;
import com.bankapp.service.OTPService;

public class VerifyOTPFrame extends JFrame {

    private final User currentUser;
    private JTextField otpField;

    public VerifyOTPFrame(User user) {
        this.currentUser = user;

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

    private void verifyOTP() {

        String entered = otpField.getText().trim();

        if (entered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter OTP.");
            return;
        }

        // 🔥 REDIS-BASED VERIFICATION
        boolean isValid = OTPService.verifyOTP(currentUser.getEmail(), entered);

        if (isValid)
        {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            new Dashboard(currentUser).setVisible(true);
            dispose();
        }
        else
        {
            JOptionPane.showMessageDialog(this,
                    "Invalid / expired OTP OR too many attempts.");
        }
    }
}