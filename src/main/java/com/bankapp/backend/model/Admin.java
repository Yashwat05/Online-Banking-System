package com.bankapp.model;

public class Admin {
    private int adminId;
    private String username;
    private String fullName;
    private String email;
    private String role; // Added to support SUPER_ADMIN and ADMIN roles

    // Parameterized constructor
    public Admin(int adminId, String username, String fullName, String email) {
        this.adminId = adminId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = "ADMIN"; // default role
    }

    // No-arg constructor
    public Admin() {}

    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Optional: convenience method
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role);
    }
}
