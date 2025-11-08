package com.bankapp.dao;

import com.bankapp.config.DBConnection;
import com.bankapp.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    // Register a new user with BCrypt password hashing
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users(name, email, password, status, created_at) VALUES(?, ?, ?, 'PENDING', NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName().trim());
            ps.setString(2, user.getEmail().trim());
            ps.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "User registration failed", e);
            return false;
        }
    }

    // Secure user login with BCrypt password validation
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");

                    if (BCrypt.checkpw(password.trim(), storedHash)) {
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        user.setPassword(storedHash);
                        user.setStatus(rs.getString("status"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    } else {
                        logger.info("Invalid password attempt for user: " + email);
                    }
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "User login failed for email: " + email, e);
        }
        return null;
    }

    // Update user password (used by user profile / password change)
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Password update failed for user: " + userId, e);
            return false;
        }
    }

    // Admin-only: Fetch all users
    public List<User> getAllUsers() {
        String sql = "SELECT user_id, name, email, status, created_at FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                users.add(u);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch all users", e);
        }
        return users;
    }

    // Admin-only: Update user status (approve/block)
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.trim().toUpperCase());
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to update user status: " + userId, e);
            return false;
        }
    }

    // Admin-only: Delete user completely
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to delete user: " + userId, e);
            return false;
        }
    }

    // ✅ Added method — required by BankService for transfer notifications
    public User getUserByAccount(String accountNumber) {
        String sql = """
            SELECT u.user_id, u.name, u.email, u.password, u.status, u.created_at
            FROM users u
            JOIN accounts a ON u.user_id = a.user_id
            WHERE a.account_number = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setStatus(rs.getString("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to fetch user by account number: " + accountNumber, e);
        }
        return null;
    }
    // Register new user and automatically create their first account
    public boolean registerUserWithAccount(User user, String accNum, String accType, double balance) {
        String insertUser = "INSERT INTO users (name, email, password, status, created_at) VALUES (?, ?, ?, 'APPROVED', NOW())";
        String insertAccount = "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement userPs = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement accPs = conn.prepareStatement(insertAccount)) {

            conn.setAutoCommit(false);

            // Insert new user
            userPs.setString(1, user.getName().trim());
            userPs.setString(2, user.getEmail().trim());
            userPs.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userPs.executeUpdate();

            // Get generated user_id
            try (ResultSet rs = userPs.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // Create the user's first account
                    accPs.setInt(1, userId);
                    accPs.setString(2, accNum.trim());
                    accPs.setString(3, accType.trim().toUpperCase());
                    accPs.setDouble(4, balance);
                    accPs.executeUpdate();

                    // Commit both inserts together
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "User registration with account failed", e);
            return false;
        }
    }

}
