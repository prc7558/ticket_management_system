package util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

/**
 * Centralized UI theming constants for a modern, dark-themed Swing application.
 */
public class ThemeConstants {

    private ThemeConstants() {
    }

    // ── Color Palette ──
    public static final Color BG_DARK = new Color(12, 12, 18); // near-black app bg
    public static final Color BG_PANEL = new Color(22, 23, 34); // top-bar / sidebar
    public static final Color BG_CARD = new Color(30, 32, 46); // card surfaces
    public static final Color BG_INPUT = new Color(40, 42, 58); // input fields
    public static final Color BG_HOVER = new Color(50, 52, 70); // hover state

    public static final Color ACCENT_BLUE = new Color(99, 134, 255);
    public static final Color ACCENT_PURPLE = new Color(138, 99, 255);
    public static final Color ACCENT_GREEN = new Color(72, 199, 116);
    public static final Color ACCENT_ORANGE = new Color(255, 159, 67);
    public static final Color ACCENT_RED    = new Color(255, 82, 82);
    public static final Color ACCENT_TEAL   = new Color(6, 182, 212);  // cyan
    public static final Color ACCENT_PINK   = new Color(236, 72, 153); // pink

    public static final Color TEXT_PRIMARY = new Color(228, 232, 240);
    public static final Color TEXT_SECONDARY = new Color(148, 153, 168);
    public static final Color TEXT_MUTED = new Color(90, 95, 110);

    public static final Color BORDER_COLOR = new Color(50, 52, 70);

    // ── Priority Colors ──
    public static final Color PRIORITY_HIGH = new Color(255, 82, 82);
    public static final Color PRIORITY_MEDIUM = new Color(255, 159, 67);
    public static final Color PRIORITY_LOW = new Color(72, 199, 116);

    // ── Status Colors ──
    public static final Color STATUS_OPEN = new Color(99, 134, 255);
    public static final Color STATUS_IN_PROGRESS = new Color(255, 159, 67);
    public static final Color STATUS_RESOLVED = new Color(72, 199, 116);
    public static final Color STATUS_CLOSED = new Color(148, 153, 168);

