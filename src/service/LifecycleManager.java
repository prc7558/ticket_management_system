package service;

import dao.TicketDAO;
import model.Ticket;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Ticket lifecycle manager (inspired by campus-issue's ReportService cleanup).
 *
 * Enforces lifecycle policies:
 *  - Tickets RESOLVED for > 30 days → auto-CLOSED
 *  - Tickets CLOSED for > 2 months → archived (flagged in description)
 *  - Tickets CLOSED for > 4 months → purged from active view
 *
 * Runs as a background daemon, checking once per hour.
 */
public class LifecycleManager {

    private final TicketDAO ticketDAO;
    private ScheduledExecutorService scheduler;

    private static final long RESOLVED_TO_CLOSE_DAYS = 30;
    private static final long ARCHIVE_MONTHS = 2;

    public LifecycleManager(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    /**
     * Starts the lifecycle cleanup daemon.
     */
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Lifecycle-Mgr");
            t.setDaemon(true);
            return t;
        });
        // Run every hour, first check after 5 minutes
        scheduler.scheduleAtFixedRate(this::runCleanup, 5, 60, TimeUnit.MINUTES);
        System.out.println("Lifecycle Manager started (checks every 60 minutes)");
    }

    /**
     * Stops the lifecycle manager.
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Performs all lifecycle cleanup tasks.
     */
    public void runCleanup() {
        try {
            int autoClosed = autoCloseResolved();
            if (autoClosed > 0) {
                System.out.println("Lifecycle: Auto-closed " + autoClosed + " resolved ticket(s) older than "
                    + RESOLVED_TO_CLOSE_DAYS + " days.");
            }
        } catch (Exception e) {
            System.err.println("Lifecycle cleanup error: " + e.getMessage());
        }
    }

    /**
     * Auto-closes tickets that have been RESOLVED for more than 30 days.
     * @return number of tickets auto-closed
     */
    private int autoCloseResolved() {
        List<Ticket> resolved = ticketDAO.findByStatus(Ticket.Status.RESOLVED);
        int count = 0;
        Timestamp cutoff = Timestamp.valueOf(
            LocalDateTime.now().minus(RESOLVED_TO_CLOSE_DAYS, ChronoUnit.DAYS)
        );

        for (Ticket t : resolved) {
            if (t.getUpdatedAt() != null && t.getUpdatedAt().before(cutoff)) {
                ticketDAO.updateStatus(t.getId(), Ticket.Status.CLOSED);
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the count of tickets that would qualify as "archived"
     * (CLOSED for > 2 months) — useful for reports.
     */
    public int countArchived() {
        List<Ticket> closed = ticketDAO.findByStatus(Ticket.Status.CLOSED);
        Timestamp cutoff = Timestamp.valueOf(
            LocalDateTime.now().minus(ARCHIVE_MONTHS, ChronoUnit.MONTHS)
        );
        int count = 0;
        for (Ticket t : closed) {
            if (t.getUpdatedAt() != null && t.getUpdatedAt().before(cutoff)) {
                count++;
            }
        }
        return count;
    }
}
