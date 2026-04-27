package model;

import java.sql.Timestamp;

/**
 * Represents a status change event for a ticket.
 */
public class StatusHistory {

    private int id;
    private int ticketId;
    private String oldStatus;
    private String newStatus;
    private Integer changedBy;
    private Timestamp changedAt;

    // Transient
    private String changedByName;

    public StatusHistory() {}

    public StatusHistory(int ticketId, String oldStatus, String newStatus, Integer changedBy) {
        this.ticketId = ticketId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public Integer getChangedBy() { return changedBy; }
    public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }

    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    @Override
    public String toString() {
        return oldStatus + " → " + newStatus + " at " + changedAt;
    }
}
