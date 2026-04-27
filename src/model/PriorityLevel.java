package model;

/**
 * Represents a priority level configuration (LOW, MEDIUM, HIGH).
 */
public class PriorityLevel {

    private int id;
    private String label;
    private int slaHours;
    private int sortOrder;

    public PriorityLevel() {}

    public PriorityLevel(String label, int slaHours, int sortOrder) {
        this.label = label;
        this.slaHours = slaHours;
        this.sortOrder = sortOrder;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getSlaHours() { return slaHours; }
    public void setSlaHours(int slaHours) { this.slaHours = slaHours; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    @Override
    public String toString() {
        return label + " (SLA: " + slaHours + "h)";
    }
}
