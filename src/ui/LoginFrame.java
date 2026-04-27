package ui;

import model.User;
import service.TicketService;
import util.ThemeConstants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/**
 * Full-screen split-panel login:  [Brand/gradient | Form].
 * Fixes BoxLayout alignment by keeping all form elements LEFT_ALIGNMENT
 * and wrapping any centered header in its own FlowLayout.LEFT sub-panel.
 */
public class LoginFrame extends JFrame {

    private final TicketService service = new TicketService();
    private JTextField    emailField;
    private JPasswordField passwordField;
    private JLabel         statusLabel;

    public LoginFrame() {
        setTitle("Ticket Manager — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 580));
        initUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(ThemeConstants.BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.44;
        root.add(buildBrandPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.56;
        root.add(buildFormPanel(), gbc);

        setContentPane(root);
    }

    // ── LEFT: brand / gradient ────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        final Color c1 = new Color(37, 32, 120);   // deep indigo
        final Color c2 = new Color(101, 35, 200);  // violet

        JPanel outer = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // decorative circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-60, -60, 280, 280);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() - 180, getHeight() - 180, 340, 340);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(getWidth() / 3, getHeight() / 3, 160, 160);
                g2.dispose();
            }
        };
        outer.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(60, 52, 60, 52));

        content.add(Box.createVerticalGlue());

        // Logo
        addCentered(content, makeLabel("🎫", new Font("Segoe UI Emoji", Font.PLAIN, 64), Color.WHITE));
        content.add(Box.createVerticalStrut(20));

        // App name
        addCentered(content, makeLabel("TicketManager",
                new Font("Segoe UI", Font.BOLD, 36), Color.WHITE));
        content.add(Box.createVerticalStrut(10));

        // Tagline
        addCentered(content, makeLabel("Smart. Efficient. Transparent.",
                new Font("Segoe UI", Font.PLAIN, 15), new Color(200, 200, 255)));
        content.add(Box.createVerticalStrut(44));

