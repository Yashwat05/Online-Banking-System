package com.bankapp.service;

import com.bankapp.dao.AccountDAO;
import com.bankapp.dao.TransactionDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.model.Transaction;
import com.bankapp.model.User;
import com.bankapp.config.DBConnection;
import com.bankapp.util.EmailSender;
import com.bankapp.util.LockManager;
import com.bankapp.util.TransactionXMLExporter;

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

    // Deposit
    public boolean deposit(String accountNumber, double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposited amount must be positive.");

        double currentBalance = accountDAO.getBalance(currentUser.getUserId(), accountNumber);
        if (currentBalance == 0.0) {
            System.out.println("Unauthorized or invalid account.");
            return false;
        }

        double newBalance = currentBalance + amount;
        boolean success = accountDAO.updateBalance(currentUser.getUserId(), accountNumber, newBalance);

        if (success) {
            transactionDAO.recordTransaction(
                    new Transaction(currentUser.getUserId(), null, accountNumber, amount, "DEPOSIT")
            );
            System.out.println("Deposit successful. New balance: ₹" + newBalance);
            EmailSender.sendDepositAlert(currentUser.getEmail(), currentUser.getName(), amount, newBalance);
            exportUserTransactions();
        }
        return success;
    }

    // Withdraw
    public boolean withdraw(String accountNumber, double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawn amount must be positive.");

        // Acquire lock for the single account
        LockManager.acquireLocks(accountNumber, null);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // verify ownership using secure method
            double currentBalance = accountDAO.getBalance(currentUser.getUserId(), accountNumber);
            if (currentBalance == 0.0) {
                System.out.println("Unauthorized or invalid account.");
                conn.rollback();
                return false;
            }
            if (currentBalance < amount) {
                System.out.println("Insufficient balance.");
                conn.rollback();
                return false;
            }

            double newBalance = currentBalance - amount;

            // Use transaction-safe methods (FOR UPDATE already used in getBalanceForUpdate)
            double lockedBalance = accountDAO.getBalanceForUpdate(conn, accountNumber);
            if (lockedBalance < amount) {
                conn.rollback();
                System.out.println("Insufficient after lock — rolled back.");
                return false;
            }

            boolean updateOk = accountDAO.updateBalance(conn, accountNumber, lockedBalance - amount);
            if (!updateOk) {
                conn.rollback();
                System.out.println("Update failed — rolled back.");
                return false;
            }

            transactionDAO.recordTransaction(conn,
                    new Transaction(currentUser.getUserId(), accountNumber, null, amount, "WITHDRAWAL"));

            conn.commit();

            System.out.println("Withdrawal successful. Remaining balance: ₹" + (lockedBalance - amount));
            EmailSender.sendWithdrawalAlert(currentUser.getEmail(), currentUser.getName(), amount, lockedBalance - amount);
            exportUserTransactions();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // release the app-level lock
            LockManager.releaseLocks(accountNumber, null);
        }
    }

    // Transfer with atomic transaction and dual notifications
    public boolean transfer(String fromAccount, String toAccount, double amount) {
        if (amount <= 0) {
            System.out.println("Transfer amount must be positive.");
            return false;
        }

        // Acquire both locks in a deadlock-safe order
        LockManager.acquireLocks(fromAccount, toAccount);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            double verifyOwner = accountDAO.getBalance(currentUser.getUserId(), fromAccount);
            if (verifyOwner == 0.0) {
                System.out.println("Unauthorized or invalid 'from' account.");
                conn.rollback();
                return false;
            }

            // Lock rows at DB level
            double fromBalance = accountDAO.getBalanceForUpdate(conn, fromAccount);
            double toBalance = accountDAO.getBalanceForUpdate(conn, toAccount);

            if (fromBalance < amount) {
                transactionDAO.recordTransaction(conn,
                        new Transaction(currentUser.getUserId(), fromAccount, toAccount, amount, "FAILED_TRANSFER"));
                conn.rollback();
                EmailSender.sendTransferFailure(currentUser.getEmail(), currentUser.getName(),
                        fromAccount, toAccount, amount);
                System.out.println("Insufficient funds — transfer rolled back.");
                return false;
            }

            boolean debitOK = accountDAO.updateBalance(conn, fromAccount, fromBalance - amount);
            boolean creditOK = accountDAO.updateBalance(conn, toAccount, toBalance + amount);

            if (debitOK && creditOK) {
                transactionDAO.recordTransaction(conn,
                        new Transaction(currentUser.getUserId(), fromAccount, toAccount, amount, "TRANSFER"));
                conn.commit();

                System.out.println("Transfer successful and committed!");

                // notifications (can run async later)
                double senderNewBalance = accountDAO.getBalance(currentUser.getUserId(), fromAccount);
                double receiverNewBalance = accountDAO.getBalance(toAccount);

                User sender = currentUser;
                User receiver = userDAO.getUserByAccount(toAccount);

                if (sender != null)
                    EmailSender.sendDebitAlert(sender.getEmail(), sender.getName(),
                            fromAccount, toAccount, amount, senderNewBalance);

                if (receiver != null)
                    EmailSender.sendCreditAlert(receiver.getEmail(), receiver.getName(),
                            toAccount, fromAccount, amount, receiverNewBalance);

                exportUserTransactions();
                return true;
            } else {
                conn.rollback();
                System.out.println("Transfer failed — rolled back.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // release both locks
            LockManager.releaseLocks(fromAccount, toAccount);
        }
    }

    // Check balance
    public double checkBalance(String accountNumber) {
        return accountDAO.getBalance(currentUser.getUserId(), accountNumber);
    }

    // Change password
    public boolean changePassword(int userId, String newPassword) {
        boolean updated = userDAO.updatePassword(userId, newPassword);
        if (updated) {
            currentUser.setPassword(newPassword);
            EmailSender.sendPasswordChangeAlert(currentUser.getEmail(), currentUser.getName());
        }
        return updated;
    }

    // Export XML after transaction
    private void exportUserTransactions() {
        try {
            List<Transaction> txns = transactionDAO.getUserTransactions(currentUser.getUserId());
            String filePath = "exports/transactions_" + currentUser.getUserId() + ".xml";
            boolean ok = TransactionXMLExporter.exportToXML(txns, filePath);
            System.out.println(ok ? "Transactions exported to " + filePath : "XML export failed");
        } catch (Exception e) {
            System.out.println("XML export error: " + e.getMessage());
        }
    }
}
