package com.bankapp.service;

import com.bankapp.dao.AdminDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.model.Admin;
import com.bankapp.model.User;
import java.util.List;

public class AdminService {

    private static final String LOCAL_IP = "127.0.0.1";

    private final AdminDAO adminDAO;
    private final UserDAO userDAO;

    public AdminService() {
        this.adminDAO = new AdminDAO();
        this.userDAO = new UserDAO();
    }

    /** Handles admin login securely and logs activity if successful. */
    public Admin loginAdmin(String username, String password, String ipAddress) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            System.err.println("⚠️ Invalid credentials: Empty username or password.");
            return null;
        }

        Admin admin = adminDAO.login(username, password);
        if (admin != null) {
            adminDAO.logAdminActivity(admin.getAdminId(), "Logged in", ipAddress);
        }
        return admin;
    }

    /** Register new admin (only by super-admin or one-time setup). */
    public boolean registerAdmin(String username, String password, String fullName, String email) {
        return adminDAO.registerAdmin(username, password, fullName, email);
    }

    /** Fetch all users for admin dashboard. */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /** Approve a user account. */
    public boolean approveUser(int adminId, int userId) {
        boolean ok = userDAO.updateUserStatus(userId, "APPROVED");
        if (ok)
            adminDAO.logAdminActivity(adminId, "Approved user " + userId, LOCAL_IP);
        return ok;
    }

    /** Block a user account. */
    public boolean blockUser(int adminId, int userId) {
        boolean ok = userDAO.updateUserStatus(userId, "BLOCKED");
        if (ok)
            adminDAO.logAdminActivity(adminId, "Blocked user " + userId, LOCAL_IP);
        return ok;
    }

    /** Delete a user and cascade accounts. */
    public boolean deleteUser(int adminId, int userId) {
        boolean ok = userDAO.deleteUser(userId);
        if (ok)
            adminDAO.logAdminActivity(adminId, "Deleted user " + userId, LOCAL_IP);
        return ok;
    }

    /** Fetch recent logs for the admin. */
    public String getRecentAdminLogs(int adminId, int limit) {
        return adminDAO.fetchRecentLogs(adminId, limit);
    }
}
