package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager using DriverManager.
 * Reads configuration from config.properties.
 */
public class DBConnection {

    private static DBConnection instance;
    private final String url;
    private final String username;
    private final String password;

    private DBConnection() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                // Fallback: try loading from file system
                try (InputStream fs = new java.io.FileInputStream("config.properties")) {
                    props.load(fs);
                }
            } else {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not load config.properties - " + e.getMessage());
            throw new RuntimeException("Failed to load database configuration", e);
        }

        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/complaint_mgmt_system");
        this.username = props.getProperty("db.username", "root");
        this.password = props.getProperty("db.password", "root");

        try {
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL JDBC driver not found. Add mysql-connector-j to classpath.");
            throw new RuntimeException("JDBC Driver not found", e);
        }
    }

    /**
     * Returns the singleton instance of DBConnection.
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Gets a new database connection.
     * Callers are responsible for closing this connection.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Tests the database connection and prints status.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✓ Database connection successful: " + conn.getMetaData().getURL());
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            return false;
        }
    }
}
