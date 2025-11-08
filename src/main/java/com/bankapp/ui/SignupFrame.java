package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import com.bankapp.dao.UserDAO;
import com.bankapp.model.User;
import com.formdev.flatlaf.FlatLightLaf;

public class SignupFrame extends JFrame {

    private JTextField nameField, emailField, passwordField, accNumField, accTypeField, balanceField;
    private final UserDAO userDAO;

    public SignupFrame() {
        // Apply FlatLaf theme before building the UI
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        userDAO = new UserDAO();

        setTitle("User Registration");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create Your Bank Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 118, 210));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        JLabel nameLbl = new JLabel("Full Name:");
        JLabel emailLbl = new JLabel("Email:");
        JLabel passLbl = new JLabel("Password:");
        JLabel accNumLbl = new JLabel("Account Number:");
        JLabel accTypeLbl = new JLabel("Account Type (SAVINGS/CURRENT):");
        JLabel balLbl = new JLabel("Initial Balance:");

        nameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JTextField();
        accNumField = new JTextField();
        accTypeField = new JTextField();
        balanceField = new JTextField();

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        nameLbl.setFont(labelFont);
        emailLbl.setFont(labelFont);
        passLbl.setFont(labelFont);
        accNumLbl.setFont(labelFont);
        accTypeLbl.setFont(labelFont);
        balLbl.setFont(labelFont);

        // Add fields neatly using GridBag
        addField(gbc, nameLbl, nameField, 1);
        addField(gbc, emailLbl, emailField, 2);
        addField(gbc, passLbl, passwordField, 3);
        addField(gbc, accNumLbl, accNumField, 4);
        addField(gbc, accTypeLbl, accTypeField, 5);
        addField(gbc, balLbl, balanceField, 6);

        JButton registerBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");

        registerBtn.setBackground(new Color(25, 118, 210));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setFocusPainted(false);

        cancelBtn.setBackground(new Color(220, 53, 69));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setFocusPainted(false);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        add(registerBtn, gbc);

        gbc.gridx = 1;
        add(cancelBtn, gbc);

        registerBtn.addActionListener(e -> handleRegister());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void addField(GridBagConstraints gbc, JLabel label, JTextField field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        add(label, gbc);

        gbc.gridx = 1;
        add(field, gbc);
    }

    private void handleRegister() {
        try {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String accNum = accNumField.getText();
            String accType = accTypeField.getText().toUpperCase();
            double balance = Double.parseDouble(balanceField.getText());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || accNum.isEmpty() || accType.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }

            User user = new User(name, email, password);
            boolean registered = userDAO.registerUserWithAccount(user, accNum, accType, balance);

            if (registered) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed! Try again.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid number for balance!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignupFrame().setVisible(true));
    }
}
