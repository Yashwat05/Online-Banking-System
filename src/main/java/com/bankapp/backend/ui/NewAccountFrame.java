package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import com.bankapp.dao.AccountDAO;
import com.bankapp.model.User;

public class NewAccountFrame extends JFrame {

    private final User currentUser;
    private JTextField accountNumberField, balanceField;
    private JComboBox<String> accountTypeBox;
    private final AccountDAO accountDAO = new AccountDAO();

    public NewAccountFrame(User user) {
        this.currentUser = user;

        setTitle("Open New Account");
        setSize(400, 300);
        setLayout(new GridLayout(5, 2, 10, 10));
        setLocationRelativeTo(null);

        JLabel accNumLabel = new JLabel("Account Number:");
        JLabel accTypeLabel = new JLabel("Account Type:");
        JLabel balanceLabel = new JLabel("Opening Balance:");

        accountNumberField = new JTextField();
        accountTypeBox = new JComboBox<>(new String[]{"Savings", "Current"});
        balanceField = new JTextField("0.0");

        JButton createBtn = new JButton("Create Account");
        createBtn.addActionListener(e -> createAccount());

        add(accNumLabel); add(accountNumberField);
        add(accTypeLabel); add(accountTypeBox);
        add(balanceLabel); add(balanceField);
        add(new JLabel()); add(createBtn);
    }

    private void createAccount() {
        try {
            String accNum = accountNumberField.getText().trim();
            String accType = (String) accountTypeBox.getSelectedItem();
            double balance = Double.parseDouble(balanceField.getText().trim());

            boolean created = accountDAO.createAccountForExistingUser(
                    currentUser.getUserId(), accNum, accType, balance
            );

            if (created) {
                JOptionPane.showMessageDialog(this, "Account created successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error creating account. Try again.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }
}
