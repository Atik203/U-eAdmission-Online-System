package com.ueadmission.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles database initialization and schema creation
 */
public class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    /**
     * Initialize the database with all required tables
     * @return true if initialization was successful
     */
    public static boolean initializeDatabase() {
        LOGGER.info("Initializing database schema...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            return initializeDatabase(conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
            return false;
        }
    }

    /**
     * Initialize the database with all required tables using a provided connection
     * @param conn The database connection to use
     * @return true if initialization was successful
     */
    public static boolean initializeDatabase(Connection conn) {
        LOGGER.info("Initializing database schema with provided connection...");
        try {
            // Initialize users table
            initializeUsersTable(conn);

            // Initialize chat tables
            initializeChatTables(conn);

            LOGGER.info("Database schema initialized successfully");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
            return false;
        }
    }

    /**
     * Initialize users table
     */
    private static void initializeUsersTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "username VARCHAR(50) NOT NULL UNIQUE, " +
                     "password VARCHAR(100) NOT NULL, " +
                     "email VARCHAR(100) NOT NULL UNIQUE, " +
                     "full_name VARCHAR(100) NOT NULL, " +
                     "role ENUM('admin', 'student', 'faculty') NOT NULL, " +
                     "status ENUM('active', 'inactive', 'pending') DEFAULT 'active', " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "last_login TIMESTAMP NULL" +
                     ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Users table initialized");
        }
    }

    /**
     * Initialize chat-related tables
     */
    private static void initializeChatTables(Connection conn) throws SQLException {
        try {
            // Create chat_messages table
            String chatMessagesSQL = "CREATE TABLE IF NOT EXISTS chat_messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender_id INT NOT NULL, " +
                    "receiver_id INT, " +
                    "message TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "is_broadcast BOOLEAN DEFAULT FALSE, " +
                    "is_read BOOLEAN DEFAULT FALSE, " +
                    "INDEX (sender_id), " +
                    "INDEX (receiver_id), " +
                    "INDEX (timestamp))";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(chatMessagesSQL);
                LOGGER.info("chat_messages table created");
            }

            // Create chat_messages_queue table
            String chatQueueSQL = "CREATE TABLE IF NOT EXISTS chat_messages_queue (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender_id INT NOT NULL, " +
                    "receiver_id INT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "sent BOOLEAN DEFAULT FALSE, " +
                    "attempts INT DEFAULT 0, " +
                    "INDEX (sender_id), " +
                    "INDEX (timestamp))";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(chatQueueSQL);
                LOGGER.info("chat_messages_queue table created");
            }

            // Create user_status table
            String userStatusSQL = "CREATE TABLE IF NOT EXISTS user_status (" +
                    "user_id INT PRIMARY KEY, " +
                    "status VARCHAR(20) DEFAULT 'offline', " +
                    "last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX (status))";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(userStatusSQL);
                LOGGER.info("user_status table created");
            }

            // Insert default status for existing users
            try {
                String insertSql = "INSERT IGNORE INTO user_status (user_id, status) " +
                                  "SELECT id, 'offline' FROM users";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(insertSql);
                    LOGGER.info("Default user statuses initialized");
                }
            } catch (SQLException e) {
                // This may fail if users table doesn't exist yet, which is okay
                LOGGER.log(Level.INFO, "Could not initialize user_status data: " + e.getMessage());
            }

            LOGGER.info("Chat tables initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize chat tables", e);
            throw e;
        }
    }
}
