package com.ueadmission.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database connection management
 */
public class DatabaseConnection {
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/uiu_admission_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin";
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Connection pool instead of singleton connection
    private static final int MAX_POOL_SIZE = 10;
    private static final List<Connection> connectionPool = new ArrayList<>(MAX_POOL_SIZE);
    
    /**
     * Gets a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    // Lock object for thread safety
    private static final Object CONNECTION_LOCK = new Object();
    private static boolean dbInitialized = false;

    /**
     * Gets a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        synchronized (CONNECTION_LOCK) {
            // Initialize the database if it hasn't been done yet
            if (!dbInitialized) {
                try {
                    // Load the JDBC driver
                    Class.forName("com.mysql.cj.jdbc.Driver");

                    // Create a connection to initialize the database
                    Connection initConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    LOGGER.info("Database connection established successfully for initialization");

                    // Initialize database
                    DatabaseInitializer.initializeDatabase(initConn);
                    dbInitialized = true;

                    // Add this connection to the pool
                    connectionPool.add(initConn);

                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
                    throw new SQLException("MySQL JDBC Driver not found", e);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Database initialization error", e);
                    throw e;
                }
            }

            // Look for a valid connection in the pool
            Connection validConnection = null;

            // Remove invalid connections from the pool
            Iterator<Connection> iterator = connectionPool.iterator();
            while (iterator.hasNext()) {
                Connection conn = iterator.next();
                try {
                    if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                        // Remove invalid connection
                        iterator.remove();
                    } else {
                        // Found a valid connection
                        validConnection = conn;
                        break;
                    }
                } catch (SQLException e) {
                    // Remove problematic connection
                    iterator.remove();
                }
            }

            // If no valid connection found and pool is not full, create a new one
            if (validConnection == null && connectionPool.size() < MAX_POOL_SIZE) {
                try {
                    validConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    connectionPool.add(validConnection);
                    LOGGER.info("New database connection established successfully");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to create new database connection", e);
                    throw e;
                }
            } else if (validConnection == null) {
                LOGGER.warning("Connection pool exhausted, waiting for a connection to become available");
                // Wait for a connection to become available
                try {
                    Thread.sleep(500); // Wait a bit before retrying
                    return getConnection(); // Recursively try again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Thread interrupted while waiting for database connection");
                }
            }

            return validConnection;
        }
    }
    
    /**
     * Closes all database connections
     */
    public static void closeAllConnections() {
        synchronized (CONNECTION_LOCK) {
            for (Connection conn : connectionPool) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing database connection", e);
                }
            }
            connectionPool.clear();
            LOGGER.info("All database connections closed");
        }
    }

    /**
     * Closes a PreparedStatement and ResultSet
     * @param ps PreparedStatement to close
     * @param rs ResultSet to close
     */
    public static void closeResources(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
        }

        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
        }
    }

    /**
     * Backward compatibility method for code that still uses closeConnection
     */
    public static void closeConnection() {
        // This method is kept for backward compatibility
        // Individual connections don't need to be closed as they're returned to the pool
        LOGGER.info("Connection returned to pool");
    }
}
