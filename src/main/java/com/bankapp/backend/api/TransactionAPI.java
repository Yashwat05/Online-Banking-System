package com.bankapp.backend.api;

import com.bankapp.backend.dao.TransactionDAO;
import com.bankapp.backend.model.Transaction;

import java.util.List;

public class TransactionAPI {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> getTransactions(int userId) {
        return transactionDAO.getUserTransactions(userId);
    }
}