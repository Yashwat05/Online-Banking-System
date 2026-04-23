package com.bankapp.backend.ui;

import javax.swing.*;
import java.awt.*;

import com.bankapp.backend.api.AuthAPI;
import com.bankapp.backend.model.User;
import com.formdev.flatlaf.FlatLightLaf;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    // 🔥 Use API instead of DAO
    private final AuthAPI authAPI;

    public LoginFrame() {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.authAPI = new AuthAPI();

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

        loginBtn.setBackground(new Color(25, 118, 210));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        signupBtn.setBackground(new Color(240, 240, 240));
        signupBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        exitBtn.setBackground(new Color(255, 77, 77));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);

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

        loginBtn.addActionListener(e -> {
            loginBtn.setEnabled(false);
            handleLogin();
        });
        signupBtn.addActionListener(e -> new SignupFrame().setVisible(true));
        exitBtn.addActionListener(e -> System.exit(0));
    }

    // 🔥 CLEAN LOGIN FLOW
    private void handleLogin() {

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.");
            return;
        }

        // 🔥 CREATE DTO
        com.bankapp.backend.dto.LoginRequest req =
                new com.bankapp.backend.dto.LoginRequest();
        req.email = email;
        req.password = password;

        // 🔥 CALL API WITH DTO
        User user = authAPI.login(req);

        if (user != null) {

            JOptionPane.showMessageDialog(this,
                    "OTP sent to your email.");

            new VerifyOTPFrame(user).setVisible(true);
            dispose();

        } else {

            JOptionPane.showMessageDialog(this,
                    "Invalid email or password!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}