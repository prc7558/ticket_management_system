-- ============================================================
-- Complaint & Ticket Management System - Database Schema
-- MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS complaint_mgmt_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE complaint_mgmt_system;

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role        ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- ============================================================
-- 2. PRIORITY LEVELS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS priority_levels (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    label       VARCHAR(20) NOT NULL UNIQUE,
    sla_hours   INT NOT NULL DEFAULT 24,
    sort_order  INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

-- Insert default priority levels
INSERT INTO priority_levels (label, sla_hours, sort_order) VALUES
    ('LOW', 72, 1),
    ('MEDIUM', 48, 2),
    ('HIGH', 24, 3)
ON DUPLICATE KEY UPDATE sla_hours = VALUES(sla_hours);

-- ============================================================
-- 3. COMPLAINTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS complaints (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category    VARCHAR(50) DEFAULT 'General',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_complaints_user (user_id),
    INDEX idx_complaints_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- 4. TICKETS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS tickets (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id    INT NOT NULL UNIQUE,
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status          ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    assigned_admin_id INT DEFAULT NULL,
    sla_deadline    TIMESTAMP NULL DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_admin_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (priority) REFERENCES priority_levels(label) ON UPDATE CASCADE,
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_priority (priority),
    INDEX idx_tickets_admin (assigned_admin_id),
    INDEX idx_tickets_sla (sla_deadline)
) ENGINE=InnoDB;

-- ============================================================
-- 5. STATUS HISTORY TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS status_history (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id   INT NOT NULL,
    old_status  VARCHAR(20),
    new_status  VARCHAR(20) NOT NULL,
    changed_by  INT DEFAULT NULL,
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_status_history_ticket (ticket_id),
    INDEX idx_status_history_changed (changed_at)
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA: Default Admin User
-- Password: admin123 (SHA-256 hashed)
-- ============================================================
INSERT INTO users (name, email, password_hash, role) VALUES
    ('System Admin', 'admin@system.com',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
    ('Test User', 'user@system.com',
     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'USER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- admin123 -> SHA-256 -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- password123 -> SHA-256 -> ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
