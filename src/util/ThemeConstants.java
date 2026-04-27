package util;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized UI theming constants for a modern, dark-themed Swing application.
 */
public class ThemeConstants {

    private ThemeConstants() {}

    // ── Color Palette ──
    public static final Color BG_DARK       = new Color(18, 18, 24);
    public static final Color BG_PANEL      = new Color(26, 27, 38);
    public static final Color BG_CARD       = new Color(36, 38, 52);
    public static final Color BG_INPUT      = new Color(44, 46, 62);
    public static final Color BG_HOVER      = new Color(52, 54, 72);

    public static final Color ACCENT_BLUE   = new Color(99, 134, 255);
    public static final Color ACCENT_PURPLE = new Color(138, 99, 255);
    public static final Color ACCENT_GREEN  = new Color(80, 200, 120);
    public static final Color ACCENT_ORANGE = new Color(255, 159, 67);
    public static final Color ACCENT_RED    = new Color(255, 85, 85);

    public static final Color TEXT_PRIMARY   = new Color(230, 233, 240);
    public static final Color TEXT_SECONDARY = new Color(150, 155, 170);
    public static final Color TEXT_MUTED     = new Color(100, 105, 120);

    public static final Color BORDER_COLOR  = new Color(58, 60, 78);

    // ── Priority Colors ──
    public static final Color PRIORITY_HIGH   = new Color(255, 85, 85);
    public static final Color PRIORITY_MEDIUM = new Color(255, 159, 67);
    public static final Color PRIORITY_LOW    = new Color(80, 200, 120);

    // ── Status Colors ──
    public static final Color STATUS_OPEN        = new Color(99, 134, 255);
    public static final Color STATUS_IN_PROGRESS = new Color(255, 159, 67);
    public static final Color STATUS_RESOLVED    = new Color(80, 200, 120);
    public static final Color STATUS_CLOSED      = new Color(150, 155, 170);

    // ── Fonts ──
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 13);

    // ── Dimensions ──
    public static final int BORDER_RADIUS = 12;
    public static final int PADDING       = 16;
    public static final int PADDING_SM    = 8;
    public static final int PADDING_LG    = 24;

    /**
     * Creates a styled JButton with the given text and accent color.
     */
    public static JButton createStyledButton(String text, Color accentColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(accentColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(accentColor.brighter());
                } else {
                    g2.setColor(accentColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);

                g2.setColor(Color.WHITE);
                g2.setFont(FONT_HEADING);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_HEADING);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled text field with dark theme.
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /**
     * Creates a styled password field with dark theme.
     */
    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /**
     * Creates a styled text area with dark theme.
     */
    public static JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setBackground(BG_INPUT);
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(ACCENT_BLUE);
        area.setFont(FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return area;
    }

    /**
     * Creates a styled combo box.
     */
    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return combo;
    }

    /**
     * Creates a styled label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Applies dark theme to a JTable.
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setFont(FONT_HEADING);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        table.setFillsViewportHeight(true);
    }

    /**
     * Wraps a table in a styled scroll pane.
     */
    public static JScrollPane createStyledScrollPane(JTable table) {
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return sp;
    }

    /**
     * Creates a card-style panel with rounded border appearance.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        return panel;
    }

    /**
     * Gets color for a priority label.
     */
    public static Color getPriorityColor(String priority) {
        if (priority == null) return TEXT_SECONDARY;
        return switch (priority.toUpperCase()) {
            case "HIGH"   -> PRIORITY_HIGH;
            case "MEDIUM" -> PRIORITY_MEDIUM;
            case "LOW"    -> PRIORITY_LOW;
            default       -> TEXT_SECONDARY;
        };
    }

    /**
     * Gets color for a status value.
     */
    public static Color getStatusColor(String status) {
        if (status == null) return TEXT_SECONDARY;
        return switch (status.toUpperCase()) {
            case "OPEN"        -> STATUS_OPEN;
            case "IN_PROGRESS" -> STATUS_IN_PROGRESS;
            case "RESOLVED"    -> STATUS_RESOLVED;
            case "CLOSED"      -> STATUS_CLOSED;
            default            -> TEXT_SECONDARY;
        };
    }
}
