package com.ueadmission.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Handles database initialization from SQL scripts
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    
    /**
     * Initialize the database with the schema SQL
     * @return true if initialization successful, false otherwise
     */
    public static boolean initializeDatabase() {
        try {
            // First ensure tables are created
            try {
                DatabaseInitializer.initializeDatabase();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error initializing database schema", e);
                // Continue to try loading sample data
            }

            // Load SQL script from resources
            InputStream inputStream = DatabaseManager.class.getResourceAsStream("/database/database.sql");
            
            if (inputStream == null) {
                LOGGER.severe("Could not find database.sql script in resources");
                return false;
            }
            
            // Read the SQL script
            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            
            // Split the SQL script on semicolons
            String[] statements = sql.split(";");
            
            // Execute each statement
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            boolean adminCheckDone = false;
            
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    // Check if this is an INSERT statement for admin users
                    String trimmedStatement = statement.trim().toLowerCase();
                    if (trimmedStatement.contains("insert into") && 
                        trimmedStatement.contains("users") && 
                        trimmedStatement.contains("admin@uiu.ac.bd")) {
                        
                        // Check if admin already exists
                        if (!adminCheckDone) {
                            try {
                                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE email='admin@uiu.ac.bd'");
                                if (rs.next() && rs.getInt(1) > 0) {
                                    LOGGER.info("Admin user already exists, skipping insertion");
                                    adminCheckDone = true;
                                    continue; // Skip this statement
                                }
                            } catch (SQLException checkEx) {
                                // Table might not exist yet, so continue with the insert
                                LOGGER.warning("Could not check for existing admin, will try to insert: " + checkEx.getMessage());
                            }
                        } else {
                            // Already checked, skip this statement
                            continue;
                        }
                    }
                    
                    try {
                        stmt.execute(statement);
                    } catch (SQLException stmtEx) {
                        // Check if it's a duplicate key error
                        if (stmtEx.getSQLState() != null && stmtEx.getSQLState().equals("23000")) {
                            LOGGER.warning("Duplicate key detected, continuing with initialization: " + stmtEx.getMessage());
                        } else {
                            // For other errors, log but continue with remaining statements
                            LOGGER.log(Level.WARNING, "Error executing SQL statement, continuing with next statement", stmtEx);
                        }
                    }
                }
            }
            
            LOGGER.info("Database initialized successfully");
            return true;
            
        } catch (SQLException e) {
            // Check if it's a duplicate key error, which is actually okay
            if (e.getSQLState() != null && e.getSQLState().equals("23000")) {
                LOGGER.warning("Database initialization completed with duplicate key warnings: " + e.getMessage());
                return true; // Return true as we can continue with the application
            }
            
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during database initialization", e);
            return false;
        }
    }
}
