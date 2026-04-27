package model;

import java.sql.Timestamp;

/**
 * Represents a ticket linked to a complaint, with priority and status tracking.
 * Implements Comparable for PriorityQueue ordering.
 */
public class Ticket implements Comparable<Ticket> {

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    private int id;
    private int complaintId;
    private String priority;          // LOW, MEDIUM, HIGH
    private Status status;
    private Integer assignedAdminId;
    private Timestamp slaDeadline;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient fields for display joins
    private String complaintTitle;
    private String complaintDescription;
    private String complaintCategory;
    private String userName;
    private String userEmail;
    private int userId;
    private String assignedAdminName;
    private int slaHours;

    public Ticket() {}

    public Ticket(int complaintId, String priority, Status status) {
        this.complaintId = complaintId;
        this.priority = priority;
        this.status = status;
    }

    // Priority weight for ordering (higher = more urgent)
    private int getPriorityWeight() {
        return switch (priority.toUpperCase()) {
            case "HIGH"   -> 3;
            case "MEDIUM" -> 2;
            case "LOW"    -> 1;
            default       -> 0;
        };
    }

    /**
     * Comparator: higher priority first, then older tickets first.
     */
    @Override
    public int compareTo(Ticket other) {
        int cmp = Integer.compare(other.getPriorityWeight(), this.getPriorityWeight());
        if (cmp != 0) return cmp;
        // Older tickets get higher precedence
        if (this.createdAt != null && other.createdAt != null) {
            return this.createdAt.compareTo(other.createdAt);
        }
        return Integer.compare(this.id, other.id);
    }

    /**
     * Checks whether this ticket is overdue based on its SLA deadline.
     */
    public boolean isOverdue() {
        if (slaDeadline == null) return false;
        if (status == Status.RESOLVED || status == Status.CLOSED) return false;
        return new Timestamp(System.currentTimeMillis()).after(slaDeadline);
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getAssignedAdminId() { return assignedAdminId; }
    public void setAssignedAdminId(Integer assignedAdminId) { this.assignedAdminId = assignedAdminId; }

    public Timestamp getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(Timestamp slaDeadline) { this.slaDeadline = slaDeadline; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getComplaintTitle() { return complaintTitle; }
    public void setComplaintTitle(String complaintTitle) { this.complaintTitle = complaintTitle; }

    public String getComplaintDescription() { return complaintDescription; }
    public void setComplaintDescription(String d) { this.complaintDescription = d; }

    public String getComplaintCategory() { return complaintCategory; }
    public void setComplaintCategory(String complaintCategory) { this.complaintCategory = complaintCategory; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAssignedAdminName() { return assignedAdminName; }
    public void setAssignedAdminName(String assignedAdminName) { this.assignedAdminName = assignedAdminName; }

    public int getSlaHours() { return slaHours; }
    public void setSlaHours(int slaHours) { this.slaHours = slaHours; }

    @Override
    public String toString() {
        return "Ticket #" + id + " [" + priority + "/" + status + "] - " + complaintTitle;
    }
}