    // ── Fonts ──
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_DISPLAY = new Font("Segoe UI", Font.BOLD, 34); // hero
    public static final Font FONT_LARGE   = new Font("Segoe UI", Font.BOLD, 20); // section
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);

    // ── Dimensions ──
    public static final int BORDER_RADIUS = 12;
    public static final int PADDING = 16;
    public static final int PADDING_SM = 8;
    public static final int PADDING_LG = 24;

    // ─────────────────────────────────────────────────────────────────────────
    // Global UIManager defaults — call ONCE from the main() entry point AFTER
    // setting the Look-and-Feel so every unstyled Swing widget inherits the
    // dark theme automatically (tabs, option-panes, scroll-bars, lists, etc.).
    // ─────────────────────────────────────────────────────────────────────────
    public static void applyGlobalUIDefaults() {
        // Panels / frames
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Dialog.background", BG_PANEL);

        // Buttons inside option-panes
        UIManager.put("Button.background", BG_CARD);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.select", BG_HOVER);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        // Labels
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.background", BG_DARK);

        // Text fields
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT_BLUE);
        UIManager.put("TextField.selectionBackground", ACCENT_BLUE);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        UIManager.put("PasswordField.background", BG_INPUT);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT_BLUE);
        UIManager.put("PasswordField.selectionBackground", ACCENT_BLUE);
        UIManager.put("PasswordField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        // Text area
        UIManager.put("TextArea.background", BG_INPUT);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", ACCENT_BLUE);

        // ComboBox
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", ACCENT_BLUE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground", BG_INPUT);
        UIManager.put("ComboBox.disabledBackground", BG_CARD);
        UIManager.put("ComboBox.disabledForeground", TEXT_MUTED);

        // ComboBox popup list
        UIManager.put("List.background", BG_INPUT);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", ACCENT_BLUE);
        UIManager.put("List.selectionForeground", Color.WHITE);

        // CheckBox
        UIManager.put("CheckBox.background", BG_DARK);
        UIManager.put("CheckBox.foreground", TEXT_SECONDARY);
        UIManager.put("CheckBox.focus", new Color(0, 0, 0, 0));

        // Scroll bars — dark track, card-colored thumb
        UIManager.put("ScrollBar.background", BG_DARK);
        UIManager.put("ScrollBar.thumb", BG_CARD);
        UIManager.put("ScrollBar.thumbHighlight", BG_HOVER);
        UIManager.put("ScrollBar.thumbShadow", BG_DARK);
        UIManager.put("ScrollBar.track", BG_DARK);
        UIManager.put("ScrollBar.trackHighlight", BG_DARK);
        UIManager.put("ScrollBar.width", 10);

        // Scroll pane
        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("ScrollPane.border",
                BorderFactory.createLineBorder(BORDER_COLOR, 1));
        UIManager.put("Viewport.background", BG_DARK);

        // Tabbed pane
        UIManager.put("TabbedPane.background", BG_DARK);
        UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
        UIManager.put("TabbedPane.selected", BG_PANEL);
        UIManager.put("TabbedPane.selectedForeground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.contentAreaColor", BG_DARK);
        UIManager.put("TabbedPane.light", BORDER_COLOR);
        UIManager.put("TabbedPane.highlight", BG_PANEL);
        UIManager.put("TabbedPane.shadow", BG_DARK);
        UIManager.put("TabbedPane.darkShadow", BG_DARK);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOpaque", false);
        UIManager.put("TabbedPane.borderHightlightColor", BORDER_COLOR);
        UIManager.put("TabbedPane.selectHighlight", ACCENT_BLUE);

        // Separators
        UIManager.put("Separator.background", BORDER_COLOR);
        UIManager.put("Separator.foreground", BORDER_COLOR);

        // Table (header is styled manually; these cover defaults)
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("Table.selectionBackground", new Color(60, 80, 160));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", BG_PANEL);
        UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Widget factory helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a styled JButton with the given text and accent color.
     * The button custom-paints itself (rounded rect) so it is unaffected by L&F.
     */
    public static JButton createStyledButton(String text, Color accentColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg;
                if (getModel().isPressed()) {
                    bg = accentColor.darker().darker();
                } else if (getModel().isRollover()) {
                    // lighter hover
                    bg = new Color(
                            Math.min(accentColor.getRed() + 30, 255),
                            Math.min(accentColor.getGreen() + 30, 255),
                            Math.min(accentColor.getBlue() + 30, 255));
                } else {
                    bg = accentColor;
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);

                // subtle top highlight for depth
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() / 2, BORDER_RADIUS, BORDER_RADIUS);

                g2.setColor(Color.WHITE);
                g2.setFont(FONT_HEADING);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                /* no border */ }
        };
        btn.setFont(FONT_HEADING);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
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
        field.setSelectionColor(ACCENT_BLUE);
        field.setSelectedTextColor(Color.WHITE);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
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
        field.setSelectionColor(ACCENT_BLUE);
        field.setSelectedTextColor(Color.WHITE);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
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
        area.setSelectionColor(ACCENT_BLUE);
        area.setSelectedTextColor(Color.WHITE);
        area.setFont(FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return area;
    }

    /**
     * Creates a styled combo box with a custom dark-theme renderer for the dropdown
     * popup.
     */
    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        combo.setRenderer(new BasicComboBoxRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(ACCENT_BLUE);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(BG_INPUT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                setFont(FONT_BODY);
                return this;
            }
        });
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
        table.setSelectionBackground(new Color(60, 80, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setFont(FONT_HEADING);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        table.setFillsViewportHeight(true);
        // Make alternating row colors (via custom row renderer is complex;
        // use a plain background matching the card for simplicity)
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
     * Creates a card-style panel with a dark rounded background.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Do NOT call super — we fill ourselves to avoid L&F background bleed
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
     * Creates a styled JScrollPane that wraps any component with a dark background.
     */
    public static JScrollPane createStyledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_INPUT);
        sp.getViewport().setBackground(BG_INPUT);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return sp;
    }

    /**
     * Gets color for a priority label.
     */
    public static Color getPriorityColor(String priority) {
        if (priority == null)
            return TEXT_SECONDARY;
        return switch (priority.toUpperCase()) {
            case "HIGH" -> PRIORITY_HIGH;
            case "MEDIUM" -> PRIORITY_MEDIUM;
            case "LOW" -> PRIORITY_LOW;
            default -> TEXT_SECONDARY;
        };
    }

    /**
     * Gets color for a status value.
     */
    public static Color getStatusColor(String status) {
        if (status == null)
            return TEXT_SECONDARY;
        return switch (status.toUpperCase()) {
            case "OPEN" -> STATUS_OPEN;
            case "IN_PROGRESS" -> STATUS_IN_PROGRESS;
            case "RESOLVED" -> STATUS_RESOLVED;
            case "CLOSED" -> STATUS_CLOSED;
            default -> TEXT_SECONDARY;
        };
    }
}
