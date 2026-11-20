package com.freshbasket.ui;

import com.freshbasket.model.Order;
import com.freshbasket.model.OrderItem;
import com.freshbasket.util.QRCodeGenerator;
import com.freshbasket.util.Theme;
import com.freshbasket.util.UIHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ReceiptFrame extends JDialog {
    private final Order order;
    private BufferedImage qrImage;

    public ReceiptFrame(Frame owner, Order order) {
        super(owner, "FreshBasket — Digital Tax Invoice & QR Receipt", true);
        this.order = order;

        setSize(540, 710);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG_PAGE);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));

        UIHelper.ShadowCard card = new UIHelper.ShadowCard(14, Color.WHITE, Theme.BB_GREEN);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel ok = new JLabel("✓ ORDER CONFIRMED");
        ok.setFont(Theme.FONT_H1);
        ok.setForeground(Theme.BB_GREEN_DARK);
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel code = new JLabel("Invoice #" + order.getOrderCode());
        code.setFont(Theme.FONT_SUBTITLE);
        code.setForeground(Theme.TEXT_DARK);
        code.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel meta = new JLabel(order.getDeliverySlot() + "  •  " + order.getPaymentMode());
        meta.setFont(Theme.FONT_SMALL);
        meta.setForeground(Theme.TEXT_MUTED);
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-26s %5s %10s\n", "ITEM", "QTY", "AMOUNT"));
        sb.append("--------------------------------------------\n");
        for (OrderItem i : order.getItems()) {
            String nm = i.getProductName().length() > 24 ? i.getProductName().substring(0, 23) + "…" : i.getProductName();
            sb.append(String.format("%-26s %5d %10.2f\n", nm, i.getQuantity(), i.getTotalPrice()));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-32s %10.2f\n", "Subtotal:", order.getSubtotal()));
        sb.append(String.format("%-32s %10.2f\n", "GST (5%):", order.getGstAmount()));
        sb.append(String.format("%-32s %10.2f\n", "Delivery Fee:", order.getDeliveryFee()));
        sb.append(String.format("%-32s -%9.2f\n", "Promo Savings:", order.getDiscountAmount()));
        sb.append("============================================\n");
        sb.append(String.format("%-32s ₹%9.2f\n", "GRAND TOTAL:", order.getGrandTotal()));

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBackground(Theme.BG_SURFACE);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane sp = new JScrollPane(area);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        JLabel qrLbl = new JLabel();
        qrLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            String payload = "FRESHBASKET|" + order.getOrderCode() + "|INR_" + String.format("%.2f", order.getGrandTotal()) + "|" + order.getStatus();
            qrImage = QRCodeGenerator.generateQRImage(payload, 160, 160);
            qrLbl.setIcon(new ImageIcon(qrImage));
        } catch (Exception ignored) {}

        JLabel hint = new JLabel("Scan QR at Doorstep for Contactless Delivery Verification");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(ok);
        card.add(Box.createVerticalStrut(4));
        card.add(code);
        card.add(Box.createVerticalStrut(2));
        card.add(meta);
        card.add(Box.createVerticalStrut(14));
        card.add(sp);
        card.add(Box.createVerticalStrut(14));
        card.add(qrLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        JButton saveBtn = UIHelper.createSolidButton("Download QR PNG", Theme.BB_GREEN, Theme.BB_GREEN_DARK, Color.WHITE, 8);
        saveBtn.addActionListener(e -> saveQr());
        JButton doneBtn = UIHelper.createSolidButton("Done", Theme.BB_DARK, Theme.BB_BLACK, Color.WHITE, 8);
        doneBtn.addActionListener(e -> dispose());
        btns.add(saveBtn);
        btns.add(doneBtn);

        root.add(card, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void saveQr() {
        if (qrImage == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(order.getOrderCode() + "-QR.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(qrImage, "PNG", fc.getSelectedFile());
                UIHelper.showInfo(this, "Saved QR Receipt to " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                UIHelper.showError(this, "Save failed: " + ex.getMessage());
            }
        }
    }
}
