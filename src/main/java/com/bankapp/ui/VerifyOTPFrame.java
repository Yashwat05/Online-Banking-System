package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import com.bankapp.model.User;

public class VerifyOTPFrame extends JFrame {

    private final String correctOTP;
    private final User currentUser;
    private JTextField otpField;

    public VerifyOTPFrame(User user, String otp) {
        this.correctOTP = otp;
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
        if (entered.equals(correctOTP)) {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            new Dashboard(currentUser).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect OTP. Please try again.");
        }
    }
}
