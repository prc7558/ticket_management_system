package dao;

import model.StatusHistory;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the status_history table.
 */
public class StatusHistoryDAO {

    public int insert(StatusHistory sh) {
        String sql = "INSERT INTO status_history (ticket_id, old_status, new_status, changed_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sh.getTicketId());
            ps.setString(2, sh.getOldStatus());
            ps.setString(3, sh.getNewStatus());
            if (sh.getChangedBy() != null) ps.setInt(4, sh.getChangedBy());
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) { sh.setId(rs.getInt(1)); return sh.getId(); }
            }
        } catch (SQLException e) { System.err.println("Error inserting status history: " + e.getMessage()); }
        return -1;
    }

    public List<StatusHistory> findByTicketId(int ticketId) {
        String sql = "SELECT sh.*, u.name AS changed_by_name FROM status_history sh LEFT JOIN users u ON sh.changed_by = u.id WHERE sh.ticket_id = ? ORDER BY sh.changed_at DESC";
        List<StatusHistory> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("Error fetching status history: " + e.getMessage()); }
        return list;
    }

    private StatusHistory mapRow(ResultSet rs) throws SQLException {
        StatusHistory sh = new StatusHistory();
        sh.setId(rs.getInt("id"));
        sh.setTicketId(rs.getInt("ticket_id"));
        sh.setOldStatus(rs.getString("old_status"));
        sh.setNewStatus(rs.getString("new_status"));
        int cb = rs.getInt("changed_by");
        sh.setChangedBy(rs.wasNull() ? null : cb);
        sh.setChangedAt(rs.getTimestamp("changed_at"));
        try { sh.setChangedByName(rs.getString("changed_by_name")); } catch (SQLException ignored) {}
        return sh;
    }
}
