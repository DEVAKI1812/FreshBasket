package com.freshbasket.ui;

import com.freshbasket.dao.OrderDAO;
import com.freshbasket.model.Order;
import com.freshbasket.model.User;
import com.freshbasket.util.CartManager;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class CheckoutFrame extends JDialog {
    private final CustomerDashboardFrame dashboard;
    private final User currentUser;
    private final double subtotal, gst, deliveryFee, discount, grandTotal;
    private final OrderDAO orderDAO = new OrderDAO();
    private final CartManager cartManager = CartManager.getInstance();

    private final JComboBox<String> slotCombo;
    private final JComboBox<String> paymentCombo;
    private final JTextArea addressArea;

    public CheckoutFrame(CustomerDashboardFrame dashboard, User currentUser,
                         double subtotal, double gst, double deliveryFee, double discount, double grandTotal) {
        super(dashboard, "FreshBasket — Express Checkout", true);
        this.dashboard = dashboard;
        this.currentUser = currentUser;
        this.subtotal = subtotal;
        this.gst = gst;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.grandTotal = grandTotal;

        setSize(520, 540);
        setLocationRelativeTo(dashboard);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG_PAGE);
        root.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("Delivery & Payment");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.TEXT_DARK);

        UIHelper.ShadowCard card = new UIHelper.ShadowCard(14, Color.WHITE, Theme.BORDER_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        slotCombo = new JComboBox<>(new String[]{
                "⚡ Express 10-Min Delivery (Now)",
                "Morning Slot (7:00 AM - 10:00 AM)",
                "Evening Slot (5:00 PM - 8:00 PM)"
        });
        slotCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        slotCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        addressArea = new JTextArea(currentUser.getAddress(), 3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        addrScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        paymentCombo = new JComboBox<>(new String[]{
                String.format("FreshBasket Wallet (Balance: ₹%.2f)", currentUser.getWalletBalance()),
                "UPI / Instant QR Pay",
                "Credit / Debit Card",
                "Cash on Delivery (COD)"
        });
        paymentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        paymentCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel totalLbl = new JLabel(String.format("Payable Amount: ₹%.2f", grandTotal));
        totalLbl.setFont(Theme.FONT_H1);
        totalLbl.setForeground(Theme.BB_GREEN_DARK);
        totalLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton placeBtn = UIHelper.createSolidButton("Place Order Now", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        placeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        placeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        placeBtn.addActionListener(e -> placeOrder());

        addLabeled(card, "Delivery Slot", slotCombo);
        addLabeled(card, "Delivery Address", addrScroll);
        addLabeled(card, "Payment Method", paymentCombo);
        card.add(Box.createVerticalStrut(10));
        card.add(totalLbl);
        card.add(Box.createVerticalStrut(14));
        card.add(placeBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addLabeled(JPanel p, String label, JComponent c) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL_BOLD);
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(c);
        p.add(Box.createVerticalStrut(14));
    }

    private void placeOrder() {
        String addr = addressArea.getText().trim();
        if (addr.isEmpty()) {
            UIHelper.showError(this, "Enter a delivery address.");
            return;
        }
        boolean useWallet = paymentCombo.getSelectedIndex() == 0;
        String payMode = useWallet ? "FreshBasket Wallet" : (String) paymentCombo.getSelectedItem();
        try {
            Order order = orderDAO.placeOrderTransaction(
                    currentUser, cartManager.getItems(), subtotal, gst, deliveryFee, discount, grandTotal,
                    (String) slotCombo.getSelectedItem(), addr, payMode, useWallet);
            cartManager.clear();
            dashboard.loadProducts();
            dispose();
            new ReceiptFrame(dashboard, order).setVisible(true);
        } catch (SQLException ex) {
            UIHelper.showError(this, "Checkout Failed: " + ex.getMessage());
        }
    }
}
