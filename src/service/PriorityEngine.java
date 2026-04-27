package service;

import model.Ticket;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background engine that checks SLA deadlines every 5 minutes
 * and auto-escalates overdue tickets. Shows system tray / dialog alerts.
 */
public class PriorityEngine {

    private final TicketService service;
    private final ScheduledExecutorService scheduler;
    private TrayIcon trayIcon;

    public PriorityEngine(TicketService service) {
        this.service = service;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SLA-Monitor");
            t.setDaemon(true);
            return t;
        });
        initTray();
    }

    private void initTray() {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image img = Toolkit.getDefaultToolkit().createImage(new byte[0]);
                trayIcon = new TrayIcon(img, "Ticket System SLA Monitor");
                trayIcon.setImageAutoSize(true);
            } catch (Exception e) {
                trayIcon = null;
            }
        }
    }

    /**
     * Starts the SLA monitoring engine.
     * Runs every 5 minutes by default.
     */
    public void start() {
        start(5, TimeUnit.MINUTES);
    }

    public void start(long interval, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::checkAndEscalate, 1, interval, unit);
        System.out.println("Priority Engine started (interval: " + interval + " " + unit + ")");
    }

    /**
     * Stops the engine gracefully.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Priority Engine stopped.");
    }

    /**
     * Core check: escalate overdue tickets and alert.
     */
    private void checkAndEscalate() {
        try {
            // Escalate overdue tickets
            int escalated = service.escalateOverdue();
            if (escalated > 0) {
                System.out.println("Auto-escalated " + escalated + " overdue ticket(s).");
            }

            // Check for remaining overdue tickets and alert
            List<Ticket> overdue = service.getOverdueTickets();
            if (!overdue.isEmpty()) {
                alertOverdue(overdue);
            }
        } catch (Exception e) {
            System.err.println("SLA check error: " + e.getMessage());
        }
    }

    private void alertOverdue(List<Ticket> overdueTickets) {
        StringBuilder msg = new StringBuilder();
        msg.append(overdueTickets.size()).append(" ticket(s) are overdue:\n\n");
        int shown = Math.min(overdueTickets.size(), 5);
        for (int i = 0; i < shown; i++) {
            Ticket t = overdueTickets.get(i);
            msg.append("• Ticket #").append(t.getId())
               .append(" [").append(t.getPriority()).append("] - ")
               .append(t.getComplaintTitle() != null ? t.getComplaintTitle() : "N/A")
               .append("\n");
        }
        if (overdueTickets.size() > 5) {
            msg.append("... and ").append(overdueTickets.size() - 5).append(" more.\n");
        }

        // Show via system tray or dialog
        if (trayIcon != null) {
            trayIcon.displayMessage("SLA Alert", msg.toString(), TrayIcon.MessageType.WARNING);
        }
        // Also show a non-blocking dialog on the EDT
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null, msg.toString(),
                "⚠ Overdue Tickets Alert", JOptionPane.WARNING_MESSAGE)
        );
    }
}
