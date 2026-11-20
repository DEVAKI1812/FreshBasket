package com.freshbasket.dao;

import com.freshbasket.config.DatabaseConfig;
import com.freshbasket.model.User;

import java.sql.*;

public class UserDAO {
    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        }
        return null;
    }

    public boolean registerCustomer(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password, phone, address, role, wallet_balance) VALUES (?, ?, ?, ?, ?, 'CUSTOMER', 1500.00)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName().trim());
            ps.setString(2, user.getEmail().trim());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone().trim());
            ps.setString(5, user.getAddress().trim());
            return ps.executeUpdate() > 0;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("role"),
                rs.getDouble("wallet_balance")
        );
    }
}
