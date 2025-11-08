package com.bankapp.util;

import com.bankapp.service.AdminService;
import com.bankapp.config.DBConnection;

public class CreateAdmin {
    public static void main(String[] args) {
        try {
            DBConnection.getConnection().close(); // Quick check if DB works
        } catch (Exception e) {
            System.err.println("Database connection failed. Check DBConnection config.");
            return;
        }

        AdminService adminService = new AdminService();

        String username = "yashwatsa";
        String password = "yashwatsa05"; // You can change this
        String fullName = "Yashwat Pratap Singh";
        String email = "yashwatpratapsingh23092005@gmail.com";

        boolean ok = adminService.registerAdmin(username, password, fullName, email);
        if (ok) {
            System.out.println("Admin created successfully.");
        } else {
            System.err.println("Failed to create admin (maybe username already exists).");
        }
    }
}
