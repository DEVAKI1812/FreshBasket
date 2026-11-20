package com.freshbasket.dao;

import com.freshbasket.config.DatabaseConfig;
import com.freshbasket.model.CartItem;
import com.freshbasket.model.Order;
import com.freshbasket.model.OrderItem;
import com.freshbasket.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class OrderDAO {
    public Order placeOrderTransaction(User user, Collection<CartItem> cartItems, double subtotal,
                                       double gstAmount, double deliveryFee, double discountAmount,
                                       double grandTotal, String deliverySlot, String deliveryAddress,
                                       String paymentMode, boolean deductWallet) throws SQLException {

        String orderCode = "FB-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"))
                + "-" + (100 + new Random().nextInt(900));

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            String checkStockSql = "SELECT stock_qty, name FROM products WHERE product_id = ? FOR UPDATE";
            String updateStockSql = "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ?";

            try (PreparedStatement checkPs = conn.prepareStatement(checkStockSql);
                 PreparedStatement updatePs = conn.prepareStatement(updateStockSql)) {
                for (CartItem ci : cartItems) {
                    checkPs.setInt(1, ci.getProduct().getProductId());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Product not found: " + ci.getProduct().getName());
                        int available = rs.getInt("stock_qty");
                        if (available < ci.getQuantity()) {
                            throw new SQLException("Insufficient stock for " + rs.getString("name") + " (Available: " + available + ")");
                        }
                    }
                    updatePs.setInt(1, ci.getQuantity());
                    updatePs.setInt(2, ci.getProduct().getProductId());
                    updatePs.executeUpdate();
                }
            }

            if (deductWallet) {
                if (user.getWalletBalance() < grandTotal) throw new SQLException("Insufficient FreshBasket Wallet balance.");
                try (PreparedStatement wPs = conn.prepareStatement("UPDATE users SET wallet_balance = wallet_balance - ? WHERE user_id = ?")) {
                    wPs.setDouble(1, grandTotal);
                    wPs.setInt(2, user.getUserId());
                    wPs.executeUpdate();
                }
                user.setWalletBalance(user.getWalletBalance() - grandTotal);
            }

            String insertOrderSql = "INSERT INTO orders (order_code, user_id, subtotal, gst_amount, delivery_fee, discount_amount, "
                    + "grand_total, delivery_slot, delivery_address, payment_mode, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Order Placed')";
            int generatedOrderId;
            try (PreparedStatement oPs = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                oPs.setString(1, orderCode);
                oPs.setInt(2, user.getUserId());
                oPs.setDouble(3, subtotal);
                oPs.setDouble(4, gstAmount);
                oPs.setDouble(5, deliveryFee);
                oPs.setDouble(6, discountAmount);
                oPs.setDouble(7, grandTotal);
                oPs.setString(8, deliverySlot);
                oPs.setString(9, deliveryAddress);
                oPs.setString(10, paymentMode);
                oPs.executeUpdate();

                try (ResultSet keys = oPs.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Failed to retrieve generated Order ID.");
                    generatedOrderId = keys.getInt(1);
                }
            }

            String insertItemSql = "INSERT INTO order_items (order_id, product_id, product_name, unit_weight, quantity, unit_price, total_price) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            List<OrderItem> savedItems = new ArrayList<>();
            try (PreparedStatement iPs = conn.prepareStatement(insertItemSql)) {
                for (CartItem ci : cartItems) {
                    iPs.setInt(1, generatedOrderId);
                    iPs.setInt(2, ci.getProduct().getProductId());
                    iPs.setString(3, ci.getProduct().getName());
                    iPs.setString(4, ci.getProduct().getUnitWeight());
                    iPs.setInt(5, ci.getQuantity());
                    iPs.setDouble(6, ci.getProduct().getSellingPrice());
                    iPs.setDouble(7, ci.getLineTotal());
                    iPs.addBatch();

                    savedItems.add(new OrderItem(
                            ci.getProduct().getProductId(), ci.getProduct().getName(),
                            ci.getProduct().getUnitWeight(), ci.getQuantity(),
                            ci.getProduct().getSellingPrice(), ci.getLineTotal()));
                }
                iPs.executeBatch();
            }

            conn.commit();

            Order order = new Order();
            order.setOrderId(generatedOrderId);
            order.setOrderCode(orderCode);
            order.setUserId(user.getUserId());
            order.setCustomerName(user.getFullName());
            order.setSubtotal(subtotal);
            order.setGstAmount(gstAmount);
            order.setDeliveryFee(deliveryFee);
            order.setDiscountAmount(discountAmount);
            order.setGrandTotal(grandTotal);
            order.setDeliverySlot(deliverySlot);
            order.setDeliveryAddress(deliveryAddress);
            order.setPaymentMode(paymentMode);
            order.setStatus("Order Placed");
            order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            order.setItems(savedItems);
            return order;
        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Order> getOrdersByUser(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name FROM orders o JOIN users u ON o.user_id = u.user_id WHERE o.user_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapOrder(rs);
                    o.setItems(getOrderItems(conn, o.getOrderId()));
                    orders.add(o);
                }
            }
        }
        return orders;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name FROM orders o JOIN users u ON o.user_id = u.user_id ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order o = mapOrder(rs);
                o.setItems(getOrderItems(conn, o.getOrderId()));
                orders.add(o);
            }
        }
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<OrderItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM order_items WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setOrderId(orderId);
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitWeight(rs.getString("unit_weight"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setTotalPrice(rs.getDouble("total_price"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setUserId(rs.getInt("user_id"));
        o.setCustomerName(rs.getString("full_name"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setGstAmount(rs.getDouble("gst_amount"));
        o.setDeliveryFee(rs.getDouble("delivery_fee"));
        o.setDiscountAmount(rs.getDouble("discount_amount"));
        o.setGrandTotal(rs.getDouble("grand_total"));
        o.setDeliverySlot(rs.getString("delivery_slot"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setPaymentMode(rs.getString("payment_mode"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}
