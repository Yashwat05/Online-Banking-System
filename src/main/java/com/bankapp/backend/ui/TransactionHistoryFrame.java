package com.bankapp.backend.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import com.bankapp.backend.model.User;
import com.bankapp.backend.model.Transaction;
import com.bankapp.backend.api.TransactionAPI;

public class TransactionHistoryFrame extends JFrame {

    private final User currentUser;
    private final String token;
    private final TransactionAPI transactionAPI;

    public TransactionHistoryFrame(User user, String token) {
        this.currentUser = user;
        this.token = token;
        this.transactionAPI = new TransactionAPI();

        setTitle("Transaction History");
        setSize(500, 400);
        setLocationRelativeTo(null);

        JTextArea area = new JTextArea();
        area.setEditable(false);

        List<Transaction> txns =
                transactionAPI.getTransactions(currentUser.getUserId());

        for (Transaction t : txns) {
            area.append(
                    t.getTxnType() + " | ₹" + t.getAmount() +
                            " | From: " + t.getFromAccount() +
                            " | To: " + t.getToAccount() + "\n"
            );
        }

        add(new JScrollPane(area));
        setVisible(true);
    }
}