package com.freshbasket.ui;

import com.freshbasket.dao.UserDAO;
import com.freshbasket.model.User;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("FreshBasket — Login / Sign Up");
        setSize(820, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));

        // LEFT PANEL: Light Gray Trust Panel ("Why choose FreshBasket?")
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(0xF1, 0xF1, 0xF1));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(42, 36, 36, 36));

        JLabel whyTitle = new JLabel("Why choose FreshBasket?");
        whyTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        whyTitle.setForeground(new Color(0x8A, 0x6D, 0x1D));
        whyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(2, 2, 22, 24));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(32, 10, 28, 10));
        grid.add(createTrustBadge("★", "Quality", "Farm-fresh handpicked"));
        grid.add(createTrustBadge("⏱", "On time", "10-min express slots"));
        grid.add(createTrustBadge("↺", "Return Policy", "No-questions return"));
        grid.add(createTrustBadge("🚚", "Free Delivery", "Orders above ₹499"));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(260, 2));
        sep.setForeground(Theme.BB_DARK);

        JLabel footerLogo = new JLabel("freshbasket  •  Madurai Express Hub");
        footerLogo.setFont(Theme.FONT_SMALL_BOLD);
        footerLogo.setForeground(Theme.TEXT_MUTED);
        footerLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(whyTitle);
        leftPanel.add(grid);
        leftPanel.add(sep);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(footerLogo);

        // RIGHT PANEL: Sleek Pitch-Black Login Form
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Theme.BB_BLACK);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(40, 42, 36, 42));

        JLabel title = new JLabel("Login/ Sign up");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Using Email & Password");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(new Color(0xD1, 0xD5, 0xDB));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel underline = new JPanel();
        underline.setBackground(new Color(0xD9, 0x77, 0x06));
        underline.setMaximumSize(new Dimension(72, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new JTextField();
        emailField.putClientProperty("JTextField.placeholderText", "Enter Email Id (e.g. devaki@freshbasket.com)");
        emailField.setFont(Theme.FONT_BODY);
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(Theme.BB_DARK);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Enter Password");
        passwordField.setFont(Theme.FONT_BODY);
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Theme.BB_DARK);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton continueBtn = UIHelper.createSolidButton("Continue", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        continueBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        continueBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        continueBtn.addActionListener(e -> handleLogin());

        // Quick 1-Click Demo Row
        JPanel demoRow = new JPanel(new GridLayout(1, 2, 10, 0));
        demoRow.setOpaque(false);
        demoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        demoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton demoCust = UIHelper.createSolidButton("Demo Customer", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 8);
        demoCust.addActionListener(e -> {
            emailField.setText("devaki@freshbasket.com");
            passwordField.setText("devaki123");
            handleLogin();
        });

        JButton demoAdm = UIHelper.createSolidButton("Demo Admin", new Color(0x27, 0x27, 0x2A), new Color(0x3F, 0x3F, 0x46), Color.WHITE, 8);
        demoAdm.addActionListener(e -> {
            emailField.setText("admin@freshbasket.com");
            passwordField.setText("admin123");
            handleLogin();
        });

        demoRow.add(demoCust);
        demoRow.add(demoAdm);

        JButton createAccBtn = new JButton("New to FreshBasket? Create Account →");
        createAccBtn.setFont(Theme.FONT_SMALL_BOLD);
        createAccBtn.setForeground(Theme.BB_GREEN_LIGHT);
        createAccBtn.setContentAreaFilled(false);
        createAccBtn.setBorderPainted(false);
        createAccBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createAccBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createAccBtn.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });

        JLabel terms = new JLabel("<html><center><font color='#6B7280'>By continuing, I accept FreshBasket's Terms & Conditions and Privacy Policy.</font></center></html>");
        terms.setFont(Theme.FONT_SMALL);
        terms.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPanel.add(title);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(sub);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(underline);
        rightPanel.add(Box.createVerticalStrut(26));
        rightPanel.add(emailField);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(18));
        rightPanel.add(continueBtn);
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(demoRow);
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(createAccBtn);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(terms);

        root.add(leftPanel);
        root.add(rightPanel);
        setContentPane(root);
        getRootPane().setDefaultButton(continueBtn);
    }

    private JPanel createTrustBadge(String icon, String label, String sub) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel circle = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circle.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        circle.setForeground(Theme.BB_GREEN_DARK);
        circle.setPreferredSize(new Dimension(54, 54));
        circle.setMaximumSize(new Dimension(54, 54));
        circle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.TEXT_DARK);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(circle);
        p.add(Box.createVerticalStrut(8));
        p.add(lbl);
        p.add(subLbl);
        return p;
    }

    private void handleLogin() {
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());

        if (email.isBlank() || pass.isBlank()) {
            UIHelper.showError(this, "Please enter both email and password.");
            return;
        }
        try {
            User user = userDAO.authenticate(email, pass);
            if (user == null) {
                UIHelper.showError(this, "Invalid email or password.");
                return;
            }
            dispose();
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminDashboardFrame(user).setVisible(true);
            } else {
                new CustomerDashboardFrame(user).setVisible(true);
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Database connection failed: " + ex.getMessage());
        }
    }
}
