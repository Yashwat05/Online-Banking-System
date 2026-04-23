package com.bankapp.util;

import com.bankapp.config.DBConnection;
import java.sql.*;

public class PasswordMigration {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, password FROM users")) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String plain = rs.getString("password");
                String hash = PasswordUtil.hashPassword(plain);

                try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=? WHERE user_id=?")) {
                    ps.setString(1, hash);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            System.out.println("Passwords migrated to SHA-256 successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