        // Feature bullets
        String[] features = {
            "🎯  Intelligent ticket routing & priority",
            "📊  Real-time dashboards & SLA tracking",
            "🔔  Smart keyword matching on complaints",
            "🔒  Role-based access for users & admins"
        };
        for (String f : features) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lbl = makeLabel(f, new Font("Segoe UI", Font.PLAIN, 14),
                    new Color(210, 210, 240));
            row.add(lbl);
            content.add(row);
        }

        content.add(Box.createVerticalGlue());

        // Footer
        content.add(Box.createVerticalStrut(20));
        addCentered(content, makeLabel("© 2025 Complaint & Ticket Management System",
                new Font("Segoe UI", Font.PLAIN, 11), new Color(170, 160, 220)));

        outer.add(content);
        return outer;
    }

    // ── RIGHT: login form ──────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(ThemeConstants.BG_DARK);

        // Form card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConstants.BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeConstants.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(46, 48, 46, 48));
        card.setPreferredSize(new Dimension(460, 580));
        card.setMaximumSize(new Dimension(460, 700));

        // ── All content uses LEFT_ALIGNMENT so BoxLayout doesn't shift anything ──

        // Header rows (centered text inside a FlowLayout.CENTER row that is itself LEFT_ALIGNMENT)
        addCenteredRow(card, makeLabel("👋  Welcome back",
                new Font("Segoe UI", Font.BOLD, 28), ThemeConstants.TEXT_PRIMARY));
        card.add(Box.createVerticalStrut(6));
        addCenteredRow(card, makeLabel("Sign in to your account",
                ThemeConstants.FONT_BODY, ThemeConstants.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(30));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(28));

        // Email
        JLabel emailLbl = field_label("Email address");
        emailField = ThemeConstants.createStyledTextField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.addActionListener(e -> doLogin());

        // Password
        JLabel passLbl = field_label("Password");
        passwordField = ThemeConstants.createStyledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.addActionListener(e -> doLogin());

        // Status
        statusLabel = makeLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.ACCENT_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sign-in button
        JButton loginBtn = ThemeConstants.createStyledButton("Sign In →", ThemeConstants.ACCENT_BLUE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.addActionListener(e -> doLogin());

        card.add(emailLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(18));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(6));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(24));

        // Register row
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        regRow.setOpaque(false);
        regRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        regRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel regText = makeLabel("Don't have an account?",
                ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_MUTED);
        JLabel regLink = makeLabel("Register here",
                ThemeConstants.FONT_HEADING, ThemeConstants.ACCENT_BLUE);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showRegisterDialog(); }
            @Override public void mouseEntered(MouseEvent e) { regLink.setForeground(new Color(140, 170, 255)); }
            @Override public void mouseExited(MouseEvent e)  { regLink.setForeground(ThemeConstants.ACCENT_BLUE); }
        });
        regRow.add(regText);
        regRow.add(regLink);
        card.add(regRow);
        card.add(Box.createVerticalStrut(12));

        // Demo credentials
        JPanel demoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        demoRow.setOpaque(false);
        demoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        demoRow.add(makeLabel(
            "admin@system.com / admin123   |   user@system.com / password123",
            ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_MUTED));
        card.add(demoRow);

        outer.add(card);
        return outer;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void doLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(ThemeConstants.ACCENT_RED);
            statusLabel.setText("Please enter both email and password.");
            return;
        }
        statusLabel.setForeground(ThemeConstants.TEXT_SECONDARY);
        statusLabel.setText("Authenticating…");

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() {
                return service.authenticate(email, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        dispose();
                        if (user.getRole() == User.Role.ADMIN)
                            new AdminDashboard(user, service).setVisible(true);
                        else
                            new UserPortal(user, service).setVisible(true);
                    } else {
                        statusLabel.setForeground(ThemeConstants.ACCENT_RED);
                        statusLabel.setText("Invalid email or password.");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ThemeConstants.ACCENT_RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Registration dialog ────────────────────────────────────────────────────
    private void showRegisterDialog() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int dw = 540, dh = Math.max(640, (int)(screen.height * 0.72));

        JDialog dlg = new JDialog(this, "Create New Account", true);
        dlg.setSize(dw, dh);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // ── Scrollable dark content ──
        JPanel content = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeConstants.BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(36, 44, 36, 44));
        content.setOpaque(false);

        addCenteredRow(content, makeLabel("✍  Create an Account",
                ThemeConstants.FONT_SUBTITLE, ThemeConstants.TEXT_PRIMARY));
        content.add(Box.createVerticalStrut(4));
        addCenteredRow(content, makeLabel("Fill in the details below to get started",
                ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_MUTED));
        content.add(Box.createVerticalStrut(24));

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sep);
        content.add(Box.createVerticalStrut(26));

        JTextField     nameF  = ThemeConstants.createStyledTextField(25);
        JTextField     emailF = ThemeConstants.createStyledTextField(25);
        JPasswordField passF  = ThemeConstants.createStyledPasswordField(25);
        String[]       roles  = {"USER", "ADMIN"};
        JComboBox<String> roleBox = ThemeConstants.createStyledComboBox(roles);

        for (JComponent f : new JComponent[]{nameF, emailF, passF, roleBox}) {
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            f.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        content.add(field_label("Full Name"));  content.add(Box.createVerticalStrut(6)); content.add(nameF);
        content.add(Box.createVerticalStrut(16));
        content.add(field_label("Email Address")); content.add(Box.createVerticalStrut(6)); content.add(emailF);
        content.add(Box.createVerticalStrut(16));
        content.add(field_label("Password"));  content.add(Box.createVerticalStrut(6)); content.add(passF);
        content.add(Box.createVerticalStrut(16));
        content.add(field_label("Account Role")); content.add(Box.createVerticalStrut(6)); content.add(roleBox);
        content.add(Box.createVerticalStrut(10));

        JLabel statusLbl = makeLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.ACCENT_RED);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLbl);
        content.add(Box.createVerticalStrut(22));

        // Buttons
        JButton cancelBtn = ThemeConstants.createStyledButton("Cancel",   new Color(55, 58, 80));
        JButton regBtn    = ThemeConstants.createStyledButton("Register", ThemeConstants.ACCENT_BLUE);
        cancelBtn.setPreferredSize(new Dimension(130, 48));
        regBtn.setPreferredSize(new Dimension(170, 48));
        cancelBtn.addActionListener(e -> dlg.dispose());

        regBtn.addActionListener(e -> {
            String n  = nameF.getText().trim();
            String em = emailF.getText().trim();
            String pw = new String(passF.getPassword());
            if (n.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                statusLbl.setForeground(ThemeConstants.ACCENT_RED);
                statusLbl.setText("All fields are required.");
                return;
            }
            User.Role r = User.Role.valueOf((String) roleBox.getSelectedItem());
            User u = service.registerUser(n, em, pw, r);
            if (u != null) {
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now log in.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusLbl.setForeground(ThemeConstants.ACCENT_RED);
                statusLbl.setText("Registration failed — email may already exist.");
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btnRow.add(cancelBtn);
        btnRow.add(regBtn);
        content.add(btnRow);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBackground(ThemeConstants.BG_PANEL);
        scroll.getViewport().setBackground(ThemeConstants.BG_PANEL);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dlg.setContentPane(scroll);
        dlg.setBackground(ThemeConstants.BG_PANEL);
        dlg.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Centered row: FlowLayout.CENTER panel that is itself LEFT_ALIGNMENT (safe in BoxLayout). */
    private static void addCenteredRow(JPanel parent, JLabel label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height + 4));
        row.add(label);
        parent.add(row);
    }

    /** Adds label centered horizontally inside a BoxLayout Y parent. */
    private static void addCentered(JPanel parent, JLabel label) {
        addCenteredRow(parent, label);
    }

    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** Styled field label (bold, secondary color, LEFT_ALIGNMENT). */
    private static JLabel field_label(String text) {
        JLabel l = ThemeConstants.createLabel(text, ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            ThemeConstants.applyGlobalUIDefaults();
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
