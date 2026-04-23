package com.bankapp.backend.api;

import com.bankapp.backend.services.BankService;
import com.bankapp.backend.model.User;

public class AccountAPI {

    private final BankService bankService;

    public AccountAPI(User user) {
        this.bankService = new BankService(user);
    }

    public double getBalance(String accountNumber) {
        return bankService.getBalance(accountNumber);
    }

    public boolean deposit(String accountNumber, double amount) {
        return bankService.deposit(accountNumber, amount);
    }

    public boolean withdraw(String accountNumber, double amount) {
        return bankService.withdraw(accountNumber, amount);
    }

    public boolean transfer(String from, String to, double amount) {
        return bankService.transfer(from, to, amount);
    }

    public boolean changePassword(int userId, String newPassword) {
        return bankService.changePassword(userId, newPassword);
    }
}