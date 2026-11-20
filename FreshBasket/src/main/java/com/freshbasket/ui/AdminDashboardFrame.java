package com.freshbasket.ui;

import com.freshbasket.dao.OrderDAO;
import com.freshbasket.dao.ProductDAO;
import com.freshbasket.model.Order;
import com.freshbasket.model.Product;
import com.freshbasket.model.User;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private JLabel revKpi, ordKpi, lowKpi, catKpi;
    private DefaultTableModel prodModel, ordModel;
    private JTable prodTable, ordTable;

    public AdminDashboardFrame(User adminUser) {
        setTitle("FreshBasket — Store Operations & Admin Console");
        setSize(1240, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.BG_PAGE);
        root.setBorder(new EmptyBorder(18, 28, 22, 28));

        // Top Bar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UIHelper.createBrandLogoPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel adminLbl = new JLabel("Admin: " + adminUser.getFullName());
        adminLbl.setFont(Theme.FONT_BODY_BOLD);
        adminLbl.setForeground(Theme.TEXT_MUTED);

        JButton refBtn = UIHelper.createOutlineButton("Refresh", Theme.BORDER_CARD, Theme.TEXT_DARK, Theme.BG_SURFACE, Theme.TEXT_DARK);
        refBtn.addActionListener(e -> reloadData());

        JButton outBtn = UIHelper.createSolidButton("Logout", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        outBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        right.add(adminLbl); right.add(refBtn); right.add(outBtn);
        top.add(right, BorderLayout.EAST);

        // KPI Row
        JPanel kpis = new JPanel(new GridLayout(1, 4, 16, 0));
        kpis.setOpaque(false);
        revKpi = new JLabel("₹0.00");
        ordKpi = new JLabel("0");
        lowKpi = new JLabel("0");
        catKpi = new JLabel("0");

        kpis.add(makeKpi("TOTAL REVENUE", revKpi, Theme.BB_GREEN_DARK));
        kpis.add(makeKpi("TOTAL ORDERS", ordKpi, Theme.BB_NAVY));
        kpis.add(makeKpi("LOW STOCK ALERTS (≤10)", lowKpi, Theme.BB_RED));
        kpis.add(makeKpi("ACTIVE CATALOG ITEMS", catKpi, Theme.BB_GOLD));

        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(top, BorderLayout.NORTH);
        north.add(kpis, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_SUBTITLE);
        tabs.addTab("Inventory & Catalog CRUD", buildInvTab());
        tabs.addTab("Order Fulfillment & Dispatch", buildOrdTab());

        root.add(north, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
        reloadData();
    }

    private JPanel makeKpi(String title, JLabel val, Color accent) {
        UIHelper.ShadowCard c = new UIHelper.ShadowCard(12, Color.WHITE, Theme.BORDER_CARD);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL_BOLD);
        t.setForeground(Theme.TEXT_MUTED);
        val.setFont(new Font("Segoe UI", Font.BOLD, 24));
        val.setForeground(accent);
        c.add(t); c.add(Box.createVerticalStrut(6)); c.add(val);
        return c;
    }

    private JPanel buildInvTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_PAGE);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        prodModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Category", "Unit", "MRP (₹)", "Selling Price (₹)", "Stock", "Brand"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        prodTable = new JTable(prodModel);
        prodTable.setRowHeight(32);
        prodTable.setFont(Theme.FONT_BODY);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        JButton add = UIHelper.createSolidButton("+ Add Item", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 8);
        add.addActionListener(e -> openEditor(null));

        JButton edit = UIHelper.createSolidButton("Edit / Restock", Theme.BB_DARK, Theme.BB_BLACK, Color.WHITE, 8);
        edit.addActionListener(e -> {
            int r = prodTable.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select a row first."); return; }
            openEditor(new Product(
                    (int) prodModel.getValueAt(r, 0), (String) prodModel.getValueAt(r, 1),
                    (String) prodModel.getValueAt(r, 2), (String) prodModel.getValueAt(r, 3),
                    Double.parseDouble(prodModel.getValueAt(r, 4).toString()),
                    Double.parseDouble(prodModel.getValueAt(r, 5).toString()),
                    Integer.parseInt(prodModel.getValueAt(r, 6).toString()),
                    (String) prodModel.getValueAt(r, 7)));
        });

        JButton del = UIHelper.createSolidButton("Delete Item", Theme.BB_RED, Theme.BB_RED_HOVER, Color.WHITE, 8);
        del.addActionListener(e -> {
            int r = prodTable.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select a row first."); return; }
            int id = (int) prodModel.getValueAt(r, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete product #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { productDAO.deleteProduct(id); reloadData(); }
                catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
            }
        });

        bar.add(add); bar.add(edit); bar.add(del);
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(prodTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildOrdTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_PAGE);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        ordModel = new DefaultTableModel(new String[]{"Order ID", "Code", "Customer", "Total (₹)", "Slot", "Payment", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ordTable = new JTable(ordModel);
        ordTable.setRowHeight(32);
        ordTable.setFont(Theme.FONT_BODY);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Order Placed", "Packed", "Out for Delivery", "Delivered"});
        JButton upd = UIHelper.createSolidButton("Update Order Status", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 8);
        upd.addActionListener(e -> {
            int r = ordTable.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select an order first."); return; }
            try {
                orderDAO.updateOrderStatus((int) ordModel.getValueAt(r, 0), (String) statusBox.getSelectedItem());
                reloadData();
                UIHelper.showInfo(this, "Status updated!");
            } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
        });

        bar.add(new JLabel("Fulfillment Stage:")); bar.add(statusBox); bar.add(upd);
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(ordTable), BorderLayout.CENTER);
        return p;
    }

    private void openEditor(Product ex) {
        JTextField nF = new JTextField(ex != null ? ex.getName() : "");
        JComboBox<String> cC = new JComboBox<>(new String[]{"Fruits & Vegetables", "Dairy & Bakery", "Staples & Grains", "Snacks & Beverages", "Household"});
        if (ex != null) cC.setSelectedItem(ex.getCategory());
        JTextField uF = new JTextField(ex != null ? ex.getUnitWeight() : "1 kg");
        JTextField mF = new JTextField(ex != null ? String.valueOf(ex.getMrp()) : "100.00");
        JTextField sF = new JTextField(ex != null ? String.valueOf(ex.getSellingPrice()) : "85.00");
        JTextField qF = new JTextField(ex != null ? String.valueOf(ex.getStockQty()) : "50");
        JTextField bF = new JTextField(ex != null ? ex.getBadgeTag() : "fresho!");

        JPanel f = new JPanel(new GridLayout(0, 2, 8, 8));
        f.add(new JLabel("Name:")); f.add(nF);
        f.add(new JLabel("Category:")); f.add(cC);
        f.add(new JLabel("Unit:")); f.add(uF);
        f.add(new JLabel("MRP (₹):")); f.add(mF);
        f.add(new JLabel("Selling Price (₹):")); f.add(sF);
        f.add(new JLabel("Stock Qty:")); f.add(qF);
        f.add(new JLabel("Brand:")); f.add(bF);

        if (JOptionPane.showConfirmDialog(this, f, ex == null ? "Add Product" : "Edit Product", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Product p = new Product(ex != null ? ex.getProductId() : 0, nF.getText().trim(), (String) cC.getSelectedItem(),
                        uF.getText().trim(), Double.parseDouble(mF.getText()), Double.parseDouble(sF.getText()),
                        Integer.parseInt(qF.getText()), bF.getText().trim());
                if (ex == null) productDAO.addProduct(p); else productDAO.updateProduct(p);
                reloadData();
            } catch (Exception e) { UIHelper.showError(this, e.getMessage()); }
        }
    }

    private void reloadData() {
        try {
            List<Product> prods = productDAO.getAllProducts();
            List<Order> ords = orderDAO.getAllOrders();
            prodModel.setRowCount(0);
            int low = 0;
            for (Product p : prods) {
                if (p.getStockQty() <= 10) low++;
                prodModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getCategory(), p.getUnitWeight(),
                        String.format("%.2f", p.getMrp()), String.format("%.2f", p.getSellingPrice()), p.getStockQty(), p.getBadgeTag()});
            }
            ordModel.setRowCount(0);
            double rev = 0;
            for (Order o : ords) {
                rev += o.getGrandTotal();
                ordModel.addRow(new Object[]{o.getOrderId(), o.getOrderCode(), o.getCustomerName(),
                        String.format("%.2f", o.getGrandTotal()), o.getDeliverySlot(), o.getPaymentMode(), o.getStatus()});
            }
            revKpi.setText(String.format("₹%.2f", rev));
            ordKpi.setText(String.valueOf(ords.size()));
            lowKpi.setText(String.valueOf(low));
            catKpi.setText(String.valueOf(prods.size()));
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }
}
