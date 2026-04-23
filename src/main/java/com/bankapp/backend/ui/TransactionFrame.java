package com.bankapp.backend.ui;

import javax.swing.*;
import java.awt.*;

import com.bankapp.backend.model.User;
import com.bankapp.backend.api.AccountAPI;

public class TransactionFrame extends JFrame {

    private final User currentUser;
    private final String token; // future use
    private final String type;
    private final AccountAPI accountAPI;

    private JTextField accField;
    private JTextField amountField;
    private JTextField toAccField;

    public TransactionFrame(User user, String token, String type) {
        this.currentUser = user;
        this.token = token;
        this.type = type;

        this.accountAPI = new AccountAPI(user);

        setTitle(type + " Transaction");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("Account Number:"));
        accField = new JTextField();
        add(accField);

        if (type.equals("TRANSFER")) {
            add(new JLabel("To Account:"));
            toAccField = new JTextField();
            add(toAccField);
        }

        add(new JLabel("Amount:"));
        amountField = new JTextField();
        add(amountField);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> handleTransaction());

        add(new JLabel());
        add(submitBtn);

        setVisible(true);
    }

    private void handleTransaction() {

        String acc = accField.getText().trim();

        if (acc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter account number");
            return;
        }

        double amt;

        try {
            amt = Double.parseDouble(amountField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid amount");
            return;
        }

        boolean success = false;

        switch (type) {
            case "DEPOSIT":
                success = accountAPI.deposit(acc, amt);
                break;

            case "WITHDRAW":
                success = accountAPI.withdraw(acc, amt);
                break;

            case "TRANSFER":
                String to = toAccField.getText().trim();

                if (to.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Enter target account");
                    return;
                }

                success = accountAPI.transfer(acc, to, amt);
                break;
        }

        JOptionPane.showMessageDialog(this, success ? "Success" : "Failed");
        dispose();
    }
}