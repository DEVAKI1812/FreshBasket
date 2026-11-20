package com.freshbasket.ui;

import com.freshbasket.dao.CouponDAO;
import com.freshbasket.model.CartItem;
import com.freshbasket.model.User;
import com.freshbasket.util.CartManager;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class CartFrame extends JDialog {
    private final CustomerDashboardFrame dashboard;
    private final User currentUser;
    private final CartManager cartManager = CartManager.getInstance();
    private final CouponDAO couponDAO = new CouponDAO();

    private JPanel itemsPanel;
    private JTextField promoField;
    private JLabel subLbl, gstLbl, delLbl, discLbl, totalLbl, freeHint;
    private JProgressBar freeDeliveryBar;
    private double appliedDiscount = 0.0;
    private String appliedCode = "";

    public CartFrame(CustomerDashboardFrame dashboard, User currentUser) {
        super(dashboard, "FreshBasket — Smart Basket", true);
        this.dashboard = dashboard;
        this.currentUser = currentUser;

        setSize(880, 620);
        setLocationRelativeTo(dashboard);

        JPanel root = new JPanel(new BorderLayout(18, 16));
        root.setBackground(Theme.BG_PAGE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Your Smart Basket");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.TEXT_DARK);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Theme.BG_PAGE);

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PAGE);

        UIHelper.ShadowCard summary = new UIHelper.ShadowCard(14, Color.WHITE, Theme.BORDER_CARD);
        summary.setPreferredSize(new Dimension(320, 0));
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel billTitle = new JLabel("Order Summary");
        billTitle.setFont(Theme.FONT_H2);
        billTitle.setForeground(Theme.TEXT_DARK);

        freeHint = new JLabel();
        freeHint.setFont(Theme.FONT_SMALL_BOLD);
        freeDeliveryBar = new JProgressBar(0, 499);
        freeDeliveryBar.setForeground(Theme.BB_GREEN);
        freeDeliveryBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

        JPanel promoRow = new JPanel(new BorderLayout(6, 0));
        promoRow.setOpaque(false);
        promoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        promoField = new JTextField();
        promoField.putClientProperty("JTextField.placeholderText", "FRESH20 or FIRST100");
        JButton applyBtn = UIHelper.createSolidButton("Apply", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 6);
        applyBtn.addActionListener(e -> applyPromo());
        promoRow.add(promoField, BorderLayout.CENTER);
        promoRow.add(applyBtn, BorderLayout.EAST);

        subLbl = new JLabel(); subLbl.setFont(Theme.FONT_BODY);
        gstLbl = new JLabel(); gstLbl.setFont(Theme.FONT_BODY);
        delLbl = new JLabel(); delLbl.setFont(Theme.FONT_BODY);
        discLbl = new JLabel(); discLbl.setFont(Theme.FONT_BODY_BOLD); discLbl.setForeground(Theme.BB_GREEN_DARK);
        totalLbl = new JLabel(); totalLbl.setFont(Theme.FONT_H1); totalLbl.setForeground(Theme.TEXT_DARK);

        JButton checkoutBtn = UIHelper.createSolidButton("Proceed to Checkout  →", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        checkoutBtn.addActionListener(e -> {
            if (cartManager.isEmpty()) {
                UIHelper.showError(this, "Your basket is empty.");
                return;
            }
            double sub = cartManager.getSubtotal();
            double gst = sub * 0.05;
            double del = sub >= 499.0 ? 0.0 : 40.0;
            double grand = Math.max(0, sub + gst + del - appliedDiscount);
            dispose();
            new CheckoutFrame(dashboard, currentUser, sub, gst, del, appliedDiscount, grand).setVisible(true);
        });

        summary.add(billTitle);
        summary.add(Box.createVerticalStrut(8));
        summary.add(freeHint);
        summary.add(Box.createVerticalStrut(4));
        summary.add(freeDeliveryBar);
        summary.add(Box.createVerticalStrut(16));
        summary.add(promoRow);
        summary.add(Box.createVerticalStrut(18));
        summary.add(subLbl);
        summary.add(Box.createVerticalStrut(6));
        summary.add(gstLbl);
        summary.add(Box.createVerticalStrut(6));
        summary.add(delLbl);
        summary.add(Box.createVerticalStrut(6));
        summary.add(discLbl);
        summary.add(Box.createVerticalStrut(14));
        summary.add(new JSeparator());
        summary.add(Box.createVerticalStrut(14));
        summary.add(totalLbl);
        summary.add(Box.createVerticalGlue());
        summary.add(checkoutBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(summary, BorderLayout.EAST);
        setContentPane(root);
        refreshView();
    }

    private void applyPromo() {
        String code = promoField.getText().trim();
        if (code.isEmpty()) return;
        try {
            appliedDiscount = couponDAO.calculateDiscount(code, cartManager.getSubtotal());
            appliedCode = code.toUpperCase();
            UIHelper.showInfo(this, String.format("Coupon %s applied! Saved ₹%.2f", appliedCode, appliedDiscount));
            refreshView();
        } catch (SQLException ex) {
            appliedDiscount = 0; appliedCode = "";
            UIHelper.showError(this, ex.getMessage());
            refreshView();
        }
    }

    private void refreshView() {
        itemsPanel.removeAll();
        if (cartManager.isEmpty()) {
            JLabel empty = new JLabel("Your basket is empty. Add fresh items from the storefront!");
            empty.setFont(Theme.FONT_SUBTITLE);
            empty.setForeground(Theme.TEXT_MUTED);
            itemsPanel.add(empty);
        } else {
            for (CartItem ci : cartManager.getItems()) {
                UIHelper.ShadowCard row = new UIHelper.ShadowCard(10, Color.WHITE, Theme.BORDER_CARD);
                row.setLayout(new BorderLayout(12, 0));
                row.setBorder(new EmptyBorder(12, 16, 12, 16));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setOpaque(false);
                JLabel n = new JLabel(ci.getProduct().getName());
                n.setFont(Theme.FONT_BODY_BOLD);
                JLabel w = new JLabel(ci.getProduct().getUnitWeight() + " • ₹" + String.format("%.2f", ci.getProduct().getSellingPrice()));
                w.setFont(Theme.FONT_SMALL); w.setForeground(Theme.TEXT_MUTED);
                info.add(n); info.add(w);

                JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
                ctrl.setOpaque(false);
                JButton m = UIHelper.createOutlineButton("-", Theme.BB_RED, Theme.BB_RED, Theme.BB_RED, Color.WHITE);
                m.setBorder(new EmptyBorder(4, 10, 4, 10));
                m.addActionListener(e -> { cartManager.decrementProduct(ci.getProduct().getProductId()); refreshView(); });

                JLabel q = new JLabel(String.valueOf(ci.getQuantity()));
                q.setFont(Theme.FONT_BODY_BOLD);

                JButton p = UIHelper.createSolidButton("+", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 6);
                p.setBorder(new EmptyBorder(4, 10, 4, 10));
                p.addActionListener(e -> { cartManager.addProduct(ci.getProduct()); refreshView(); });

                JLabel lt = new JLabel(String.format("₹%.2f", ci.getLineTotal()));
                lt.setFont(Theme.FONT_BODY_BOLD);
                lt.setBorder(new EmptyBorder(0, 10, 0, 0));

                ctrl.add(m); ctrl.add(q); ctrl.add(p); ctrl.add(lt);
                row.add(info, BorderLayout.CENTER);
                row.add(ctrl, BorderLayout.EAST);
                itemsPanel.add(row);
                itemsPanel.add(Box.createVerticalStrut(8));
            }
        }

        double sub = cartManager.getSubtotal();
        if (!appliedCode.isEmpty()) {
            try { appliedDiscount = couponDAO.calculateDiscount(appliedCode, sub); }
            catch (SQLException e) { appliedDiscount = 0; }
        }
        double gst = sub * 0.05;
        double del = (sub == 0 || sub >= 499.0) ? 0.0 : 40.0;
        double grand = Math.max(0, sub + gst + del - appliedDiscount);

        freeDeliveryBar.setValue((int) Math.min(499, sub));
        if (sub >= 499.0) {
            freeHint.setText("✓ FREE 10-Min Express Delivery Unlocked!");
            freeHint.setForeground(Theme.BB_GREEN_DARK);
        } else {
            freeHint.setText(String.format("Add ₹%.0f more for FREE Delivery", 499.0 - sub));
            freeHint.setForeground(Theme.BB_GOLD);
        }

        subLbl.setText(String.format("Subtotal:          ₹%.2f", sub));
        gstLbl.setText(String.format("GST (5%%):          ₹%.2f", gst));
        delLbl.setText(del == 0 ? "Delivery Fee:      FREE" : String.format("Delivery Fee:      ₹%.2f", del));
        discLbl.setText(String.format("Promo Savings:    -₹%.2f", appliedDiscount));
        totalLbl.setText(String.format("Total: ₹%.2f", grand));

        itemsPanel.revalidate();
        itemsPanel.repaint();
        dashboard.loadProducts();
    }
}
