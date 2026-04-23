package com.bankapp.model;

public class Account {
    private int accountId;        // unique ID in 'accounts' table (primary key)
    private int userId;           // foreign key linking to users.user_id
    private String accountNumber; // unique 10- or 12-digit account number
    private String accountType;   // Savings / Current
    private double balance;       // current account balance

    //Default constructor
    public Account() {}

    //Parameterized constructor
    public Account(int accountId, int userId, String accountNumber, String accountType, double balance) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
    }

    //Alternate constructor (if you don’t have accountId during creation)
    public Account(int userId, String accountNumber, String accountType, double balance) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
    }

    //Getters
    public int getAccountId() { return accountId; }
    public int getUserId() { return userId; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }

    //Setters
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setBalance(double balance) { this.balance = balance; }

    //for debugging/logging
    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                '}';
    }
}
