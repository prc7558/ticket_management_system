package dao;

import model.Ticket;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the tickets table.
 */
public class TicketDAO {

    private static final String BASE_SELECT = """
        SELECT t.*, c.title AS complaint_title, c.description AS complaint_description,
               c.category AS complaint_category, c.user_id AS user_id,
               u.name AS user_name, u.email AS user_email,
               a.name AS assigned_admin_name, pl.sla_hours
        FROM tickets t
        JOIN complaints c ON t.complaint_id = c.id
        JOIN users u ON c.user_id = u.id
        LEFT JOIN users a ON t.assigned_admin_id = a.id
        LEFT JOIN priority_levels pl ON t.priority = pl.label
        """;

    public int insertTicket(Ticket ticket) {
        String sql = "INSERT INTO tickets (complaint_id, priority, status, assigned_admin_id, sla_deadline) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ticket.getComplaintId());
            ps.setString(2, ticket.getPriority());
            ps.setString(3, ticket.getStatus().name());
            if (ticket.getAssignedAdminId() != null) ps.setInt(4, ticket.getAssignedAdminId());
            else ps.setNull(4, Types.INTEGER);
            if (ticket.getSlaDeadline() != null) ps.setTimestamp(5, ticket.getSlaDeadline());
            else ps.setNull(5, Types.TIMESTAMP);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) { ticket.setId(rs.getInt(1)); return ticket.getId(); }
            }
        } catch (SQLException e) { System.err.println("Error creating ticket: " + e.getMessage()); }
        return -1;
    }

    public Ticket findById(int id) {
        String sql = BASE_SELECT + " WHERE t.id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapRow(rs); }
        } catch (SQLException e) { System.err.println("Error finding ticket: " + e.getMessage()); }
        return null;
    }

    public List<Ticket> findByUserId(int userId) {
        return executeQuery(BASE_SELECT + " WHERE c.user_id = ? ORDER BY t.created_at DESC", userId);
    }

    public List<Ticket> findByAdminId(int adminId) {
        return executeQuery(BASE_SELECT + " WHERE t.assigned_admin_id = ? ORDER BY t.created_at DESC", adminId);
    }

    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " ORDER BY t.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("Error fetching tickets: " + e.getMessage()); }
        return tickets;
    }

    public List<Ticket> findByStatus(Ticket.Status status) {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE t.status = ? ORDER BY t.created_at DESC")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) tickets.add(mapRow(rs)); }
        } catch (SQLException e) { System.err.println("Error fetching by status: " + e.getMessage()); }
        return tickets;
    }

    public List<Ticket> findOverdueTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE t.sla_deadline < NOW() AND t.status NOT IN ('RESOLVED','CLOSED') ORDER BY t.sla_deadline ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("Error fetching overdue: " + e.getMessage()); }
        return tickets;
    }

    public boolean updatePriority(int ticketId, String newPriority) {
        String sql = "UPDATE tickets t JOIN priority_levels pl ON pl.label = ? SET t.priority = ?, t.sla_deadline = TIMESTAMPADD(HOUR, pl.sla_hours, t.created_at) WHERE t.id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPriority); ps.setString(2, newPriority); ps.setInt(3, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error updating priority: " + e.getMessage()); }
        return false;
    }

    public boolean updateStatus(int ticketId, Ticket.Status newStatus) {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name()); ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error updating status: " + e.getMessage()); }
        return false;
    }

    public boolean assignAdmin(int ticketId, int adminId) {
        String sql = "UPDATE tickets SET assigned_admin_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId); ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error assigning admin: " + e.getMessage()); }
        return false;
    }

    public int escalateOverdueTickets() {
        String sql = """
            UPDATE tickets t JOIN priority_levels pl ON pl.label = CASE
                WHEN t.priority = 'LOW' THEN 'MEDIUM' WHEN t.priority = 'MEDIUM' THEN 'HIGH' ELSE t.priority END
            SET t.priority = CASE WHEN t.priority = 'LOW' THEN 'MEDIUM' WHEN t.priority = 'MEDIUM' THEN 'HIGH' ELSE t.priority END,
                t.sla_deadline = TIMESTAMPADD(HOUR, pl.sla_hours, NOW())
            WHERE t.sla_deadline < NOW() AND t.status NOT IN ('RESOLVED','CLOSED') AND t.priority != 'HIGH'
            """;
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) { System.err.println("Error escalating: " + e.getMessage()); }
        return 0;
    }

    public int countByStatus(Ticket.Status status) {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tickets WHERE status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { System.err.println("Error counting: " + e.getMessage()); }
        return 0;
    }

    public int countAll() {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tickets");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("Error counting all: " + e.getMessage()); }
        return 0;
    }

    public int countOverdue() {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tickets WHERE sla_deadline < NOW() AND status NOT IN ('RESOLVED','CLOSED')");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("Error counting overdue: " + e.getMessage()); }
        return 0;
    }

    public List<Ticket> search(String query) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE c.title LIKE ? OR u.name LIKE ? OR c.category LIKE ? ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + query + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) tickets.add(mapRow(rs)); }
        } catch (SQLException e) { System.err.println("Error searching: " + e.getMessage()); }
        return tickets;
    }

    private List<Ticket> executeQuery(String sql, int param) {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) tickets.add(mapRow(rs)); }
        } catch (SQLException e) { System.err.println("Error executing query: " + e.getMessage()); }
        return tickets;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getInt("id"));
        t.setComplaintId(rs.getInt("complaint_id"));
        t.setPriority(rs.getString("priority"));
        t.setStatus(Ticket.Status.valueOf(rs.getString("status")));
        int adminId = rs.getInt("assigned_admin_id");
        t.setAssignedAdminId(rs.wasNull() ? null : adminId);
        t.setSlaDeadline(rs.getTimestamp("sla_deadline"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));
        try { t.setComplaintTitle(rs.getString("complaint_title")); } catch (SQLException ignored) {}
        try { t.setComplaintDescription(rs.getString("complaint_description")); } catch (SQLException ignored) {}
        try { t.setComplaintCategory(rs.getString("complaint_category")); } catch (SQLException ignored) {}
        try { t.setUserId(rs.getInt("user_id")); } catch (SQLException ignored) {}
        try { t.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        try { t.setUserEmail(rs.getString("user_email")); } catch (SQLException ignored) {}
        try { t.setAssignedAdminName(rs.getString("assigned_admin_name")); } catch (SQLException ignored) {}
        try { t.setSlaHours(rs.getInt("sla_hours")); } catch (SQLException ignored) {}
        return t;
    }
}
