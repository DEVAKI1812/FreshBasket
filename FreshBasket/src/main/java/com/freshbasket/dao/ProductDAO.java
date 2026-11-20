package com.freshbasket.dao;

import com.freshbasket.config.DatabaseConfig;
import com.freshbasket.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapProduct(rs));
        }
        return list;
    }

    public List<Product> searchProducts(String keyword, String category, boolean onlyUnder35, boolean onlyTopDeals) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(category) LIKE ? OR LOWER(badge_tag) LIKE ?) ");
        }
        if (category != null && !category.equalsIgnoreCase("All")) {
            sql.append("AND category = ? ");
        }
        if (onlyUnder35) {
            sql.append("AND selling_price <= 40.00 ");
        }
        if (onlyTopDeals) {
            sql.append("AND ((mrp - selling_price) / mrp) >= 0.18 ");
        }
        sql.append("ORDER BY product_id ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String k = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (category != null && !category.equalsIgnoreCase("All")) {
                ps.setString(idx++, category);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapProduct(rs));
            }
        }
        return list;
    }

    public boolean addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (name, category, unit_weight, mrp, selling_price, stock_qty, badge_tag) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getUnitWeight());
            ps.setDouble(4, p.getMrp());
            ps.setDouble(5, p.getSellingPrice());
            ps.setInt(6, p.getStockQty());
            ps.setString(7, p.getBadgeTag());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProduct(Product p) throws SQLException {
        String sql = "UPDATE products SET name=?, category=?, unit_weight=?, mrp=?, selling_price=?, stock_qty=?, badge_tag=? WHERE product_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getUnitWeight());
            ps.setDouble(4, p.getMrp());
            ps.setDouble(5, p.getSellingPrice());
            ps.setInt(6, p.getStockQty());
            ps.setString(7, p.getBadgeTag());
            ps.setInt(8, p.getProductId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("unit_weight"),
                rs.getDouble("mrp"),
                rs.getDouble("selling_price"),
                rs.getInt("stock_qty"),
                rs.getString("badge_tag")
        );
    }
}
