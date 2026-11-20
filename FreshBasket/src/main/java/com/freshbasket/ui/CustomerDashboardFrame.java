package com.freshbasket.ui;

import com.freshbasket.dao.ProductDAO;
import com.freshbasket.model.Product;
import com.freshbasket.model.User;
import com.freshbasket.util.CartManager;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomerDashboardFrame extends JFrame {
    private final User currentUser;
    private final ProductDAO productDAO = new ProductDAO();
    private final CartManager cartManager = CartManager.getInstance();

    private JTextField searchField;
    private JButton cartBtn;
    private JLabel sectionTitleLabel;
    private JPanel productGridPanel;

    private String selectedCategory = "All";
    private boolean filterUnder35 = false;
    private boolean filterTopDeals = false;

    public CustomerDashboardFrame(User user) {
        this.currentUser = user;
        setTitle("freshbasket — Online Grocery Delivery (" + user.getFullName() + ")");
        setSize(1320, 840);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_WHITE);

        // Sticky Top Header + Sub-Nav
        root.add(buildHeaderContainer(), BorderLayout.NORTH);

        // Scrollable Main Storefront Body
        JPanel bodyContent = new JPanel();
        bodyContent.setLayout(new BoxLayout(bodyContent, BoxLayout.Y_AXIS));
        bodyContent.setBackground(Theme.BG_WHITE);
        bodyContent.setBorder(new EmptyBorder(18, 42, 36, 42));

        bodyContent.add(buildPromoPillsStrip());
        bodyContent.add(Box.createVerticalStrut(20));
        bodyContent.add(buildTopOffersBannerRow());
        bodyContent.add(Box.createVerticalStrut(24));
        bodyContent.add(buildSmartBasketSection());

        JScrollPane scroll = new JScrollPane(bodyContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(Theme.BG_WHITE);

        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);

        loadProducts();
    }

    private JPanel buildHeaderContainer() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_WHITE);
        wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_LIGHT));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout(20, 0));
        topBar.setBackground(Theme.BG_WHITE);
        topBar.setBorder(new EmptyBorder(12, 42, 10, 42));

        JPanel logo = UIHelper.createBrandLogoPanel();
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                resetFilters();
            }
        });

        // Search Bar
        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setOpaque(false);
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search for Products... (apples, milk, basmati, chips)");
        searchField.setFont(Theme.FONT_BODY);
        searchField.setPreferredSize(new Dimension(420, 38));
        searchField.addActionListener(e -> loadProducts());

        JButton searchBtn = UIHelper.createSolidButton("Search", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 8);
        searchBtn.addActionListener(e -> loadProducts());
        searchWrap.add(searchField, BorderLayout.CENTER);
        searchWrap.add(searchBtn, BorderLayout.EAST);

        // Right Controls
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JPanel deliveryPill = new JPanel();
        deliveryPill.setLayout(new BoxLayout(deliveryPill, BoxLayout.Y_AXIS));
        deliveryPill.setBackground(Theme.BG_SURFACE);
        deliveryPill.setBorder(new EmptyBorder(4, 12, 4, 12));
        JLabel fastLbl = new JLabel("⚡ Delivery in 10 mins");
        fastLbl.setFont(Theme.FONT_SMALL_BOLD);
        fastLbl.setForeground(Theme.BB_GREEN_DARK);
        JLabel locLbl = new JLabel("Madurai • Wallet ₹" + String.format("%.0f", currentUser.getWalletBalance()));
        locLbl.setFont(Theme.FONT_SMALL);
        locLbl.setForeground(Theme.TEXT_MUTED);
        deliveryPill.add(fastLbl);
        deliveryPill.add(locLbl);

        JButton ordersBtn = UIHelper.createSolidButton("My Orders", Theme.BB_DARK, new Color(0x37, 0x41, 0x51), Color.WHITE, 8);
        ordersBtn.addActionListener(e -> new MyOrdersFrame(this, currentUser).setVisible(true));

        cartBtn = UIHelper.createSolidButton("🛒 Cart (0)", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        cartBtn.addActionListener(e -> new CartFrame(this, currentUser).setVisible(true));

        JButton logoutBtn = UIHelper.createOutlineButton("Logout", Theme.BORDER_CARD, Theme.TEXT_DARK, Theme.BB_RED_SOFT, Theme.BB_RED);
        logoutBtn.addActionListener(e -> {
            cartManager.clear();
            dispose();
            new LoginFrame().setVisible(true);
        });

        rightPanel.add(deliveryPill);
        rightPanel.add(ordersBtn);
        rightPanel.add(cartBtn);
        rightPanel.add(logoutBtn);

        topBar.add(logo, BorderLayout.WEST);
        topBar.add(searchWrap, BorderLayout.CENTER);
        topBar.add(rightPanel, BorderLayout.EAST);

        // Second Nav Row (Shop by Category Mega Menu + Quick Links)
        JPanel subNav = new JPanel(new BorderLayout());
        subNav.setBackground(Theme.BG_WHITE);
        subNav.setBorder(new EmptyBorder(4, 42, 10, 42));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftNav.setOpaque(false);

        JButton megaMenuBtn = UIHelper.createSolidButton("Shop by Category   ▼", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 6);
        megaMenuBtn.addActionListener(e -> showThreeColumnMegaMenu(megaMenuBtn));
        leftNav.add(megaMenuBtn);

        String[] quickCats = {"All", "Fruits & Vegetables", "Dairy & Bakery", "Staples & Grains", "Snacks & Beverages", "Household"};
        for (String cat : quickCats) {
            JButton link = new JButton(cat.equals("All") ? "Smart Basket" : cat);
            link.setFont(Theme.FONT_BODY);
            link.setForeground(Theme.TEXT_DARK);
            link.setContentAreaFilled(false);
            link.setBorderPainted(false);
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            link.addActionListener(e -> {
                selectedCategory = cat;
                filterUnder35 = false;
                filterTopDeals = false;
                sectionTitleLabel.setText(cat.equals("All") ? "My Smart Basket" : cat);
                loadProducts();
            });
            leftNav.add(link);
        }

        subNav.add(leftNav, BorderLayout.WEST);

        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(subNav, BorderLayout.SOUTH);
        return wrapper;
    }

    /**
     * Authentic 3-Column BigBasket Mega Menu (Dark Left | Light-Gray Middle | Pure White Right)
     */
    private void showThreeColumnMegaMenu(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Theme.BORDER_CARD, 1));

        JPanel container = new JPanel(new GridLayout(1, 3));
        container.setPreferredSize(new Dimension(690, 320));

        // Category Hierarchy Data
        Map<String, Map<String, String[]>> hierarchy = new LinkedHashMap<>();
        hierarchy.put("Fruits & Vegetables", Map.of(
                "Fresh Vegetables", new String[]{"Tomato", "Carrot", "Potato", "Coriander"},
                "Fresh Fruits", new String[]{"Apples", "Bananas"}
        ));
        hierarchy.put("Dairy & Bakery", Map.of(
                "Dairy Essentials", new String[]{"Milk", "Butter", "Paneer"},
                "Bakery & Eggs", new String[]{"Bread", "Eggs"}
        ));
        hierarchy.put("Staples & Grains", Map.of(
                "Rice, Atta & Dal", new String[]{"Basmati Rice", "Atta", "Toor Dal"},
                "Oils & Masalas", new String[]{"Sunflower Oil", "Turmeric"}
        ));
        hierarchy.put("Snacks & Beverages", Map.of(
                "Tea & Coffee", new String[]{"Tea", "Coffee"},
                "Juices & Chips", new String[]{"Aamras", "Chips"}
        ));
        hierarchy.put("Household", Map.of(
                "Laundry & Dishwash", new String[]{"Detergent", "Dishwash Gel"},
                "Cleaners & Tissues", new String[]{"Floor Cleaner", "Tissues"}
        ));

        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setBackground(Theme.BB_DARK);
        col1.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setBackground(Theme.BG_SURFACE);
        col2.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel col3 = new JPanel();
        col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS));
        col3.setBackground(Color.WHITE);
        col3.setBorder(new EmptyBorder(8, 8, 8, 8));

        Runnable[] populateCol2Ref = new Runnable[1];

        for (String mainCat : hierarchy.keySet()) {
            JButton b1 = new JButton(mainCat);
            b1.setFont(Theme.FONT_BODY_BOLD);
            b1.setForeground(Color.WHITE);
            b1.setBackground(Theme.BB_DARK);
            b1.setHorizontalAlignment(SwingConstants.LEFT);
            b1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            b1.setBorder(new EmptyBorder(6, 12, 6, 12));
            b1.setFocusPainted(false);
            b1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            MouseAdapter hover1 = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    for (Component c : col1.getComponents()) c.setBackground(Theme.BB_DARK);
                    b1.setBackground(new Color(0x38, 0x38, 0x38));
                    col2.removeAll();
                    col3.removeAll();

                    Map<String, String[]> subs = hierarchy.get(mainCat);
                    boolean firstSub = true;
                    for (Map.Entry<String, String[]> entry : subs.entrySet()) {
                        JButton b2 = new JButton(entry.getKey());
                        b2.setFont(Theme.FONT_BODY_BOLD);
                        b2.setForeground(Theme.TEXT_DARK);
                        b2.setBackground(firstSub ? Color.WHITE : Theme.BG_SURFACE);
                        b2.setHorizontalAlignment(SwingConstants.LEFT);
                        b2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                        b2.setBorder(new EmptyBorder(6, 12, 6, 12));
                        b2.setFocusPainted(false);
                        b2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        Runnable fillCol3 = () -> {
                            col3.removeAll();
                            for (String itemKw : entry.getValue()) {
                                JButton b3 = new JButton(itemKw);
                                b3.setFont(Theme.FONT_BODY);
                                b3.setForeground(Theme.TEXT_DARK);
                                b3.setBackground(Color.WHITE);
                                b3.setHorizontalAlignment(SwingConstants.LEFT);
                                b3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
                                b3.setBorder(new EmptyBorder(5, 12, 5, 12));
                                b3.setFocusPainted(false);
                                b3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                b3.addActionListener(ev -> {
                                    popup.setVisible(false);
                                    selectedCategory = mainCat;
                                    searchField.setText(itemKw);
                                    sectionTitleLabel.setText(mainCat + " › " + itemKw);
                                    loadProducts();
                                });
                                col3.add(b3);
                            }
                            col3.revalidate();
                            col3.repaint();
                        };

                        if (firstSub) {
                            fillCol3.run();
                            firstSub = false;
                        }

                        b2.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent ev) {
                                for (Component c : col2.getComponents()) c.setBackground(Theme.BG_SURFACE);
                                b2.setBackground(Color.WHITE);
                                fillCol3.run();
                            }
                        });
                        b2.addActionListener(ev -> {
                            popup.setVisible(false);
                            selectedCategory = mainCat;
                            searchField.setText("");
                            sectionTitleLabel.setText(mainCat + " › " + entry.getKey());
                            loadProducts();
                        });
                        col2.add(b2);
                    }
                    col2.revalidate();
                    col2.repaint();
                }
            };
            b1.addMouseListener(hover1);
            b1.addActionListener(ev -> {
                popup.setVisible(false);
                selectedCategory = mainCat;
                searchField.setText("");
                sectionTitleLabel.setText(mainCat);
                loadProducts();
            });
            col1.add(b1);
            if (populateCol2Ref[0] == null) {
                populateCol2Ref[0] = () -> hover1.mouseEntered(null);
            }
        }

        if (populateCol2Ref[0] != null) populateCol2Ref[0].run();

        container.add(col1);
        container.add(col2);
        container.add(col3);

        popup.add(container);
        popup.show(invoker, 0, invoker.getHeight() + 4);
    }

    private JPanel buildPromoPillsStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 6, 14, 0));
        strip.setOpaque(false);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        strip.add(createPromoPill("EGGS & DAIRY", Theme.BG_SURFACE, Theme.TEXT_DARK, () -> filterByCat("Dairy & Bakery", "Dairy & Bakery")));
        strip.add(createPromoPill("⚡ FRESH PASS", Theme.BB_NAVY, Color.WHITE, () -> {
            filterTopDeals = true; filterUnder35 = false; selectedCategory = "All";
            sectionTitleLabel.setText("FreshPass Exclusive Deals");
            loadProducts();
        }));
        strip.add(createPromoPill("ORGANIC", new Color(0x4D, 0x6B, 0x1A), Color.WHITE, () -> filterByCat("Fruits & Vegetables", "Organic Fruits & Veggies")));
        strip.add(createPromoPill("BUY MORE SAVE MORE", Theme.BG_SURFACE, Theme.TEXT_DARK, () -> filterByCat("Staples & Grains", "Daily Staples")));
        strip.add(createPromoPill("DEALS OF THE WEEK", Theme.BG_SURFACE, Theme.TEXT_DARK, () -> {
            filterTopDeals = true; filterUnder35 = false; selectedCategory = "All";
            sectionTitleLabel.setText("Deals of the Week (18%+ OFF)");
            loadProducts();
        }));
        strip.add(createPromoPill("SNACKS STORE", Theme.BG_SURFACE, Theme.TEXT_DARK, () -> filterByCat("Snacks & Beverages", "Snacks & Beverages")));

        return strip;
    }

    private JButton createPromoPill(String text, Color bg, Color fg, Runnable action) {
        JButton b = UIHelper.createSolidButton(text, bg, bg.darker(), fg, 10);
        b.setFont(Theme.FONT_SMALL_BOLD);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JPanel buildTopOffersBannerRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

        row.add(createRedTopOfferCard("DEALS OF", "THE WEEK", "View offers >", () -> {
            filterTopDeals = true; filterUnder35 = false; selectedCategory = "All";
            sectionTitleLabel.setText("Deals of the Week");
            loadProducts();
        }));
        row.add(createRedTopOfferCard("BIG PACK", "BIGGER DISCOUNTS", "View offers >", () -> filterByCat("Staples & Grains", "Big Pack Staples")));
        row.add(createRedTopOfferCard("COMBOS", "YOU CAN'T MISS", "View offers >", () -> filterByCat("Snacks & Beverages", "Combos & Beverages")));
        row.add(createRedTopOfferCard("THE", "₹30 CORNER", "View offers >", () -> {
            filterUnder35 = true; filterTopDeals = false; selectedCategory = "All";
            sectionTitleLabel.setText("The ₹30 Budget Corner");
            loadProducts();
        }));

        return row;
    }

    private JPanel createRedTopOfferCard(String topText, String bottomText, String linkText, Runnable onClick) {
        UIHelper.ShadowCard card = new UIHelper.ShadowCard(12, Color.WHITE, Theme.BORDER_CARD);
        card.enableHoverBorder(Theme.BB_RED);
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel redTop = new JPanel(new BorderLayout());
        redTop.setBackground(new Color(0xDC, 0x26, 0x26));
        redTop.setBorder(new EmptyBorder(16, 16, 8, 16));
        JLabel tLbl = new JLabel(topText);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tLbl.setForeground(Color.WHITE);
        redTop.add(tLbl, BorderLayout.SOUTH);

        JPanel whiteBottom = new JPanel(new BorderLayout());
        whiteBottom.setOpaque(false);
        whiteBottom.setBorder(new EmptyBorder(8, 16, 12, 16));
        JLabel bLbl = new JLabel(bottomText);
        bLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bLbl.setForeground(Theme.BB_DARK);

        JLabel link = new JLabel(linkText);
        link.setFont(Theme.FONT_SMALL_BOLD);
        link.setForeground(Theme.TEXT_MUTED);

        whiteBottom.add(bLbl, BorderLayout.NORTH);
        whiteBottom.add(link, BorderLayout.SOUTH);

        card.add(redTop, BorderLayout.NORTH);
        card.add(whiteBottom, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return card;
    }

    private JPanel buildSmartBasketSection() {
        UIHelper.ShadowCard section = new UIHelper.ShadowCard(14, Theme.BG_PAGE, Theme.BORDER_LIGHT);
        section.setLayout(new BorderLayout(0, 14));
        section.setBorder(new EmptyBorder(18, 20, 22, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        sectionTitleLabel = new JLabel("My Smart Basket");
        sectionTitleLabel.setFont(Theme.FONT_H1);
        sectionTitleLabel.setForeground(Theme.TEXT_DARK);

        JButton viewAllBtn = UIHelper.createOutlineButton("View All Products", Theme.BORDER_CARD, Theme.TEXT_DARK, Theme.BB_DARK, Color.WHITE);
        viewAllBtn.addActionListener(e -> resetFilters());

        header.add(sectionTitleLabel, BorderLayout.WEST);
        header.add(viewAllBtn, BorderLayout.EAST);

        productGridPanel = new JPanel(new GridLayout(0, 4, 16, 16));
        productGridPanel.setOpaque(false);

        section.add(header, BorderLayout.NORTH);
        section.add(productGridPanel, BorderLayout.CENTER);
        return section;
    }

    private void filterByCat(String cat, String title) {
        selectedCategory = cat;
        filterUnder35 = false;
        filterTopDeals = false;
        searchField.setText("");
        sectionTitleLabel.setText(title);
        loadProducts();
    }

    private void resetFilters() {
        selectedCategory = "All";
        filterUnder35 = false;
        filterTopDeals = false;
        searchField.setText("");
        sectionTitleLabel.setText("My Smart Basket");
        loadProducts();
    }

    public void refreshCartBadge() {
        int count = cartManager.getTotalItemCount();
        cartBtn.setText(count > 0
                ? String.format("🛒 Cart (%d) • ₹%.0f", count, cartManager.getSubtotal())
                : "🛒 Cart (0)");
    }

    public void loadProducts() {
        productGridPanel.removeAll();
        try {
            List<Product> list = productDAO.searchProducts(searchField.getText(), selectedCategory, filterUnder35, filterTopDeals);
            if (list.isEmpty()) {
                JLabel empty = new JLabel("No products found for this filter. Click 'View All Products'.");
                empty.setFont(Theme.FONT_SUBTITLE);
                empty.setForeground(Theme.TEXT_MUTED);
                productGridPanel.add(empty);
            } else {
                for (Product p : list) {
                    productGridPanel.add(buildBigBasketProductCard(p));
                }
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Error loading catalog: " + ex.getMessage());
        }
        productGridPanel.revalidate();
        productGridPanel.repaint();
        refreshCartBadge();
    }

    private JPanel buildBigBasketProductCard(Product p) {
        UIHelper.ShadowCard card = new UIHelper.ShadowCard(12, Color.WHITE, Theme.BORDER_CARD);
        card.enableHoverBorder(Theme.BB_GREEN);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 12, 14, 12));

        // Product Vector Studio Image
        JPanel art = UIHelper.createProductArtPanel(p.getName(), p.getCategory(), p.getDiscountPercent());
        art.setPreferredSize(new Dimension(240, 140));

        // Middle Metadata
        JPanel meta = new JPanel();
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.setOpaque(false);

        JPanel brandEtaRow = new JPanel(new BorderLayout());
        brandEtaRow.setOpaque(false);
        JLabel brandLbl = new JLabel(p.getBadgeTag());
        brandLbl.setFont(Theme.FONT_SMALL);
        brandLbl.setForeground(Theme.TEXT_LIGHT);

        JLabel etaLbl = new JLabel("⚡ 10 MINS");
        etaLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        etaLbl.setForeground(Theme.BB_GREEN_DARK);
        etaLbl.setOpaque(true);
        etaLbl.setBackground(Theme.BB_GREEN_SOFT);
        etaLbl.setBorder(new EmptyBorder(2, 6, 2, 6));

        brandEtaRow.add(brandLbl, BorderLayout.WEST);
        brandEtaRow.add(etaLbl, BorderLayout.EAST);

        JLabel nameLbl = new JLabel("<html><body style='width: 205px'>" + p.getName() + "</body></html>");
        nameLbl.setFont(Theme.FONT_SUBTITLE);
        nameLbl.setForeground(Theme.TEXT_DARK);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel weightPill = new JLabel("  " + p.getUnitWeight() + "  •  " + (p.getStockQty() <= 10 ? "Only " + p.getStockQty() + " left!" : "In Stock"));
        weightPill.setFont(Theme.FONT_SMALL);
        weightPill.setForeground(p.getStockQty() <= 10 ? Theme.BB_RED : Theme.TEXT_MUTED);
        weightPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                new EmptyBorder(4, 6, 4, 8)
        ));
        weightPill.setAlignmentX(Component.LEFT_ALIGNMENT);

        meta.add(brandEtaRow);
        meta.add(Box.createVerticalStrut(4));
        meta.add(nameLbl);
        meta.add(Box.createVerticalStrut(8));
        meta.add(weightPill);

        // Bottom Price + BigBasket Red Outline Add / Stepper
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel priceStack = new JPanel();
        priceStack.setLayout(new BoxLayout(priceStack, BoxLayout.Y_AXIS));
        priceStack.setOpaque(false);
        JLabel priceLbl = new JLabel(String.format("₹%.2f", p.getSellingPrice()));
        priceLbl.setFont(Theme.FONT_PRICE);
        priceLbl.setForeground(Theme.TEXT_DARK);
        priceStack.add(priceLbl);
        if (p.getMrp() > p.getSellingPrice()) {
            JLabel mrpLbl = new JLabel(String.format("<html><strike>₹%.2f</strike></html>", p.getMrp()));
            mrpLbl.setFont(Theme.FONT_SMALL);
            mrpLbl.setForeground(Theme.TEXT_LIGHT);
            priceStack.add(mrpLbl);
        }

        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionWrap.setOpaque(false);

        int qty = cartManager.getQuantity(p.getProductId());
        if (p.getStockQty() <= 0) {
            JButton oos = UIHelper.createOutlineButton("Sold Out", Theme.BORDER_CARD, Theme.TEXT_LIGHT, Theme.BG_SURFACE, Theme.TEXT_LIGHT);
            oos.setEnabled(false);
            actionWrap.add(oos);
        } else if (qty == 0) {
            JButton addBtn = UIHelper.createOutlineButton("Add", Theme.BB_RED, Theme.BB_RED, Theme.BB_RED, Color.WHITE);
            addBtn.setPreferredSize(new Dimension(92, 34));
            addBtn.addActionListener(e -> {
                cartManager.addProduct(p);
                loadProducts();
            });
            actionWrap.add(addBtn);
        } else {
            JButton minus = UIHelper.createSolidButton("-", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 6);
            minus.setBorder(new EmptyBorder(5, 11, 5, 11));
            minus.addActionListener(e -> {
                cartManager.decrementProduct(p.getProductId());
                loadProducts();
            });

            JLabel qLbl = new JLabel(String.valueOf(qty));
            qLbl.setFont(Theme.FONT_BODY_BOLD);
            qLbl.setForeground(Theme.BB_RED);
            qLbl.setBorder(new EmptyBorder(0, 6, 0, 6));

            JButton plus = UIHelper.createSolidButton("+", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 6);
            plus.setBorder(new EmptyBorder(5, 11, 5, 11));
            plus.addActionListener(e -> {
                if (qty < p.getStockQty()) {
                    cartManager.addProduct(p);
                    loadProducts();
                } else {
                    UIHelper.showInfo(this, "Max available stock reached.");
                }
            });
            actionWrap.add(minus);
            actionWrap.add(qLbl);
            actionWrap.add(plus);
        }

        bottom.add(priceStack, BorderLayout.WEST);
        bottom.add(actionWrap, BorderLayout.EAST);

        card.add(art, BorderLayout.NORTH);
        card.add(meta, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }
}
