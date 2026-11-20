package com.freshbasket.dao;

import com.freshbasket.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CouponDAO {
    public double calculateDiscount(String code, double subtotal) throws SQLException {
        if (code == null || code.trim().isEmpty()) return 0.0;
        String sql = "SELECT * FROM coupons WHERE UPPER(code) = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Invalid or expired promo code.");
                double minOrder = rs.getDouble("min_order_amount");
                if (subtotal < minOrder) {
                    throw new SQLException(String.format("Minimum order of ₹%.0f required for %s.", minOrder, code.toUpperCase()));
                }
                String type = rs.getString("discount_type");
                double val = rs.getDouble("discount_value");
                double maxDisc = rs.getDouble("max_discount");
                double discount = "PERCENT".equalsIgnoreCase(type) ? (subtotal * val / 100.0) : val;
                return Math.min(discount, maxDisc);
            }
        }
    }
}
