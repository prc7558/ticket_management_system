package ui;

import model.Ticket;
import model.User;
import service.TicketService;
import util.PasswordUtil;
import util.ThemeConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User Portal with 4 panels:
 * 1. Raise Complaint
 * 2. My Tickets
 * 3. Track Status (auto-refresh)
 * 4. Edit Profile
 */
public class UserPortal extends JFrame {

    private final User currentUser;
    private final TicketService service;
    private JTabbedPane tabbedPane;

    // My Tickets tab
    private DefaultTableModel ticketTableModel;
    private JTable ticketTable;

    // Auto-refresh
    private ScheduledExecutorService refreshScheduler;

    public UserPortal(User user, TicketService service) {
        this.currentUser = user;
        this.service = service;

        setTitle("User Portal — " + user.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        initUI();
        startAutoRefresh();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeConstants.BG_DARK);

        // ── Top Bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeConstants.BG_PANEL);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JLabel titleLbl = ThemeConstants.createLabel("👤 User Portal", ThemeConstants.FONT_SUBTITLE, ThemeConstants.TEXT_PRIMARY);
        JLabel userLbl = ThemeConstants.createLabel("Welcome, " + currentUser.getName(), ThemeConstants.FONT_BODY, ThemeConstants.TEXT_SECONDARY);

        JButton logoutBtn = ThemeConstants.createStyledButton("Logout", ThemeConstants.ACCENT_RED);
        logoutBtn.setPreferredSize(new Dimension(100, 34));
        logoutBtn.addActionListener(e -> {
            stopAutoRefresh();
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTop.setOpaque(false);
        leftTop.add(titleLbl);
        leftTop.add(userLbl);

        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // ── Tabbed Pane ──
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(ThemeConstants.BG_DARK);
        tabbedPane.setForeground(ThemeConstants.TEXT_PRIMARY);
        tabbedPane.setFont(ThemeConstants.FONT_HEADING);

        tabbedPane.addTab("📝 Raise Complaint", createRaiseComplaintPanel());
        tabbedPane.addTab("🎫 My Tickets", createMyTicketsPanel());
        tabbedPane.addTab("📊 Track Status", createTrackStatusPanel());
        tabbedPane.addTab("⚙ Edit Profile", createEditProfilePanel());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // ════════════════════════════════════════════
    // TAB 1: Raise Complaint
    // ════════════════════════════════════════════
    private JPanel createRaiseComplaintPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(ThemeConstants.BG_DARK);

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 520));

        JLabel heading = ThemeConstants.createLabel("Raise a New Complaint", ThemeConstants.FONT_SUBTITLE, ThemeConstants.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = ThemeConstants.createLabel("Title", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField titleField = ThemeConstants.createStyledTextField(30);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catLbl = ThemeConstants.createLabel("Category", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        catLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] categories = {"General", "Technical", "Billing", "Service", "Lost Item", "Found Item", "Anonymous Feedback", "Other"};
        JComboBox<String> catBox = ThemeConstants.createStyledComboBox(categories);
        catBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        catBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Anonymous checkbox (inspired by campus-issue CIRS)
        JCheckBox anonBox = new JCheckBox("Submit anonymously (your name won't be visible to admins)");
        anonBox.setOpaque(false);
        anonBox.setForeground(ThemeConstants.TEXT_SECONDARY);
        anonBox.setFont(ThemeConstants.FONT_SMALL);
        anonBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Auto-check anonymous when "Anonymous Feedback" is selected
        catBox.addActionListener(ev -> {
            if ("Anonymous Feedback".equals(catBox.getSelectedItem())) {
                anonBox.setSelected(true);
            }
        });

        JLabel descLbl = ThemeConstants.createLabel("Description", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea descArea = ThemeConstants.createStyledTextArea(6, 30);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        descScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR, 1));

        JButton submitBtn = ThemeConstants.createStyledButton("Submit Complaint", ThemeConstants.ACCENT_GREEN);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel resultLbl = ThemeConstants.createLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.ACCENT_GREEN);
        resultLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String d = descArea.getText().trim();
            String c = (String) catBox.getSelectedItem();
            boolean anon = anonBox.isSelected();
            if (t.isEmpty() || d.isEmpty()) {
                resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                resultLbl.setText("Title and Description are required.");
                return;
            }
            Ticket ticket = service.raiseComplaint(currentUser.getId(), t, d, c, anon);
            if (ticket != null) {
                resultLbl.setForeground(ThemeConstants.ACCENT_GREEN);
                resultLbl.setText("✓ Ticket #" + ticket.getId() + " created" + (anon ? " (anonymous)" : "") + "!");
                titleField.setText("");
                descArea.setText("");
                anonBox.setSelected(false);
                refreshTicketTable();
            } else {
                resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                resultLbl.setText("Failed to create complaint. Try again.");
            }
        });

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLbl); card.add(Box.createVerticalStrut(4)); card.add(titleField);
        card.add(Box.createVerticalStrut(14));
        card.add(catLbl); card.add(Box.createVerticalStrut(4)); card.add(catBox);
        card.add(Box.createVerticalStrut(10));
        card.add(anonBox);
        card.add(Box.createVerticalStrut(14));
        card.add(descLbl); card.add(Box.createVerticalStrut(4)); card.add(descScroll);
        card.add(Box.createVerticalStrut(16));
        card.add(submitBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(resultLbl);

        outer.add(card);
        return outer;
    }

    // ════════════════════════════════════════════
    // TAB 2: My Tickets
    // ════════════════════════════════════════════
    private JPanel createMyTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Title", "Category", "Priority", "Status", "Created", "SLA Deadline"};
        ticketTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ticketTable = new JTable(ticketTableModel);
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Priority color renderer
        ticketTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel && v != null) comp.setForeground(ThemeConstants.getPriorityColor(v.toString()));
                else if (sel) comp.setForeground(Color.WHITE);
                comp.setBackground(sel ? ThemeConstants.ACCENT_BLUE.darker() : ThemeConstants.BG_CARD);
                return comp;
            }
        });
        // Status color renderer
        ticketTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel && v != null) comp.setForeground(ThemeConstants.getStatusColor(v.toString()));
                else if (sel) comp.setForeground(Color.WHITE);
                comp.setBackground(sel ? ThemeConstants.ACCENT_BLUE.darker() : ThemeConstants.BG_CARD);
                return comp;
            }
        });

        JScrollPane sp = ThemeConstants.createStyledScrollPane(ticketTable);

        JButton refreshBtn = ThemeConstants.createStyledButton("Refresh", ThemeConstants.ACCENT_BLUE);
        refreshBtn.addActionListener(e -> refreshTicketTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(ThemeConstants.createLabel("My Tickets", ThemeConstants.FONT_SUBTITLE, ThemeConstants.TEXT_PRIMARY));
        top.add(Box.createHorizontalStrut(20));
        top.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        refreshTicketTable();
        return panel;
    }

    private void refreshTicketTable() {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Ticket> doInBackground() {
                return service.getUserTickets(currentUser.getId());
            }
            @Override
            protected void done() {
                try {
                    List<Ticket> tickets = get();
                    ticketTableModel.setRowCount(0);
                    for (Ticket t : tickets) {
                        ticketTableModel.addRow(new Object[]{
                            t.getId(),
                            t.getComplaintTitle(),
                            t.getComplaintCategory(),
                            t.getPriority(),
                            t.getStatus().name(),
                            t.getCreatedAt() != null ? t.getCreatedAt().toString().substring(0, 16) : "",
                            t.getSlaDeadline() != null ? t.getSlaDeadline().toString().substring(0, 16) : "N/A"
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("Error refreshing tickets: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ════════════════════════════════════════════
    // TAB 3: Track Status
    // ════════════════════════════════════════════
    private JPanel createTrackStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Stats cards at top
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);

        JLabel totalLbl = createStatCard("Total", "0", ThemeConstants.ACCENT_BLUE);
        JLabel openLbl = createStatCard("Open", "0", ThemeConstants.STATUS_OPEN);
        JLabel progressLbl = createStatCard("In Progress", "0", ThemeConstants.STATUS_IN_PROGRESS);
        JLabel resolvedLbl = createStatCard("Resolved", "0", ThemeConstants.STATUS_RESOLVED);

        statsPanel.add(totalLbl.getParent());
        statsPanel.add(openLbl.getParent());
        statsPanel.add(progressLbl.getParent());
        statsPanel.add(resolvedLbl.getParent());

        JLabel refreshInfo = ThemeConstants.createLabel("Auto-refreshes every 30 seconds", ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_MUTED);

        JPanel topP = new JPanel(new BorderLayout());
        topP.setOpaque(false);
        topP.add(statsPanel, BorderLayout.CENTER);
        topP.add(refreshInfo, BorderLayout.SOUTH);

        // Detail area
        JTextArea detailArea = ThemeConstants.createStyledTextArea(15, 40);
        detailArea.setEditable(false);
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR, 1));

        panel.add(topP, BorderLayout.NORTH);
        panel.add(detailScroll, BorderLayout.CENTER);

        // Update function
        Runnable updateStats = () -> {
            List<Ticket> tickets = service.getUserTickets(currentUser.getId());
            int[] counts = {0, 0, 0}; // open, inProg, resolved
            StringBuilder sb = new StringBuilder();
            for (Ticket t : tickets) {
                switch (t.getStatus()) {
                    case OPEN -> counts[0]++;
                    case IN_PROGRESS -> counts[1]++;
                    case RESOLVED, CLOSED -> counts[2]++;
                }
                sb.append("Ticket #").append(t.getId())
                  .append("  |  ").append(t.getComplaintTitle())
                  .append("  |  Priority: ").append(t.getPriority())
                  .append("  |  Status: ").append(t.getStatus())
                  .append("  |  SLA: ").append(t.getSlaDeadline() != null ? t.getSlaDeadline().toString().substring(0, 16) : "N/A")
                  .append(t.isOverdue() ? "  ⚠ OVERDUE" : "")
                  .append("\n");
            }
            final int total = tickets.size();
            final int openCount = counts[0];
            final int inProgCount = counts[1];
            final int resolvedCount = counts[2];
            final String detail = sb.toString();
            SwingUtilities.invokeLater(() -> {
                totalLbl.setText(String.valueOf(total));
                openLbl.setText(String.valueOf(openCount));
                progressLbl.setText(String.valueOf(inProgCount));
                resolvedLbl.setText(String.valueOf(resolvedCount));
                detailArea.setText(detail);
            });
        };

        // Initial load
        SwingUtilities.invokeLater(() -> new Thread(updateStats).start());

        // Store updater for auto-refresh
        panel.putClientProperty("updater", updateStats);

        return panel;
    }

    private JLabel createStatCard(String title, String value, Color color) {
        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel valueLbl = ThemeConstants.createLabel(value, ThemeConstants.FONT_TITLE, color);
        valueLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = ThemeConstants.createLabel(title, ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLbl);
        card.add(Box.createVerticalGlue());

        return valueLbl;
    }

    // ════════════════════════════════════════════
    // TAB 4: Edit Profile
    // ════════════════════════════════════════════
    private JPanel createEditProfilePanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(ThemeConstants.BG_DARK);

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 360));

        JLabel heading = ThemeConstants.createLabel("Edit Profile", ThemeConstants.FONT_SUBTITLE, ThemeConstants.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = ThemeConstants.createLabel("Name", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField nameField = ThemeConstants.createStyledTextField(25);
        nameField.setText(currentUser.getName());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pwLbl = ThemeConstants.createLabel("New Password (leave blank to keep)", ThemeConstants.FONT_HEADING, ThemeConstants.TEXT_SECONDARY);
        pwLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField pwField = ThemeConstants.createStyledPasswordField(25);
        pwField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pwField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = ThemeConstants.createStyledButton("Save Changes", ThemeConstants.ACCENT_BLUE);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel resultLbl = ThemeConstants.createLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.ACCENT_GREEN);
        resultLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pw = new String(pwField.getPassword());
            if (name.isEmpty()) {
                resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                resultLbl.setText("Name cannot be empty.");
                return;
            }
            currentUser.setName(name);
            if (!pw.isEmpty()) {
                currentUser.setPasswordHash(PasswordUtil.hashPassword(pw));
            }
            if (service.updateProfile(currentUser)) {
                resultLbl.setForeground(ThemeConstants.ACCENT_GREEN);
                resultLbl.setText("✓ Profile updated successfully!");
                setTitle("User Portal — " + currentUser.getName());
            } else {
                resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                resultLbl.setText("Update failed. Try again.");
            }
        });

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(nameLbl); card.add(Box.createVerticalStrut(4)); card.add(nameField);
        card.add(Box.createVerticalStrut(14));
        card.add(pwLbl); card.add(Box.createVerticalStrut(4)); card.add(pwField);
        card.add(Box.createVerticalStrut(20));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(resultLbl);

        outer.add(card);
        return outer;
    }

    // ── Auto-Refresh ──
    private void startAutoRefresh() {
        refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "UI-Refresh");
            t.setDaemon(true);
            return t;
        });
        refreshScheduler.scheduleAtFixedRate(() -> {
            if (isVisible()) {
                refreshTicketTable();
                // Refresh track status tab
                Component tab = tabbedPane.getComponentAt(2);
                if (tab instanceof JPanel p) {
                    Object updater = p.getClientProperty("updater");
                    if (updater instanceof Runnable r) r.run();
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh() {
        if (refreshScheduler != null) {
            refreshScheduler.shutdown();
        }
    }
}
