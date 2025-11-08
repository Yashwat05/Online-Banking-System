package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import com.bankapp.dao.UserDAO;
import com.bankapp.model.User;
import com.formdev.flatlaf.FlatLightLaf;
import com.bankapp.util.OTPGenerator;
import com.bankapp.util.EmailSender;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private final UserDAO userDAO;

    public LoginFrame() {
        // Apply FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        userDAO = new UserDAO();

        setTitle("Bank Login");
        setSize(450, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to MyBank", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 118, 210));

        JLabel emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");
        JButton exitBtn = new JButton("Exit");

        // Button styling
        loginBtn.setBackground(new Color(25, 118, 210));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        signupBtn.setBackground(new Color(240, 240, 240));
        signupBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        exitBtn.setBackground(new Color(255, 77, 77));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);

        // Layout arrangement
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(emailLabel, gbc);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(loginBtn, gbc);
        gbc.gridx = 1;
        add(signupBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(exitBtn, gbc);

        // Action listeners
        loginBtn.addActionListener(e -> handleLogin());
        signupBtn.addActionListener(e -> new SignupFrame().setVisible(true));
        exitBtn.addActionListener(e -> System.exit(0));
    }

    // Updated login logic with 2FA (Email OTP)
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.");
            return;
        }

        User user = userDAO.login(email, password);

        if (user != null) {
            // Step 1️ Generate OTP
            String otp = OTPGenerator.generateOTP();

            // Step 2️ Send OTP via Email
            boolean sent = EmailSender.sendOTP(user.getEmail(), otp);

            if (sent) {
                JOptionPane.showMessageDialog(this, "An OTP has been sent to your registered email.");

                // Step 3️ Open OTP verification window
                new VerifyOTPFrame(user, otp).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send OTP. Please check your internet connection or email settings.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
