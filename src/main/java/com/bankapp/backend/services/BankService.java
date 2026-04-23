package com.bankapp.backend.services;

import com.bankapp.backend.dao.AccountDAO;
import com.bankapp.backend.dao.TransactionDAO;
import com.bankapp.backend.dao.UserDAO;
import com.bankapp.backend.model.Transaction;
import com.bankapp.backend.model.User;
import com.bankapp.backend.config.DBConnection;
import com.bankapp.backend.util.EmailSender;
import com.bankapp.backend.util.LockManager;
import com.bankapp.backend.util.TransactionXMLExporter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BankService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    private final User currentUser;

    public BankService(User currentUser) {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.userDAO = new UserDAO();
        this.currentUser = currentUser;
    }

    // =========================
    // 🔥 BALANCE (WITH CACHE)
    // =========================
    public double getBalance(String accountNumber) {

        validateAccount(accountNumber);

        Double cached = CacheService.getBalance(accountNumber);
        if (cached != null) {
            return cached;
        }

        double balance = accountDAO.getBalance(currentUser.getUserId(), accountNumber);

        CacheService.cacheBalance(accountNumber, balance);

        return balance;
    }

    // =========================
    // 💰 DEPOSIT
    // =========================
    public boolean deposit(String accountNumber, double amount) {

        validateAccount(accountNumber);
        validateAmount(amount);

        double currentBalance = accountDAO.getBalance(currentUser.getUserId(), accountNumber);

        if (currentBalance == 0.0) {
            return false;
        }

        double newBalance = currentBalance + amount;

        boolean success = accountDAO.updateBalance(
                currentUser.getUserId(), accountNumber, newBalance
        );

        if (success) {

            transactionDAO.recordTransaction(
                    new Transaction(currentUser.getUserId(), null, accountNumber, amount, "DEPOSIT")
            );

            CacheService.invalidateBalance(accountNumber);

            EmailSender.sendDepositAlert(
                    currentUser.getEmail(),
                    currentUser.getName(),
                    amount,
                    newBalance
            );

            exportUserTransactions();
        }

        return success;
    }

    // =========================
    // 💸 WITHDRAW
    // =========================
    public boolean withdraw(String accountNumber, double amount) {

        validateAccount(accountNumber);
        validateAmount(amount);

        LockManager.acquireLocks(accountNumber, null);

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            double currentBalance =
                    accountDAO.getBalance(currentUser.getUserId(), accountNumber);

            if (currentBalance < amount) {
                conn.rollback();
                return false;
            }

            double lockedBalance =
                    accountDAO.getBalanceForUpdate(conn, accountNumber);

            if (lockedBalance < amount) {
                conn.rollback();
                return false;
            }

            boolean updated =
                    accountDAO.updateBalance(conn, accountNumber, lockedBalance - amount);

            if (!updated) {
                conn.rollback();
                return false;
            }

            transactionDAO.recordTransaction(
                    conn,
                    new Transaction(currentUser.getUserId(), accountNumber, null, amount, "WITHDRAWAL")
            );

            conn.commit();

            CacheService.invalidateBalance(accountNumber);

            EmailSender.sendWithdrawalAlert(
                    currentUser.getEmail(),
                    currentUser.getName(),
                    amount,
                    lockedBalance - amount
            );

            exportUserTransactions();

            return true;

        } catch (SQLException e) {
            System.out.println("Withdraw failed: " + e.getMessage());
            return false;
        } finally {
            LockManager.releaseLocks(accountNumber, null);
        }
    }

    // =========================
    // 🔁 TRANSFER (FIXED SECURITY)
    // =========================
    public boolean transfer(String fromAccount, String toAccount, double amount) {

        validateAccount(fromAccount);
        validateAccount(toAccount);
        validateAmount(amount);

        LockManager.acquireLocks(fromAccount, toAccount);

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            // 🔥 SECURITY FIX: verify ownership
            double ownedBalance =
                    accountDAO.getBalance(currentUser.getUserId(), fromAccount);

            if (ownedBalance == 0.0) {
                conn.rollback();
                return false;
            }

            double fromBalance =
                    accountDAO.getBalanceForUpdate(conn, fromAccount);

            double toBalance =
                    accountDAO.getBalanceForUpdate(conn, toAccount);

            if (fromBalance < amount) {
                conn.rollback();
                return false;
            }

            boolean debit =
                    accountDAO.updateBalance(conn, fromAccount, fromBalance - amount);

            boolean credit =
                    accountDAO.updateBalance(conn, toAccount, toBalance + amount);

            if (debit && credit) {

                transactionDAO.recordTransaction(
                        conn,
                        new Transaction(currentUser.getUserId(), fromAccount, toAccount, amount, "TRANSFER")
                );

                conn.commit();

                CacheService.invalidateBalance(fromAccount);
                CacheService.invalidateBalance(toAccount);

                exportUserTransactions();

                return true;

            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Transfer failed: " + e.getMessage());
            return false;
        } finally {
            LockManager.releaseLocks(fromAccount, toAccount);
        }
    }

    // =========================
    // 🔐 CHANGE PASSWORD
    // =========================
    public boolean changePassword(int userId, String newPassword) {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        return userDAO.updatePassword(userId, newPassword);
    }

    // =========================
    // EXPORT
    // =========================
    private void exportUserTransactions() {

        List<Transaction> txns =
                transactionDAO.getUserTransactions(currentUser.getUserId());

        TransactionXMLExporter.exportToXML(
                txns,
                "exports/transactions_" + currentUser.getUserId() + ".xml"
        );
    }

    // =========================
    // 🔒 VALIDATION HELPERS
    // =========================
    private void validateAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid account number");
        }
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}