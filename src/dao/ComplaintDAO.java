package dao;

import model.Complaint;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the complaints table.
 */
public class ComplaintDAO {

    /**
     * Inserts a new complaint.
     * @return the generated complaint ID, or -1 on failure
     */
    public int insertComplaint(Complaint complaint) {
        String sql = "INSERT INTO complaints (user_id, title, description, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, complaint.getUserId());
            ps.setString(2, complaint.getTitle());
            ps.setString(3, complaint.getDescription());
            ps.setString(4, complaint.getCategory());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    complaint.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting complaint: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Gets a complaint by ID.
     */
    public Complaint findById(int id) {
        String sql = """
            SELECT c.*, u.name AS user_name
            FROM complaints c
            JOIN users u ON c.user_id = u.id
            WHERE c.id = ?
            """;
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding complaint: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all complaints for a specific user.
     */
    public List<Complaint> findByUserId(int userId) {
        String sql = """
            SELECT c.*, u.name AS user_name
            FROM complaints c
            JOIN users u ON c.user_id = u.id
            WHERE c.user_id = ?
            ORDER BY c.created_at DESC
            """;
        List<Complaint> complaints = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user complaints: " + e.getMessage());
        }
        return complaints;
    }

    /**
     * Gets all complaints.
     */
    public List<Complaint> findAll() {
        String sql = """
            SELECT c.*, u.name AS user_name
            FROM complaints c
            JOIN users u ON c.user_id = u.id
            ORDER BY c.created_at DESC
            """;
        List<Complaint> complaints = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all complaints: " + e.getMessage());
        }
        return complaints;
    }

    /**
     * Updates a complaint.
     */
    public boolean updateComplaint(Complaint complaint) {
        String sql = "UPDATE complaints SET title = ?, description = ?, category = ? WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, complaint.getTitle());
            ps.setString(2, complaint.getDescription());
            ps.setString(3, complaint.getCategory());
            ps.setInt(4, complaint.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating complaint: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a complaint by ID.
     */
    public boolean deleteComplaint(int id) {
        String sql = "DELETE FROM complaints WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting complaint: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts total complaints.
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM complaints";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting complaints: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Maps a ResultSet row to a Complaint object.
     */
    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        try {
            c.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {
            // user_name may not be in all queries
        }
        return c;
    }
}
