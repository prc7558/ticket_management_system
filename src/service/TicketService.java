package service;

import dao.*;
import model.*;
import util.PasswordUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Business logic service layer coordinating DAO operations.
 * Integrates SmartMatcherService and LifecycleManager (from campus-issue reference).
 */
public class TicketService {

    private final UserDAO userDAO = new UserDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final StatusHistoryDAO historyDAO = new StatusHistoryDAO();

    // Integrated from campus-issue reference
    private final SmartMatcherService matcher = new SmartMatcherService();
    private final LifecycleManager lifecycleManager;

    public TicketService() {
        this.lifecycleManager = new LifecycleManager(ticketDAO);
    }

    // ── Authentication ──

    public User authenticate(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user != null && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public User registerUser(String name, String email, String password, User.Role role) {
        User user = new User(name, email, PasswordUtil.hashPassword(password), role);
        int id = userDAO.insertUser(user);
        if (id > 0) return user;
        return null;
    }

    // ── Complaint & Ticket Creation ──

    public Ticket raiseComplaint(int userId, String title, String description, String category) {
        return raiseComplaint(userId, title, description, category, false);
    }

    /**
     * Raises a complaint with optional anonymous flag (inspired by campus-issue CIRS).
     */
    public Ticket raiseComplaint(int userId, String title, String description, String category, boolean anonymous) {
        Complaint complaint = new Complaint(userId, title, description, category, anonymous);
        int cId = complaintDAO.insertComplaint(complaint);
        if (cId <= 0) return null;

        // Auto-create ticket with MEDIUM priority
        Ticket ticket = new Ticket(cId, "MEDIUM", Ticket.Status.OPEN);
        // SLA deadline = now + 48 hours (MEDIUM)
        long slaMs = System.currentTimeMillis() + (48L * 60 * 60 * 1000);
        ticket.setSlaDeadline(new Timestamp(slaMs));
        int tId = ticketDAO.insertTicket(ticket);
        if (tId <= 0) return null;

        // Record initial status
        historyDAO.insert(new StatusHistory(tId, null, "OPEN", null));

        return ticketDAO.findById(tId);
    }

    // ── Ticket Operations ──

    public List<Ticket> getUserTickets(int userId) {
        return ticketDAO.findByUserId(userId);
    }

    public List<Ticket> getAllTickets() {
        return ticketDAO.findAll();
    }

    public List<Ticket> searchTickets(String query) {
        return ticketDAO.search(query);
    }

    public Ticket getTicket(int ticketId) {
        return ticketDAO.findById(ticketId);
    }

    public boolean updatePriority(int ticketId, String newPriority, int adminId) {
        Ticket old = ticketDAO.findById(ticketId);
        if (old == null) return false;
        boolean ok = ticketDAO.updatePriority(ticketId, newPriority);
        if (ok) {
            historyDAO.insert(new StatusHistory(ticketId, "Priority:" + old.getPriority(), "Priority:" + newPriority, adminId));
        }
        return ok;
    }

    public boolean updateStatus(int ticketId, Ticket.Status newStatus, int changedBy) {
        Ticket old = ticketDAO.findById(ticketId);
        if (old == null) return false;
        boolean ok = ticketDAO.updateStatus(ticketId, newStatus);
        if (ok) {
            historyDAO.insert(new StatusHistory(ticketId, old.getStatus().name(), newStatus.name(), changedBy));
        }
        return ok;
    }

    public boolean assignAdmin(int ticketId, int adminId) {
        return ticketDAO.assignAdmin(ticketId, adminId);
    }

    // ── Priority Queue ──

    public PriorityQueue<Ticket> getPrioritizedQueue() {
        PriorityQueue<Ticket> queue = new PriorityQueue<>();
        List<Ticket> active = ticketDAO.findAll();
        for (Ticket t : active) {
            if (t.getStatus() != Ticket.Status.CLOSED && t.getStatus() != Ticket.Status.RESOLVED) {
                queue.offer(t);
            }
        }
        return queue;
    }

    // ── SLA / Escalation ──

    public List<Ticket> getOverdueTickets() {
        return ticketDAO.findOverdueTickets();
    }

    public int escalateOverdue() {
        return ticketDAO.escalateOverdueTickets();
    }

    // ── Smart Matcher (from campus-issue) ──

    /**
     * Finds related/duplicate tickets using keyword matching.
     */
    public List<SmartMatcherService.TicketMatch> findRelatedTickets() {
        return matcher.findRelatedTickets(ticketDAO.findAll());
    }

    /**
     * Finds tickets related to a specific ticket.
     */
    public List<SmartMatcherService.TicketMatch> findRelatedTo(int ticketId) {
        Ticket target = ticketDAO.findById(ticketId);
        if (target == null) return List.of();
        return matcher.findRelatedTo(target, ticketDAO.findAll());
    }

    // ── Lifecycle Manager (from campus-issue) ──

    public LifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    public void startLifecycleManager() {
        lifecycleManager.start();
    }

    public void stopLifecycleManager() {
        lifecycleManager.stop();
    }

    // ── Reports / Stats ──

    public int countByStatus(Ticket.Status status) { return ticketDAO.countByStatus(status); }
    public int countAllTickets() { return ticketDAO.countAll(); }
    public int countOverdue() { return ticketDAO.countOverdue(); }
    public int countAllComplaints() { return complaintDAO.countAll(); }
    public int countArchived() { return lifecycleManager.countArchived(); }

    // ── Status History ──

    public List<StatusHistory> getTicketHistory(int ticketId) {
        return historyDAO.findByTicketId(ticketId);
    }

    // ── User management ──

    public boolean updateProfile(User user) { return userDAO.updateUser(user); }
    public User getUser(int id) { return userDAO.findById(id); }
    public List<User> getAllAdmins() { return userDAO.findAllAdmins(); }
    public List<User> getAllUsers() { return userDAO.findAll(); }
}
