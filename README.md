# 🎫 Complaint & Ticket Management System

A **desktop-based application** built with **Java Swing** and **MySQL (JDBC)** that enables users to raise complaints and track their status, while administrators manage, update, and prioritize tickets efficiently.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [ER Diagram](#-er-diagram)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Setup & Installation](#-setup--installation)
- [Running the Application](#-running-the-application)
- [Default Credentials](#-default-credentials)
- [Testing](#-testing)
- [Software Requirements Specification](#-software-requirements-specification-srs)

---

## ✨ Features

### User Module
- **Raise Complaints** — Submit new complaints with title, description, and category
- **View My Tickets** — Track all tickets in a sortable table
- **Track Status** — Auto-refreshing status view (polls every 30 seconds)
- **Edit Profile** — Update name and password

### Admin Module
- **All Tickets Dashboard** — Searchable, sortable JTable with priority/date sorting
- **Priority Assignment** — Assign LOW / MEDIUM / HIGH priority with auto SLA recalculation
- **Status Updater** — Transition tickets: Open → In Progress → Resolved → Closed
- **Reports** — Aggregate data with status breakdown, SLA compliance, and resolution rates

### Priority Handling Engine
- **Auto-Escalation** — `ScheduledExecutorService` checks SLA deadlines every 5 minutes
- **Priority Queue** — `PriorityQueue<Ticket>` sorted by priority weight + ticket age
- **Overdue Alerts** — System tray notifications and `JOptionPane` alerts for overdue tickets

### Security
- **SHA-256 Password Hashing** — Secure credential storage
- **Role-Based Access Control** — Separate portals for USER and ADMIN roles
- **Prepared Statements** — SQL injection prevention in all DAO operations

---

## 🏗 Architecture

```
┌──────────────────────────────────────────────┐
│                   UI Layer                    │
│  LoginFrame │ UserPortal │ AdminDashboard     │
├──────────────────────────────────────────────┤
│               Service Layer                   │
│       TicketService │ PriorityEngine          │
├──────────────────────────────────────────────┤
│                 DAO Layer                     │
│ UserDAO │ ComplaintDAO │ TicketDAO │ History   │
├──────────────────────────────────────────────┤
│              Model Layer                      │
│ User │ Complaint │ Ticket │ PriorityLevel     │
├──────────────────────────────────────────────┤
│              Utility Layer                    │
│  DBConnection │ PasswordUtil │ ThemeConstants  │
├──────────────────────────────────────────────┤
│            MySQL Database                     │
│  users │ complaints │ tickets │ priority_levels│
└──────────────────────────────────────────────┘
```

---

## 📊 ER Diagram

```
┌─────────────────┐       ┌─────────────────────┐
│     users        │       │   priority_levels    │
├─────────────────┤       ├─────────────────────┤
│ PK id (INT)      │       │ PK id (INT)          │
│    name          │       │    label (VARCHAR)    │
│    email (UNIQUE)│       │    sla_hours (INT)    │
│    password_hash │       │    sort_order (INT)   │
│    role (ENUM)   │       └──────────┬──────────┘
│    created_at    │                  │
│    updated_at    │                  │ label
└──┬───────┬──────┘                  │
   │       │                         │
   │ user_id│ assigned_admin_id      │
   │       │                         │
   ▼       │                         │
┌──────────┴──────┐       ┌──────────┴──────────┐
│   complaints     │       │      tickets         │
├─────────────────┤       ├─────────────────────┤
│ PK id (INT)      │──────▶│ PK id (INT)          │
│ FK user_id       │ 1:1   │ FK complaint_id      │
│    title         │       │ FK priority           │
│    description   │       │    status (ENUM)      │
│    category      │       │ FK assigned_admin_id  │
│    created_at    │       │    sla_deadline       │
│    updated_at    │       │    created_at         │
└─────────────────┘       │    updated_at         │
                           └──────────┬──────────┘
                                      │
                                      │ ticket_id
                                      ▼
                           ┌─────────────────────┐
                           │   status_history     │
                           ├─────────────────────┤
                           │ PK id (INT)          │
                           │ FK ticket_id         │
                           │    old_status        │
                           │    new_status        │
                           │ FK changed_by        │
                           │    changed_at        │
                           └─────────────────────┘
```

### Relationships
| Relationship | Type | Description |
|---|---|---|
| users → complaints | 1:N | A user can file many complaints |
| complaints → tickets | 1:1 | Each complaint generates one ticket |
| users → tickets | 1:N | An admin can be assigned many tickets |
| priority_levels → tickets | 1:N | Each priority level can apply to many tickets |
| tickets → status_history | 1:N | Each ticket has many status change records |

---

## 📁 Project Structure

```
Complaint & Ticket Management System/
├── config.properties          # Database & app configuration
├── schema.sql                 # MySQL DDL with seed data
├── build.bat                  # Windows build script
├── run.bat                    # Quick compile & run script
├── META-INF/
│   └── MANIFEST.MF            # JAR manifest
├── lib/
│   └── mysql-connector-j-8.3.0.jar  # (download required)
└── src/
    ├── model/
    │   ├── User.java           # User entity with Role enum
    │   ├── Complaint.java      # Complaint entity
    │   ├── Ticket.java         # Ticket entity (Comparable)
    │   ├── PriorityLevel.java  # Priority configuration
    │   └── StatusHistory.java  # Status change tracking
    ├── dao/
    │   ├── UserDAO.java        # User CRUD operations
    │   ├── ComplaintDAO.java   # Complaint CRUD operations
    │   ├── TicketDAO.java      # Ticket CRUD + search + escalation
    │   └── StatusHistoryDAO.java # Status history operations
    ├── service/
    │   ├── TicketService.java  # Business logic coordinator
    │   └── PriorityEngine.java # SLA monitor (ScheduledExecutor)
    ├── ui/
    │   ├── LoginFrame.java     # Login window (main entry point)
    │   ├── UserPortal.java     # User dashboard (4 tabs)
    │   └── AdminDashboard.java # Admin dashboard (4 tabs)
    ├── util/
    │   ├── DBConnection.java   # Singleton JDBC connection manager
    │   ├── PasswordUtil.java   # SHA-256 hashing utility
    │   └── ThemeConstants.java # Dark theme UI constants
    └── test/
        ├── UserDAOTest.java    # JUnit 5 tests for UserDAO
        ├── ComplaintDAOTest.java # JUnit 5 tests for ComplaintDAO
        └── TicketDAOTest.java  # JUnit 5 tests for TicketDAO
```

---

## 🔧 Prerequisites

| Requirement | Version |
|---|---|
| **JDK** | 17 or higher |
| **MySQL Server** | 8.x |
| **MySQL Connector/J** | 8.3.0 (JDBC driver) |
| **JUnit 5** | 5.10+ (for testing only) |

---

## 🚀 Setup & Installation

### 1. Clone / Download the Project

### 2. Set Up MySQL Database
```sql
-- Run the schema file in MySQL Workbench or CLI:
mysql -u root -p < schema.sql
```
This creates the `complaint_mgmt_system` database with all 5 tables and seeds a default admin and user.

### 3. Download MySQL Connector
1. Download `mysql-connector-j-8.3.0.jar` from [MySQL Downloads](https://dev.mysql.com/downloads/connector/j/)
2. Create a `lib/` folder in the project root
3. Place the JAR in `lib/`

### 4. Configure Database Credentials
Edit `config.properties` if your MySQL credentials differ:
```properties
db.url=jdbc:mysql://localhost:3306/complaint_mgmt_system
db.username=root
db.password=your_password
```

---

## ▶ Running the Application

### Option A: Quick Run (Development)
```batch
run.bat
```

### Option B: Build & Run JAR
```batch
build.bat
java -jar dist\TicketManager.jar
```

### Option C: Manual Compilation
```batch
mkdir out
javac -d out -cp "lib\mysql-connector-j-8.3.0.jar" --source 17 src\model\*.java src\util\*.java src\dao\*.java src\service\*.java src\ui\*.java
copy config.properties out\
java -cp "out;lib\mysql-connector-j-8.3.0.jar" ui.LoginFrame
```

---

## 🔑 Default Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@system.com` | `admin123` |
| **User** | `user@system.com` | `password123` |

---

## 🧪 Testing

Run JUnit 5 tests (requires JUnit 5 JAR + MySQL running):
```batch
javac -d out -cp "lib\mysql-connector-j-8.3.0.jar;lib\junit-platform-console-standalone-1.10.0.jar" src\**\*.java
java -jar lib\junit-platform-console-standalone-1.10.0.jar --class-path out --scan-class-path
```

Tests cover:
- ✅ `UserDAO` — Insert, find, update, delete, duplicate email handling
- ✅ `ComplaintDAO` — CRUD operations, user-based queries
- ✅ `TicketDAO` — CRUD, priority updates, status transitions, search, counts

---

## 📄 Software Requirements Specification (SRS)

### 1. Introduction
**Purpose:** A desktop complaint and ticket management system for organizations to handle customer/internal complaints with structured priority handling and SLA enforcement.

**Scope:** The system provides a complete lifecycle for complaint management — from complaint submission to ticket resolution — with role-based access for users and administrators.

### 2. Functional Requirements

| ID | Requirement | Priority |
|---|---|---|
| FR-01 | Users can register and login with email/password | High |
| FR-02 | Users can raise complaints with title, description, category | High |
| FR-03 | System auto-creates tickets from complaints with MEDIUM priority | High |
| FR-04 | Users can view their tickets and track status in real-time | High |
| FR-05 | Admins can view, search, and sort all tickets | High |
| FR-06 | Admins can assign/change ticket priority (LOW/MEDIUM/HIGH) | High |
| FR-07 | Priority changes trigger automatic SLA deadline recalculation | High |
| FR-08 | Admins can update ticket status (OPEN→IN_PROGRESS→RESOLVED→CLOSED) | High |
| FR-09 | System auto-escalates overdue tickets every 5 minutes | Medium |
| FR-10 | System shows alerts for overdue tickets | Medium |
| FR-11 | Admins can view aggregate reports and statistics | Medium |
| FR-12 | Users can update their profile (name, password) | Low |
| FR-13 | All status changes are recorded in history | Medium |

### 3. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-01 | Passwords stored as SHA-256 hashes (never plain text) |
| NFR-02 | All DB queries use PreparedStatements (SQL injection prevention) |
| NFR-03 | UI auto-refreshes ticket data every 30 seconds |
| NFR-04 | SLA engine runs as a daemon thread (non-blocking) |
| NFR-05 | Application runs on JDK 17+ with MySQL 8.x |
| NFR-06 | Modern dark-themed Swing UI with anti-aliased rendering |

### 4. System Constraints
- Requires network access to MySQL server
- Single-instance desktop application
- Windows, macOS, and Linux compatible (Java cross-platform)

---

## 📝 License

This project is developed for educational purposes.
