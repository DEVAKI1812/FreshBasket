package com.freshbasket.ui;

import com.freshbasket.dao.UserDAO;
import com.freshbasket.model.User;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class RegisterFrame extends JFrame {
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField addressField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final UserDAO userDAO = new UserDAO();
    private final LoginFrame parentLogin;

    public RegisterFrame(LoginFrame parentLogin) {
        this.parentLogin = parentLogin;
        setTitle("FreshBasket — Create Account");
        setSize(480, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PAGE);
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        UIHelper.ShadowCard card = new UIHelper.ShadowCard(14, Color.WHITE, Theme.BORDER_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Join FreshBasket");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Unlock ₹1,500 instant Welcome Wallet balance!");
        sub.setFont(Theme.FONT_SMALL_BOLD);
        sub.setForeground(Theme.BB_GREEN);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(18));

        addField(card, "Full Name", nameField);
        addField(card, "Email Address", emailField);
        addField(card, "Phone Number", phoneField);
        addField(card, "Delivery Address", addressField);
        addField(card, "Password", passwordField);

        JButton submitBtn = UIHelper.createSolidButton("Create Account & Claim ₹1,500", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> handleRegister());

        JButton backBtn = UIHelper.createOutlineButton("Back to Login", Theme.BORDER_CARD, Theme.TEXT_DARK, Theme.BG_SURFACE, Theme.TEXT_DARK);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> {
            dispose();
            parentLogin.setVisible(true);
        });

        card.add(Box.createVerticalStrut(10));
        card.add(submitBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(backBtn);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addField(JPanel card, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL_BOLD);
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(Theme.FONT_BODY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createVerticalStrut(4));
        card.add(field);
        card.add(Box.createVerticalStrut(10));
    }

    private void handleRegister() {
        String name = nameField.getText(), email = emailField.getText(),
                phone = phoneField.getText(), addr = addressField.getText(),
                pass = new String(passwordField.getPassword());

        if (name.isBlank() || email.isBlank() || phone.isBlank() || addr.isBlank() || pass.isBlank()) {
            UIHelper.showError(this, "Please fill all fields.");
            return;
        }
        try {
            User u = new User(0, name, email, pass, phone, addr, "CUSTOMER", 1500.0);
            if (userDAO.registerCustomer(u)) {
                UIHelper.showInfo(this, "Account created! You can now sign in.");
                dispose();
                parentLogin.setVisible(true);
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Registration error: " + ex.getMessage());
        }
    }
}
