package com.ueadmission.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database connection management
 */
public class DatabaseConnection {
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/login_schema";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "poi@20470";
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Singleton connection instance
    private static Connection connection = null;
    
    /**
     * Gets a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Create a connection to the database
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.info("Database connection established successfully");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Database connection error", e);
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        } finally {
            connection = null;
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
}
