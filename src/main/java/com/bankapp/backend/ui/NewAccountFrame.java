package com.bankapp.backend.ui;

import javax.swing.*;
import java.awt.*;

import com.bankapp.backend.model.User;
import com.bankapp.backend.dao.AccountDAO;

public class NewAccountFrame extends JFrame {

    private final User currentUser;
    private final String token; // 🔥 carry token
    private final AccountDAO accountDAO = new AccountDAO();

    private JComboBox<String> typeBox;

    public NewAccountFrame(User user, String token) {
        this.currentUser = user;
        this.token = token;

        setTitle("Open New Account");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Account Type:"));
        typeBox = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});
        add(typeBox);

        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> createAccount());

        add(new JLabel());
        add(createBtn);

        setVisible(true);
    }

    private void createAccount() {
        String type = (String) typeBox.getSelectedItem();

        boolean success =
                accountDAO.createAccount(currentUser.getUserId(), type);

        JOptionPane.showMessageDialog(this,
                success ? "Account created!" : "Failed to create account");

        dispose();
    }
}