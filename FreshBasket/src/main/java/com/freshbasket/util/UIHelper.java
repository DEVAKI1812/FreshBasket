package com.freshbasket.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class UIHelper {

    public static class ShadowCard extends JPanel {
        private final int radius;
        private Color bgColor;
        private Color borderColor;
        private boolean hoverHighlight = false;

        public ShadowCard(int radius, Color bgColor, Color borderColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        public void enableHoverBorder(Color hoverColor) {
            Color original = this.borderColor;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hoverHighlight = true;
                    borderColor = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverHighlight = false;
                    borderColor = original;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Soft elevation drop shadow
            g2.setColor(new Color(0, 0, 0, hoverHighlight ? 22 : 11));
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, radius, radius);

            Shape round = new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 4, radius, radius);
            g2.setColor(bgColor);
            g2.fill(round);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(hoverHighlight ? 1.5f : 1.0f));
                g2.draw(round);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel createBrandLogoPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BB_GREEN_LIGHT);
                g2.fillRoundRect(0, 2, 34, 34, 8, 8);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.setColor(Theme.BB_RED);
                g2.drawString("f", 7, 26);
                g2.setColor(Theme.BB_BLACK);
                g2.drawString("b", 16, 26);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(36, 38));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel title = new JLabel("<html><font color='#CC0000'><b>fresh</b></font><font color='#202020'><b>basket</b></font></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel("A SMART GROCERY APP");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 8));
        sub.setForeground(Theme.BB_GREEN_DARK);

        textCol.add(title);
        textCol.add(sub);

        p.add(badge);
        p.add(textCol);
        return p;
    }

    public static JButton createSolidButton(String text, Color bg, Color hoverBg, Color fg, int radius) {
        JButton btn = new JButton(text) {
            private Color cur = bg;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) { cur = hoverBg; repaint(); }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        cur = bg; repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? cur : new Color(0xD1, 0xD5, 0xDB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createOutlineButton(String text, Color borderCol, Color fg, Color hoverBg, Color hoverFg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) { hovered = true; setForeground(hoverFg); repaint(); }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false; setForeground(fg); repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(borderCol);
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }

    /**
     * Custom vector-illustrated studio product art so every card looks vibrant & recognizable without external image URLs.
     */
    public static JPanel createProductArtPanel(String productName, String category, int discountPct) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Soft studio photo backdrop
                GradientPaint bg = new GradientPaint(0, 0, new Color(0xFA, 0xF8, 0xF5), 0, h, new Color(0xF1, 0xEC, 0xE4));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                // Subtle ground shadow under item
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillOval(w / 2 - 44, h - 26, 88, 12);

                int cx = w / 2;
                int cy = h / 2 + 4;
                String lower = productName.toLowerCase();

                if (lower.contains("apple")) {
                    // Red Kashmiri Apples
                    drawApple(g2, cx - 18, cy, 24, new Color(0xD9, 0x26, 0x26));
                    drawApple(g2, cx + 16, cy + 4, 22, new Color(0xEF, 0x44, 0x44));
                } else if (lower.contains("tomato")) {
                    // Cluster of bright tomatoes
                    drawTomato(g2, cx - 22, cy + 4, 22);
                    drawTomato(g2, cx + 20, cy + 6, 22);
                    drawTomato(g2, cx, cy - 8, 25);
                } else if (lower.contains("carrot")) {
                    // Orange Carrots
                    g2.setColor(new Color(0x16, 0xA3, 0x4A));
                    g2.fillOval(cx + 16, cy - 26, 22, 10);
                    g2.fillOval(cx + 18, cy - 16, 20, 9);
                    g2.setColor(new Color(0xF9, 0x73, 0x16));
                    g2.fillPolygon(new int[]{cx - 34, cx + 18, cx + 22}, new int[]{cy + 20, cy - 16, cy - 2}, 3);
                    g2.setColor(new Color(0xEA, 0x58, 0x0C));
                    g2.fillPolygon(new int[]{cx - 26, cx + 24, cx + 28}, new int[]{cy + 26, cy - 4, cy + 10}, 3);
                } else if (lower.contains("banana")) {
                    // Golden Bananas
                    g2.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(0xEA, 0xB3, 0x08));
                    g2.drawArc(cx - 34, cy - 22, 68, 42, 200, 145);
                    g2.setColor(new Color(0xFA, 0xCC, 0x15));
                    g2.drawArc(cx - 28, cy - 14, 62, 38, 200, 145);
                } else if (lower.contains("coriander") || lower.contains("spinach")) {
                    // Fresh Green Herb Bundle on wooden board
                    g2.setColor(new Color(0xB4, 0x7B, 0x44));
                    g2.fillOval(cx - 42, cy - 10, 84, 34);
                    g2.setColor(new Color(0x15, 0x80, 0x3D));
                    for (int i = -24; i <= 24; i += 10) {
                        g2.fillOval(cx + i - 10, cy - 20 + Math.abs(i / 4), 22, 22);
                    }
                    g2.setColor(new Color(0x22, 0xC5, 0x5E));
                    for (int i = -18; i <= 18; i += 9) {
                        g2.fillOval(cx + i - 7, cy - 14, 16, 16);
                    }
                } else if (lower.contains("potato")) {
                    // Potatoes
                    g2.setColor(new Color(0xC2, 0x9B, 0x6B));
                    g2.fillOval(cx - 34, cy - 8, 36, 26);
                    g2.setColor(new Color(0xD4, 0xAF, 0x7A));
                    g2.fillOval(cx - 2, cy - 2, 38, 28);
                    g2.setColor(new Color(0xE8, 0xD3, 0xA2));
                    g2.fillOval(cx - 14, cy - 20, 32, 24);
                } else if (lower.contains("milk")) {
                    // Milk Carton
                    g2.setColor(new Color(0x25, 0x63, 0xEB));
                    g2.fillRoundRect(cx - 22, cy - 26, 44, 54, 6, 6);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(cx - 22, cy - 6, 44, 20);
                    g2.setColor(new Color(0x1D, 0x4E, 0xD8));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString("MILK", cx - 13, cy + 8);
                } else if (lower.contains("butter") || lower.contains("paneer")) {
                    // Yellow/Cream Pack
                    Color packCol = lower.contains("butter") ? new Color(0xFA, 0xCC, 0x15) : new Color(0x38, 0xBD, 0xF8);
                    g2.setColor(packCol);
                    g2.fillRoundRect(cx - 34, cy - 18, 68, 38, 8, 8);
                    g2.setColor(new Color(0xCC, 0x00, 0x00));
                    g2.fillRoundRect(cx - 24, cy - 8, 48, 18, 4, 4);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.drawString(lower.contains("butter") ? "BUTTER" : "PANEER", cx - 18, cy + 4);
                } else if (lower.contains("bread")) {
                    // Whole Wheat Bread Loaf
                    g2.setColor(new Color(0x92, 0x40, 0x0E));
                    g2.fillRoundRect(cx - 32, cy - 16, 64, 38, 14, 14);
                    g2.setColor(new Color(0xD9, 0x77, 0x06));
                    g2.fillRoundRect(cx - 28, cy - 10, 56, 28, 10, 10);
                } else if (lower.contains("egg")) {
                    // Brown Eggs Tray
                    g2.setColor(new Color(0xD6, 0xD3, 0xD1));
                    g2.fillRoundRect(cx - 38, cy - 6, 76, 26, 8, 8);
                    g2.setColor(new Color(0xC2, 0x78, 0x49));
                    g2.fillOval(cx - 30, cy - 18, 18, 22);
                    g2.fillOval(cx - 9, cy - 18, 18, 22);
                    g2.fillOval(cx + 12, cy - 18, 18, 22);
                } else if (category.equals("Staples & Grains")) {
                    // Grain Sack / Bowl
                    g2.setColor(new Color(0xB4, 0x53, 0x09));
                    g2.fillRoundRect(cx - 26, cy - 26, 52, 54, 10, 10);
                    g2.setColor(new Color(0xFE, 0xF3, 0xC7));
                    g2.fillRoundRect(cx - 20, cy - 12, 40, 26, 6, 6);
                    g2.setColor(new Color(0x92, 0x40, 0x0E));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.drawString("ROYAL", cx - 15, cy + 4);
                } else if (category.equals("Snacks & Beverages")) {
                    // Beverage Bottle / Snack Pack
                    g2.setColor(new Color(0xDC, 0x26, 0x26));
                    g2.fillRoundRect(cx - 24, cy - 26, 48, 54, 10, 10);
                    g2.setColor(new Color(0xFA, 0xCC, 0x15));
                    g2.fillOval(cx - 16, cy - 12, 32, 24);
                    g2.setColor(Theme.BB_DARK);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.drawString("PACK", cx - 12, cy + 3);
                } else {
                    // Household Bottle
                    g2.setColor(new Color(0x02, 0x84, 0xC7));
                    g2.fillRoundRect(cx - 20, cy - 20, 40, 48, 12, 12);
                    g2.setColor(new Color(0x03, 0x69, 0xA1));
                    g2.fillRect(cx - 8, cy - 30, 16, 12);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(cx - 14, cy - 6, 28, 18, 4, 4);
                }

                // Top-left BigBasket Olive-Green Discount Ribbon
                if (discountPct > 0) {
                    g2.setColor(Theme.BB_GREEN_DARK);
                    g2.fillRoundRect(0, 0, 68, 22, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.setFont(Theme.FONT_SMALL_BOLD);
                    g2.drawString(discountPct + "% OFF", 10, 15);
                }

                // Bottom-left Veg Indicator Square
                int vx = 8, vy = h - 20;
                g2.setColor(Color.WHITE);
                g2.fillRect(vx, vy, 14, 14);
                g2.setColor(new Color(0x16, 0xA3, 0x4A));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRect(vx, vy, 14, 14);
                g2.fill(new Ellipse2D.Double(vx + 3.5, vy + 3.5, 7, 7));

                g2.dispose();
            }

            private void drawApple(Graphics2D g2, int x, int y, int r, Color col) {
                g2.setColor(col);
                g2.fillOval(x - r, y - r, r * 2, r * 2);
                g2.setColor(new Color(255, 255, 255, 75));
                g2.fillOval(x - r + 5, y - r + 5, r / 2, r / 2);
                g2.setColor(new Color(0x16, 0xA3, 0x4A));
                g2.fillOval(x, y - r - 5, 10, 6);
            }

            private void drawTomato(Graphics2D g2, int x, int y, int r) {
                g2.setColor(new Color(0xE1, 0x1D, 0x48));
                g2.fillOval(x - r, y - r, r * 2, r * 2);
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fillOval(x - r + 5, y - r + 4, r / 2, r / 3);
                g2.setColor(new Color(0x15, 0x80, 0x3D));
                g2.fillRect(x - 4, y - r - 2, 8, 5);
            }
        };
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "FreshBasket — Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "FreshBasket", JOptionPane.INFORMATION_MESSAGE);
    }
}
