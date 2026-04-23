package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import com.bankapp.model.User;
import com.bankapp.model.Transaction;
import com.bankapp.service.BankService;
import com.bankapp.dao.TransactionDAO;
import com.bankapp.util.EmailSender;
import com.bankapp.util.TransactionXMLExporter;

public class TransactionFrame extends JFrame {

    private JTextField fromField, toField, amountField;
    private final String type;
    private final BankService bankService;
    private final User currentUser;
    private final TransactionDAO transactionDAO;

    public TransactionFrame(User user, String type) {
        this.currentUser = user;
        this.type = type.toUpperCase();
        this.bankService = new BankService(user);
        this.transactionDAO = new TransactionDAO();

        setTitle(type + " Transaction");
        setSize(400, 300);
        setLayout(new GridLayout(5, 2, 10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel fromLabel = new JLabel("From Account:");
        JLabel toLabel = new JLabel("To Account:");
        JLabel amtLabel = new JLabel("Amount:");

        fromField = new JTextField();
        toField = new JTextField();
        amountField = new JTextField();

        JButton actionBtn = new JButton(type);
        JButton backBtn = new JButton("Back");

        add(fromLabel);
        add(fromField);

        if (type.equals("TRANSFER")) {
            add(toLabel);
            add(toField);
        } else {
            add(new JLabel());
            add(new JLabel());
        }

        add(amtLabel);
        add(amountField);
        add(actionBtn);
        add(backBtn);

        //Button actions
        actionBtn.addActionListener(e -> handleTransaction());
        backBtn.addActionListener(e -> dispose());
    }

    private void handleTransaction() {
        try {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            String amtText = amountField.getText().trim();

            if (from.isEmpty() || amtText.isEmpty() ||
                    (type.equals("TRANSFER") && to.isEmpty())) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in all required fields.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Amount must be greater than zero.",
                            "Invalid Amount",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid numeric amount.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = false;

            switch (type) {
                case "DEPOSIT":
                    success = bankService.deposit(from, amount);
                    break;

                case "WITHDRAW":
                    success = bankService.withdraw(from, amount);
                    break;

                case "TRANSFER":
                    if (from.equals(to)) {
                        JOptionPane.showMessageDialog(this,
                                "You cannot transfer to the same account!",
                                "Invalid Transfer",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    success = bankService.transfer(from, to, amount);
                    break;
            }

            if (success) {
                JOptionPane.showMessageDialog(this,
                        " " + type + " Successful!",
                        "Transaction Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Auto-export updated transactions
                List<Transaction> txns = transactionDAO.getUserTransactions(currentUser.getUserId());
                TransactionXMLExporter.exportToXML(
                        txns,
                        "exports/transactions_" + currentUser.getUserId() + ".xml"
                );

                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        " " + type + " Failed!",
                        "Transaction Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Transaction Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
