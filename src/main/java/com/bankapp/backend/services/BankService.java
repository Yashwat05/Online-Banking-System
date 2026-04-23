package com.bankapp.service;

import com.bankapp.dao.AccountDAO;
import com.bankapp.dao.TransactionDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.model.Transaction;
import com.bankapp.model.User;
import com.bankapp.config.DBConnection;
import com.bankapp.util.RedisUtil;
import com.bankapp.util.EmailSender;
import com.bankapp.util.LockManager;
import com.bankapp.util.TransactionXMLExporter;

import redis.clients.jedis.Jedis;

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
    // 🔥 REDIS BALANCE CACHE
    // =========================
    public double getBalanceWithCache(String accountNumber) {
        String key = "balance:" + accountNumber;

        try (Jedis jedis = RedisUtil.getConnection()) {

            String cached = jedis.get(key);
            if (cached != null) {
                return Double.parseDouble(cached);
            }

            double balance = accountDAO.getBalance(currentUser.getUserId(), accountNumber);

            jedis.setex(key, 60, String.valueOf(balance)); // cache 60 sec

            return balance;
        }
    }

    // =========================
    // 💰 DEPOSIT
    // =========================
    public boolean deposit(String accountNumber, double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        double currentBalance = accountDAO.getBalance(currentUser.getUserId(), accountNumber);

        if (currentBalance == 0.0) {
            System.out.println("Invalid account");
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

            // 🔥 REDIS INVALIDATION
            try (Jedis jedis = RedisUtil.getConnection()) {
                jedis.del("balance:" + accountNumber);
            }

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

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        LockManager.acquireLocks(accountNumber, null);

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            double currentBalance = accountDAO.getBalance(
                    currentUser.getUserId(), accountNumber
            );

            if (currentBalance < amount) {
                conn.rollback();
                return false;
            }

            double lockedBalance = accountDAO.getBalanceForUpdate(conn, accountNumber);

            if (lockedBalance < amount) {
                conn.rollback();
                return false;
            }

            boolean updated = accountDAO.updateBalance(
                    conn, accountNumber, lockedBalance - amount
            );

            if (!updated) {
                conn.rollback();
                return false;
            }

            transactionDAO.recordTransaction(
                    conn,
                    new Transaction(currentUser.getUserId(), accountNumber, null, amount, "WITHDRAWAL")
            );

            conn.commit();

            // 🔥 REDIS INVALIDATION
            try (Jedis jedis = RedisUtil.getConnection()) {
                jedis.del("balance:" + accountNumber);
            }

            EmailSender.sendWithdrawalAlert(
                    currentUser.getEmail(),
                    currentUser.getName(),
                    amount,
                    lockedBalance - amount
            );

            exportUserTransactions();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.releaseLocks(accountNumber, null);
        }
    }

    // =========================
    // 🔁 TRANSFER
    // =========================
    public boolean transfer(String fromAccount, String toAccount, double amount) {

        if (amount <= 0)
            return false;

        LockManager.acquireLocks(fromAccount, toAccount);

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            double fromBalance = accountDAO.getBalanceForUpdate(conn, fromAccount);
            double toBalance = accountDAO.getBalanceForUpdate(conn, toAccount);

            if (fromBalance < amount) {
                conn.rollback();
                return false;
            }

            boolean debit = accountDAO.updateBalance(conn, fromAccount, fromBalance - amount);
            boolean credit = accountDAO.updateBalance(conn, toAccount, toBalance + amount);

            if (debit && credit) {

                transactionDAO.recordTransaction(
                        conn,
                        new Transaction(currentUser.getUserId(), fromAccount, toAccount, amount, "TRANSFER")
                );

                conn.commit();

                // 🔥 REDIS INVALIDATION (BOTH ACCOUNTS)
                try (Jedis jedis = RedisUtil.getConnection()) {
                    jedis.del("balance:" + fromAccount);
                    jedis.del("balance:" + toAccount);
                }

                exportUserTransactions();

                return true;

            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.releaseLocks(fromAccount, toAccount);
        }
    }

    // =========================
    // EXPORT
    // =========================
    private void exportUserTransactions() {
        List<Transaction> txns = transactionDAO.getUserTransactions(currentUser.getUserId());
        TransactionXMLExporter.exportToXML(
                txns,
                "exports/transactions_" + currentUser.getUserId() + ".xml"
        );
    }
}