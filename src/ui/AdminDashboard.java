package ui;

import model.Ticket;
import model.User;
import service.PriorityEngine;
import service.SmartMatcherService;
import service.TicketService;
import util.ThemeConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * Admin Dashboard with 5 tabs:
 * 1. All Tickets 2. Priority Assign 3. Status Updater
 * 4. Smart Matcher 5. Reports
 */
public class AdminDashboard extends JFrame {

    private final User currentAdmin;
    private final TicketService service;
    private final PriorityEngine engine;
    private JTabbedPane tabbedPane;

    // All Tickets tab
    private DefaultTableModel allTicketsModel;
    private JTable allTicketsTable;

    public AdminDashboard(User admin, TicketService service) {
        this.currentAdmin = admin;
        this.service = service;
        this.engine = new PriorityEngine(service);

        setTitle("Admin Dashboard — " + admin.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 650));

        initUI();
        engine.start();
        service.startLifecycleManager();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeConstants.BG_DARK);

        // ── Top Bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeConstants.BG_PANEL);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JLabel titleLbl = ThemeConstants.createLabel("🛡  Admin Dashboard", ThemeConstants.FONT_SUBTITLE,
                ThemeConstants.TEXT_PRIMARY);
        JLabel adminLbl = ThemeConstants.createLabel("  |  " + currentAdmin.getName(), ThemeConstants.FONT_BODY,
                ThemeConstants.TEXT_SECONDARY);

        JButton logoutBtn = ThemeConstants.createStyledButton("Logout", ThemeConstants.ACCENT_RED);
        logoutBtn.setPreferredSize(new Dimension(110, 36));
        logoutBtn.addActionListener(e -> {
            engine.stop();
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftTop.setOpaque(false);
        leftTop.add(titleLbl);
        leftTop.add(adminLbl);

        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // ── Tabbed Pane ──
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(ThemeConstants.BG_DARK);
        tabbedPane.setForeground(ThemeConstants.TEXT_SECONDARY);
        tabbedPane.setFont(ThemeConstants.FONT_HEADING);
        tabbedPane.setOpaque(true);

        tabbedPane.addTab("🎫  All Tickets", createAllTicketsPanel());
        tabbedPane.addTab("⚡  Priority", createPriorityPanel());
        tabbedPane.addTab("🔄  Status", createStatusPanel());
        tabbedPane.addTab("✨  Smart Matcher", createSmartMatcherPanel());
        tabbedPane.addTab("📊  Reports", createReportsPanel());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // ════════════════════════════════════════════════════════════════════
    // TAB 1 — All Tickets
    // ════════════════════════════════════════════════════════════════════
    private JPanel createAllTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(ThemeConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        String[] cols = { "ID", "Title", "User", "Category", "Priority", "Status", "Assigned To", "Created", "SLA" };
        allTicketsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        allTicketsTable = new JTable(allTicketsModel);
        allTicketsTable.setAutoCreateRowSorter(true);
        allTicketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Priority column renderer
        allTicketsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(sel ? new Color(60, 80, 160) : ThemeConstants.BG_CARD);
                comp.setForeground(sel ? Color.WHITE
                        : (v != null ? ThemeConstants.getPriorityColor(v.toString()) : ThemeConstants.TEXT_SECONDARY));
                setFont(ThemeConstants.FONT_HEADING);
                return comp;
            }
        });
        // Status column renderer
        allTicketsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(sel ? new Color(60, 80, 160) : ThemeConstants.BG_CARD);
                comp.setForeground(sel ? Color.WHITE
                        : (v != null ? ThemeConstants.getStatusColor(v.toString()) : ThemeConstants.TEXT_SECONDARY));
                return comp;
            }
        });

        JScrollPane sp = ThemeConstants.createStyledScrollPane(allTicketsTable);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        JLabel heading = ThemeConstants.createLabel("All Tickets", ThemeConstants.FONT_SUBTITLE,
                ThemeConstants.TEXT_PRIMARY);
        JTextField searchField = ThemeConstants.createStyledTextField(25);
        searchField.setPreferredSize(new Dimension(300, 38));
        JButton searchBtn = ThemeConstants.createStyledButton("Search", ThemeConstants.ACCENT_BLUE);
        JButton refreshBtn = ThemeConstants.createStyledButton("Refresh", ThemeConstants.ACCENT_PURPLE);
        searchBtn.setPreferredSize(new Dimension(110, 38));
        refreshBtn.setPreferredSize(new Dimension(110, 38));

        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty())
                refreshAllTickets();
            else
                searchTickets(q);
        });
        searchField.addActionListener(e -> searchBtn.doClick());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshAllTickets();
        });

        searchBar.add(heading);
        searchBar.add(Box.createHorizontalStrut(20));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(refreshBtn);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        refreshAllTickets();
        return panel;
    }

    private void refreshAllTickets() {
        loadTickets(service.getAllTickets());
    }

    private void searchTickets(String query) {
        loadTickets(service.searchTickets(query));
    }

    private void loadTickets(List<Ticket> tickets) {
        SwingUtilities.invokeLater(() -> {
            allTicketsModel.setRowCount(0);
            for (Ticket t : tickets) {
                allTicketsModel.addRow(new Object[] {
                        t.getId(),
                        t.getComplaintTitle(),
                        t.getUserName(),
                        t.getComplaintCategory(),
                        t.getPriority(),
                        t.getStatus().name(),
                        t.getAssignedAdminName() != null ? t.getAssignedAdminName() : "Unassigned",
                        t.getCreatedAt() != null ? t.getCreatedAt().toString().substring(0, 16) : "",
                        t.getSlaDeadline() != null ? t.getSlaDeadline().toString().substring(0, 16) : "N/A"
                });
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════
    // TAB 2 — Priority Assign
    // ════════════════════════════════════════════════════════════════════
    private JPanel createPriorityPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(ThemeConstants.BG_DARK);

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 340));

        JLabel heading = ThemeConstants.createLabel("Assign Priority", ThemeConstants.FONT_SUBTITLE,
                ThemeConstants.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tidLbl = ThemeConstants.createLabel("Ticket ID", ThemeConstants.FONT_HEADING,
                ThemeConstants.TEXT_SECONDARY);
        tidLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField tidField = ThemeConstants.createStyledTextField(10);
        tidField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tidField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priLbl = ThemeConstants.createLabel("Priority", ThemeConstants.FONT_HEADING,
                ThemeConstants.TEXT_SECONDARY);
        priLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] priorities = { "LOW", "MEDIUM", "HIGH" };
        JComboBox<String> priBox = ThemeConstants.createStyledComboBox(priorities);
        priBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        priBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton assignBtn = ThemeConstants.createStyledButton("Assign Priority", ThemeConstants.ACCENT_ORANGE);
        assignBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        assignBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel resultLbl = ThemeConstants.createLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_PRIMARY);
        resultLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        assignBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tidField.getText().trim());
                String pri = (String) priBox.getSelectedItem();
                if (service.updatePriority(id, pri, currentAdmin.getId())) {
                    resultLbl.setForeground(ThemeConstants.ACCENT_GREEN);
                    resultLbl.setText("✓ Ticket #" + id + " priority set to " + pri);
                    refreshAllTickets();
                } else {
                    resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                    resultLbl.setText("Failed. Check ticket ID.");
                }
            } catch (NumberFormatException ex) {
                resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                resultLbl.setText("Invalid ticket ID.");
            }
        });

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(tidLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(tidField);
        card.add(Box.createVerticalStrut(16));
        card.add(priLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(priBox);
        card.add(Box.createVerticalStrut(22));
        card.add(assignBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(resultLbl);

        outer.add(card);
        return outer;
    }

    // ════════════════════════════════════════════════════════════════════
    // TAB 3 — Status Updater
    // ════════════════════════════════════════════════════════════════════
    private JPanel createStatusPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(ThemeConstants.BG_DARK);

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 410));

        JLabel heading = ThemeConstants.createLabel("Update Ticket Status", ThemeConstants.FONT_SUBTITLE,
                ThemeConstants.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tidLbl = ThemeConstants.createLabel("Ticket ID", ThemeConstants.FONT_HEADING,
                ThemeConstants.TEXT_SECONDARY);
        tidLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField tidField = ThemeConstants.createStyledTextField(10);
        tidField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tidField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel curLbl = ThemeConstants.createLabel("Current Status: —", ThemeConstants.FONT_BODY,
                ThemeConstants.TEXT_SECONDARY);
        curLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loadBtn = ThemeConstants.createStyledButton("Load Ticket", ThemeConstants.ACCENT_BLUE);
        loadBtn.setPreferredSize(new Dimension(140, 38));
        loadBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel newLbl = ThemeConstants.createLabel("New Status", ThemeConstants.FONT_HEADING,
                ThemeConstants.TEXT_SECONDARY);
        newLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] statuses = { "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED" };
        JComboBox<String> statusBox = ThemeConstants.createStyledComboBox(statuses);
        statusBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        statusBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        loadBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tidField.getText().trim());
                Ticket t = service.getTicket(id);
                if (t != null) {
                    curLbl.setText("Status: " + t.getStatus().name() + "  |  Priority: " + t.getPriority());
                    curLbl.setForeground(ThemeConstants.getStatusColor(t.getStatus().name()));
                } else {
                    curLbl.setText("Ticket not found.");
                    curLbl.setForeground(ThemeConstants.ACCENT_RED);
                }
            } catch (NumberFormatException ex) {
                curLbl.setText("Invalid ID.");
                curLbl.setForeground(ThemeConstants.ACCENT_RED);
            }
        });

        JButton updateBtn = ThemeConstants.createStyledButton("Update Status", ThemeConstants.ACCENT_GREEN);
        updateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel resultLbl = ThemeConstants.createLabel(" ", ThemeConstants.FONT_SMALL, ThemeConstants.TEXT_PRIMARY);
        resultLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        updateBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tidField.getText().trim());
                Ticket.Status ns = Ticket.Status.valueOf((String) statusBox.getSelectedItem());
                if (service.updateStatus(id, ns, currentAdmin.getId())) {
                    resultLbl.setForeground(ThemeConstants.ACCENT_GREEN);
                    resultLbl.setText("✓ Ticket #" + id + " → " + ns.name());
                    curLbl.setText("Status: " + ns.name());
                    curLbl.setForeground(ThemeConstants.getStatusColor(ns.name()));
                    refreshAllTickets();
                } else {
                    resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                    resultLbl.setText("Update failed.");
                }
            } catch (Exception ex) {
                resultLbl.setForeground(ThemeConstants.ACCENT_RED);
                resultLbl.setText("Error: " + ex.getMessage());
            }
        });

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(tidLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(tidField);
        card.add(Box.createVerticalStrut(10));
        card.add(loadBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(curLbl);
        card.add(Box.createVerticalStrut(16));
        card.add(newLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(statusBox);
        card.add(Box.createVerticalStrut(22));
        card.add(updateBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(resultLbl);

        outer.add(card);
        return outer;
    }

    // ════════════════════════════════════════════════════════════════════
    // TAB 4 — Smart Matcher
    // ════════════════════════════════════════════════════════════════════
    private JPanel createSmartMatcherPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(ThemeConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topBar.setOpaque(false);
        topBar.add(ThemeConstants.createLabel("Smart Matcher — Related Tickets",
                ThemeConstants.FONT_SUBTITLE, ThemeConstants.TEXT_PRIMARY));
        JButton scanBtn = ThemeConstants.createStyledButton("Scan All", ThemeConstants.ACCENT_PURPLE);
        scanBtn.setPreferredSize(new Dimension(130, 38));
        topBar.add(scanBtn);

        // Output area
        JTextArea matchArea = ThemeConstants.createStyledTextArea(20, 60);
        matchArea.setEditable(false);
        matchArea.setFont(ThemeConstants.FONT_MONO);
        matchArea.setBackground(ThemeConstants.BG_CARD);

        JScrollPane matchScroll = new JScrollPane(matchArea);
        matchScroll.setBackground(ThemeConstants.BG_CARD);
        matchScroll.getViewport().setBackground(ThemeConstants.BG_CARD);
        matchScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR, 1));

        scanBtn.addActionListener(e -> {
            matchArea.setText("Scanning tickets for related complaints...\n\n");
            new Thread(() -> {
                List<SmartMatcherService.TicketMatch> matches = service.findRelatedTickets();
                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════════════════\n");
                sb.append("  SMART MATCHER — Keyword Analysis Results\n");
                sb.append("═══════════════════════════════════════════════\n\n");

                if (matches.isEmpty()) {
                    sb.append("  No related tickets found amongst active tickets.\n");
                    sb.append("  All current tickets appear to be unique complaints.\n");
                } else {
                    sb.append("  Found ").append(matches.size()).append(" potential relationship(s):\n\n");
                    for (int i = 0; i < matches.size(); i++) {
                        SmartMatcherService.TicketMatch m = matches.get(i);
                        sb.append("  ──── Match #").append(i + 1)
                                .append(" (Similarity: ").append(String.format("%.0f%%", m.getScore() * 100))
                                .append(") ────\n");
                        sb.append("  Ticket #").append(m.getTicketA().getId())
                                .append(": ").append(m.getTicketA().getComplaintTitle())
                                .append(" [").append(m.getTicketA().getPriority()).append("/")
                                .append(m.getTicketA().getStatus()).append("]\n");
                        sb.append("  Ticket #").append(m.getTicketB().getId())
                                .append(": ").append(m.getTicketB().getComplaintTitle())
                                .append(" [").append(m.getTicketB().getPriority()).append("/")
                                .append(m.getTicketB().getStatus()).append("]\n");
                        Set<String> kw = m.getCommonKeywords();
                        sb.append("  Common Keywords: ").append(String.join(", ", kw)).append("\n\n");
                    }
                }
                sb.append("═══════════════════════════════════════════════\n");

                final String result = sb.toString();
                SwingUtilities.invokeLater(() -> matchArea.setText(result));
            }).start();
        });

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(matchScroll, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════
    // TAB 5 — Reports
    // ════════════════════════════════════════════════════════════════════
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(ThemeConstants.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Stat cards
        JPanel statsGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        statsGrid.setOpaque(false);

        JLabel totalLbl = createReportCard("Total Tickets", "—", ThemeConstants.ACCENT_BLUE);
        JLabel openLbl = createReportCard("Open", "—", ThemeConstants.STATUS_OPEN);
        JLabel progLbl = createReportCard("In Progress", "—", ThemeConstants.STATUS_IN_PROGRESS);
        JLabel resLbl = createReportCard("Resolved", "—", ThemeConstants.STATUS_RESOLVED);
        JLabel closedLbl = createReportCard("Closed", "—", ThemeConstants.STATUS_CLOSED);
        JLabel overdueLbl = createReportCard("Overdue", "—", ThemeConstants.ACCENT_RED);
        JLabel archivedLbl = createReportCard("Archived", "—", ThemeConstants.TEXT_MUTED);

        statsGrid.add(totalLbl.getParent());
        statsGrid.add(openLbl.getParent());
        statsGrid.add(progLbl.getParent());
        statsGrid.add(resLbl.getParent());
        statsGrid.add(closedLbl.getParent());
        statsGrid.add(overdueLbl.getParent());
        statsGrid.add(archivedLbl.getParent());

        JButton refreshBtn = ThemeConstants.createStyledButton("Refresh Reports", ThemeConstants.ACCENT_PURPLE);

        // Summary text area
        JTextArea summaryArea = ThemeConstants.createStyledTextArea(12, 40);
        summaryArea.setEditable(false);
        summaryArea.setFont(ThemeConstants.FONT_MONO);
        summaryArea.setBackground(ThemeConstants.BG_CARD);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBackground(ThemeConstants.BG_CARD);
        summaryScroll.getViewport().setBackground(ThemeConstants.BG_CARD);
        summaryScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR, 1));

        Runnable loadReports = () -> {
            int total = service.countAllTickets();
            int open = service.countByStatus(Ticket.Status.OPEN);
            int prog = service.countByStatus(Ticket.Status.IN_PROGRESS);
            int res = service.countByStatus(Ticket.Status.RESOLVED);
            int closed = service.countByStatus(Ticket.Status.CLOSED);
            int overdue = service.countOverdue();
            int complaints = service.countAllComplaints();
            int archived = service.countArchived();

            SwingUtilities.invokeLater(() -> {
                totalLbl.setText(String.valueOf(total));
                openLbl.setText(String.valueOf(open));
                progLbl.setText(String.valueOf(prog));
                resLbl.setText(String.valueOf(res));
                closedLbl.setText(String.valueOf(closed));
                overdueLbl.setText(String.valueOf(overdue));
                archivedLbl.setText(String.valueOf(archived));

                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════════\n");
                sb.append("       SYSTEM REPORT SUMMARY\n");
                sb.append("═══════════════════════════════════════\n\n");
                sb.append("Total Complaints Filed:   ").append(complaints).append("\n");
                sb.append("Total Tickets Generated:  ").append(total).append("\n\n");
                sb.append("── Status Breakdown ──\n");
                sb.append("  Open:          ").append(open).append(pct(open, total)).append("\n");
                sb.append("  In Progress:   ").append(prog).append(pct(prog, total)).append("\n");
                sb.append("  Resolved:      ").append(res).append(pct(res, total)).append("\n");
                sb.append("  Closed:        ").append(closed).append(pct(closed, total)).append("\n\n");
                sb.append("── SLA Compliance ──\n");
                sb.append("  Overdue:       ").append(overdue).append(pct(overdue, total)).append("\n");
                sb.append("  On Track:      ").append(Math.max(0, total - overdue - res - closed)).append("\n\n");
                double resRate = total > 0 ? ((res + closed) * 100.0 / total) : 0;
                sb.append("── Performance ──\n");
                sb.append("  Resolution Rate: ").append(String.format("%.1f%%", resRate)).append("\n");
                sb.append("  Archived:        ").append(archived).append(" (closed > 2 months)\n");
                sb.append("═══════════════════════════════════════\n");
                summaryArea.setText(sb.toString());
            });
        };

        refreshBtn.addActionListener(e -> new Thread(loadReports).start());

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);
        topPanel.add(statsGrid, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(refreshBtn);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(summaryScroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> new Thread(loadReports).start());
        return panel;
    }

    private String pct(int part, int total) {
        if (total == 0)
            return "";
        return String.format("  (%.1f%%)", part * 100.0 / total);
    }

    private JLabel createReportCard(String title, String value, Color color) {
        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(140, 90));

        JLabel valueLbl = ThemeConstants.createLabel(value, ThemeConstants.FONT_TITLE, color);
        valueLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = ThemeConstants.createLabel(title, ThemeConstants.FONT_BODY, ThemeConstants.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLbl);
        card.add(Box.createVerticalGlue());

        return valueLbl;
    }

    @Override
    public void dispose() {
        engine.stop();
        service.stopLifecycleManager();
        super.dispose();
    }
}
