package model;

import java.sql.Timestamp;

/**
 * Represents a complaint raised by a user.
 */
public class Complaint {

    private int id;
    private int userId;
    private String title;
    private String description;
    private String category;
    private boolean anonymous;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient fields for display
    private String userName;

    public Complaint() {}

    public Complaint(int userId, String title, String description, String category) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.anonymous = false;
    }

    public Complaint(int userId, String title, String description, String category, boolean anonymous) {
        this(userId, title, description, category);
        this.anonymous = anonymous;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " (" + category + ")";
    }
}
