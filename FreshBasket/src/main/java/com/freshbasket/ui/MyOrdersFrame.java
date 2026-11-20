package com.freshbasket.ui;

import com.freshbasket.dao.OrderDAO;
import com.freshbasket.model.Order;
import com.freshbasket.model.User;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MyOrdersFrame extends JDialog {
    private final User currentUser;
    private final OrderDAO orderDAO = new OrderDAO();

    public MyOrdersFrame(Frame owner, User currentUser) {
        super(owner, "FreshBasket — My Orders & Live Tracking", true);
        this.currentUser = currentUser;

        setSize(820, 580);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG_PAGE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("My Orders");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.TEXT_DARK);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.BG_PAGE);

        try {
            List<Order> orders = orderDAO.getOrdersByUser(currentUser.getUserId());
            if (orders.isEmpty()) {
                JLabel empty = new JLabel("No past orders yet. Start adding fresh items to your basket!");
                empty.setFont(Theme.FONT_SUBTITLE);
                empty.setForeground(Theme.TEXT_MUTED);
                listPanel.add(empty);
            } else {
                for (Order o : orders) {
                    UIHelper.ShadowCard card = new UIHelper.ShadowCard(12, Color.WHITE, Theme.BORDER_CARD);
                    card.setLayout(new BorderLayout(12, 6));
                    card.setBorder(new EmptyBorder(14, 18, 14, 18));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

                    JPanel left = new JPanel();
                    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
                    left.setOpaque(false);
                    JLabel code = new JLabel(o.getOrderCode() + "  •  " + o.getItems().size() + " item(s)");
                    code.setFont(Theme.FONT_SUBTITLE);
                    JLabel slot = new JLabel(o.getDeliverySlot() + "  |  " + o.getPaymentMode());
                    slot.setFont(Theme.FONT_BODY); slot.setForeground(Theme.TEXT_MUTED);
                    JLabel addr = new JLabel("Deliver to: " + o.getDeliveryAddress());
                    addr.setFont(Theme.FONT_SMALL); addr.setForeground(Theme.TEXT_LIGHT);
                    left.add(code); left.add(Box.createVerticalStrut(4)); left.add(slot); left.add(Box.createVerticalStrut(4)); left.add(addr);

                    JPanel right = new JPanel();
                    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
                    right.setOpaque(false);
                    JLabel st = new JLabel("  " + o.getStatus().toUpperCase() + "  ");
                    st.setFont(Theme.FONT_SMALL_BOLD);
                    st.setOpaque(true);
                    st.setBackground("Delivered".equalsIgnoreCase(o.getStatus()) ? Theme.BB_GREEN_SOFT : Theme.BB_RED_SOFT);
                    st.setForeground("Delivered".equalsIgnoreCase(o.getStatus()) ? Theme.BB_GREEN_DARK : Theme.BB_RED);
                    st.setAlignmentX(Component.RIGHT_ALIGNMENT);

                    JLabel amt = new JLabel(String.format("₹%.2f", o.getGrandTotal()));
                    amt.setFont(Theme.FONT_PRICE);
                    amt.setAlignmentX(Component.RIGHT_ALIGNMENT);

                    JButton qrBtn = UIHelper.createOutlineButton("View QR Invoice", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Theme.BB_GREEN, Color.WHITE);
                    qrBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    qrBtn.addActionListener(e -> new ReceiptFrame((Frame) getOwner(), o).setVisible(true));

                    right.add(st); right.add(Box.createVerticalStrut(6)); right.add(amt); right.add(Box.createVerticalStrut(6)); right.add(qrBtn);

                    card.add(left, BorderLayout.CENTER);
                    card.add(right, BorderLayout.EAST);
                    listPanel.add(card);
                    listPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Error loading orders: " + ex.getMessage());
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PAGE);

        root.add(title, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }
}
