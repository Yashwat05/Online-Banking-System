package com.bankapp.model;

import java.sql.Timestamp;

public class Transaction {
    private int userId;
    private String fromAccount;
    private String toAccount;
    private double amount;
    private String txnType;
    private Timestamp txnDate;

    // Default constructor
    public Transaction() {}

    // Constructor for creating a new transaction
    public Transaction(int userId, String fromAccount, String toAccount, double amount, String txnType) {
        this.userId = userId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.txnType = txnType;
    }

    // Constructor for reading from DB (includes txnDate)
    public Transaction(int userId, String fromAccount, String toAccount, double amount, String txnType, Timestamp txnDate) {
        this.userId = userId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.txnType = txnType;
        this.txnDate = txnDate;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public double getAmount() { return amount; }
    public String getTxnType() { return txnType; }
    public Timestamp getTxnDate() { return txnDate; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public void setTxnDate(Timestamp txnDate) { this.txnDate = txnDate; }
}
