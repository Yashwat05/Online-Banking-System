package com.bankapp.dao;

import com.bankapp.model.Admin;
import com.bankapp.config.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDAO {

    private static final Logger logger = Logger.getLogger(AdminDAO.class.getName());

    /**
     * Register new admin securely using BCrypt hashing.
     */
    public boolean registerAdmin(String username, String rawPassword, String fullName, String email) {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        String query = "INSERT INTO admins (username, password, full_name, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username.trim());
            ps.setString(2, hash);
            ps.setString(3, fullName);
            ps.setString(4, email);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Admin registration failed", e);
            return false;
        }
    }

    /**
     * Secure login validation with BCrypt password verification.
     */
    public Admin login(String username, String password) {
        String query = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        Admin admin = new Admin(
                                rs.getInt("admin_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("email")
                        );
                        admin.setRole(rs.getString("role")); // NEW
                        return admin;
                    } else {
                        logger.info("Invalid password for admin: " + username);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Login failed for admin: " + username, e);
        }
        return null;
    }

    /**
     * Get admin details by ID.
     */
    public Admin getAdminById(int adminId) {
        String query = "SELECT * FROM admins WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                            rs.getInt("admin_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching admin by ID: " + adminId, e);
        }
        return null;
    }

    /**
     * Log admin activities for auditing.
     */
    public void logAdminActivity(int adminId, String action, String ipAddress) {
        String query = "INSERT INTO admin_logs (admin_id, action, ip_address, timestamp) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, adminId);
            ps.setString(2, action);
            ps.setString(3, ipAddress);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to log admin activity: " + action, e);
        }
    }

    /**
     * Fetch recent logs for an admin.
     */
    public String fetchRecentLogs(int adminId, int limit) {
        String query = "SELECT timestamp, action, ip_address FROM admin_logs WHERE admin_id = ? ORDER BY timestamp DESC LIMIT ?";
        StringBuilder sb = new StringBuilder();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, adminId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getTimestamp("timestamp"))
                            .append(" - ")
                            .append(rs.getString("action"))
                            .append(" [")
                            .append(rs.getString("ip_address"))
                            .append("]\n");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to fetch logs for admin " + adminId, e);
        }
        return sb.toString();
    }
}
