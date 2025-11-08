package com.bankapp.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.bankapp.config.DBConnection;
import com.bankapp.model.Transaction;

public class TransactionDAO {

    // Record transaction (autonomous version)
    public boolean recordTransaction(Transaction txn) {
        String sql = "INSERT INTO transactions(user_id, from_account, to_account, amount, txn_type, txn_date) VALUES(?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, txn.getUserId());
            ps.setString(2, txn.getFromAccount());
            ps.setString(3, txn.getToAccount());
            ps.setDouble(4, txn.getAmount());
            ps.setString(5, txn.getTxnType());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Record transaction within an existing transaction (atomic)
    public boolean recordTransaction(Connection conn, Transaction txn) throws SQLException {
        String sql = "INSERT INTO transactions(user_id, from_account, to_account, amount, txn_type, txn_date) VALUES(?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, txn.getUserId());
            ps.setString(2, txn.getFromAccount());
            ps.setString(3, txn.getToAccount());
            ps.setDouble(4, txn.getAmount());
            ps.setString(5, txn.getTxnType());
            return ps.executeUpdate() > 0;
        }
    }

    // Retrieve all transactions for a user
    public List<Transaction> getUserTransactions(int userId) {
        List<Transaction> txnList = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY txn_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                txnList.add(new Transaction(
                        rs.getInt("user_id"),
                        rs.getString("from_account"),
                        rs.getString("to_account"),
                        rs.getDouble("amount"),
                        rs.getString("txn_type"),
                        rs.getTimestamp("txn_date")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txnList;
    }

    // Retrieve transactions with filters
    public List<Transaction> getFilteredTransactions(
            int userId,
            String type,
            String startDate,
            String endDate,
            Double minAmt,
            Double maxAmt) {

        List<Transaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");

        if (type != null && !type.isEmpty()) sql.append(" AND txn_type = ?");
        if (isValidDate(startDate) && isValidDate(endDate)) sql.append(" AND DATE(txn_date) BETWEEN ? AND ?");
        if (minAmt != null) sql.append(" AND amount >= ?");
        if (maxAmt != null) sql.append(" AND amount <= ?");
        sql.append(" ORDER BY txn_date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            ps.setInt(index++, userId);

            if (type != null && !type.isEmpty()) ps.setString(index++, type);
            if (isValidDate(startDate) && isValidDate(endDate)) {
                ps.setString(index++, startDate);
                ps.setString(index++, endDate);
            }
            if (minAmt != null) ps.setDouble(index++, minAmt);
            if (maxAmt != null) ps.setDouble(index++, maxAmt);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("user_id"),
                        rs.getString("from_account"),
                        rs.getString("to_account"),
                        rs.getDouble("amount"),
                        rs.getString("txn_type"),
                        rs.getTimestamp("txn_date")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper to validate date format
    private boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) return false;
        if (date.equalsIgnoreCase("YYYY-MM-DD")) return false;
        try {
            java.sql.Date.valueOf(date.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
