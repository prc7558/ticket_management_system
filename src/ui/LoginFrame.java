package ui;

import model.User;
import service.TicketService;
import util.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Login window with modern dark theme.
 * Routes to AdminDashboard or UserPortal based on role.
 */
public class LoginFrame extends JFrame {

    private final TicketService service = new TicketService();
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Complaint & Ticket Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ThemeConstants.BG_DARK, 0, getHeight(),
                        new Color(24, 24, 36));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🎫");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = ThemeConstants.createLabel("Ticket Manager", ThemeConstants.FONT_TITLE, ThemeConstants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = ThemeConstants.createLabel("Sign in to continue", ThemeConstants.FONT_BODY, ThemeConstants.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(icon);
        headerPanel.add(Box.createVerticalStrut(12));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitleLabel);

        // Form
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel emailLabel = ThemeConstants.createLabel("Email", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField = ThemeConstants.createStyledTextField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = ThemeConstants.createLabel("Password", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = ThemeConstants.createStyledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = ThemeConstants.createStyledButton("Sign In", ThemeConstants.ACCENT_BLUE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        // Enter key triggers login
        passwordField.addActionListener(e -> doLogin());

        statusLabel = ThemeConstants.createLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.ACCENT_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(Box.createVerticalStrut(40));
        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(passLabel);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(24));
        formPanel.add(loginBtn);

        // Register link
        JPanel regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        regPanel.setOpaque(false);
        JLabel regLabel = ThemeConstants.createLabel("Don't have an account? ", ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_MUTED);
        JLabel regLink = ThemeConstants.createLabel("Register", ThemeConstants.FONT_SMALL, ThemeConstants.ACCENT_BLUE);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { showRegisterDialog(); }
            @Override
            public void mouseEntered(MouseEvent e) { regLink.setForeground(ThemeConstants.ACCENT_BLUE.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { regLink.setForeground(ThemeConstants.ACCENT_BLUE); }
        });
        regPanel.add(regLabel);
        regPanel.add(regLink);

        // Demo credentials hint
        JLabel demoLabel = ThemeConstants.createLabel(
                "Demo: admin@system.com / admin123  |  user@system.com / password123",
                ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_MUTED);
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        demoPanel.setOpaque(false);
        demoPanel.add(demoLabel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(Box.createVerticalStrut(16));
        bottomPanel.add(regPanel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(demoPanel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        statusLabel.setForeground(ThemeConstants.TEXT_SECONDARY);
        statusLabel.setText("Authenticating...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return service.authenticate(email, password);
            }
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        dispose();
                        if (user.getRole() == User.Role.ADMIN) {
                            new AdminDashboard(user, service).setVisible(true);
                        } else {
                            new UserPortal(user, service).setVisible(true);
                        }
                    } else {
                        statusLabel.setForeground(ThemeConstants.ACCENT_RED);
                        statusLabel.setText("Invalid email or password.");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ThemeConstants.ACCENT_RED);
                    statusLabel.setText("Connection error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showRegisterDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBackground(ThemeConstants.BG_PANEL);
        JTextField nameF = new JTextField();
        JTextField emailF = new JTextField();
        JPasswordField passF = new JPasswordField();
        String[] roles = {"USER", "ADMIN"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        panel.add(new JLabel("Name:")); panel.add(nameF);
        panel.add(new JLabel("Email:")); panel.add(emailF);
        panel.add(new JLabel("Password:")); panel.add(passF);
        panel.add(new JLabel("Role:")); panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Register New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String n = nameF.getText().trim();
            String em = emailF.getText().trim();
            String pw = new String(passF.getPassword());
            User.Role r = User.Role.valueOf((String) roleBox.getSelectedItem());

            if (n.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User u = service.registerUser(n, em, pw, r);
            if (u != null) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Email may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Main Entry Point ──
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
