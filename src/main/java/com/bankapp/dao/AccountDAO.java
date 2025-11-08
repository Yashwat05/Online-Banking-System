package com.bankapp.dao;

import java.sql.*;
import java.util.*;
import com.bankapp.config.DBConnection;
import com.bankapp.model.Account;

public class AccountDAO {

    // No persistent connection field

    // Get all accounts for a specific user
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_id, account_number, account_type, balance FROM accounts WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setAccountType(rs.getString("account_type"));
                account.setBalance(rs.getDouble("balance"));
                account.setUserId(userId);
                accounts.add(account);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    // Secure balance check (user ownership)
    public double getBalance(int userId, String accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE user_id = ? AND account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Get balance by account number (used for receiver side)
    public double getBalance(String accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Update balance (user-specific)
    public boolean updateBalance(int userId, String accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ? AND account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);
            ps.setString(3, accountNumber);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Transaction-safe balance retrieval
    public double getBalanceForUpdate(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
            else throw new SQLException("Account not found: " + accountNumber);
        }
    }

    // Transaction-safe balance update
    public boolean updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            return ps.executeUpdate() > 0;
        }
    }

    // Create new account for existing user
    public boolean createAccountForExistingUser(int userId, String accountNumber, String accountType, double openingBalance) {
        String sql = "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, accountNumber);
            ps.setString(3, accountType);
            ps.setDouble(4, openingBalance);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
