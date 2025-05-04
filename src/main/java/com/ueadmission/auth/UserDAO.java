package com.ueadmission.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.db.DatabaseConnection;

/**
 * Data Access Object for user management operations
 */
public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    
    /**
     * Registers a new user in the database
     * @param user The user registration data
     * @return true if registration successful, false otherwise
     */
    public static boolean registerUser(Registration user) {
        String sql = "INSERT INTO users (first_name, last_name, email, phone, address, city, country, password, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getCity());
            ps.setString(7, user.getCountry());
            ps.setString(8, user.getPassword()); // In production, you should hash this password
            ps.setString(9, user.getRole());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering user", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }
    
    /**
     * Authenticates a user with email and password
     * @param email User's email
     * @param password User's password
     * @return User object if authentication successful, null otherwise
     */
    public static Registration authenticateUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, email);
            ps.setString(2, password); // In production, you should verify against a hashed password
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                Registration user = new Registration();
                user.setId(rs.getInt("id")); // Set the user ID
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setCity(rs.getString("city"));
                user.setCountry(rs.getString("country"));
                user.setRole(rs.getString("role"));
                
                // Check if the user is already logged in
                boolean isLoggedIn = rs.getBoolean("is_logged_in");
                if (isLoggedIn) {
                    LOGGER.warning("User " + email + " is already logged in from another location");
                    user.setAlreadyLoggedIn(true);
                }
                
                return user;
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user", e);
            return null;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
    
    /**
     * Update the user's login status and record IP address and timestamp
     * @param userId The user's ID
     * @param ipAddress The user's IP address
     * @param isLoggingIn True if logging in, false if logging out
     * @return true if update successful, false otherwise
     */
    public static boolean updateLoginStatus(int userId, String ipAddress, boolean isLoggingIn) {
        String sql;
        
        if (isLoggingIn) {
            sql = "UPDATE users SET is_logged_in = ?, ip_address = ?, last_login_time = ? WHERE id = ?";
        } else {
            sql = "UPDATE users SET is_logged_in = ? WHERE id = ?";
        }
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            if (isLoggingIn) {
                ps.setBoolean(1, true);
                ps.setString(2, ipAddress);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(4, userId);
            } else {
                ps.setBoolean(1, false);
                ps.setInt(2, userId);
            }
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Updated login status for user ID " + userId + " to " + 
                           (isLoggingIn ? "logged in from " + ipAddress : "logged out"));
                return true;
            } else {
                LOGGER.warning("No rows affected when updating login status for user ID " + userId);
                return false;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating login status", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }
    
    /**
     * Logs out a user by setting their is_logged_in status to false
     * @param userId The user's ID
     * @return true if successful, false otherwise
     */
    public static boolean logoutUser(int userId) {
        return updateLoginStatus(userId, null, false);
    }
    
    /**
     * Force logout of all users - useful for admin operations or system resets
     * @return true if successful, false otherwise
     */
    public static boolean forceLogoutAllUsers() {
        String sql = "UPDATE users SET is_logged_in = false";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            int rowsAffected = ps.executeUpdate();
            LOGGER.info("Force logged out " + rowsAffected + " users");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error force logging out all users", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }
    
    /**
     * Checks if a user is currently logged in
     * @param userId The user's ID
     * @return true if logged in, false otherwise
     */
    public static boolean isUserLoggedIn(int userId) {
        String sql = "SELECT is_logged_in FROM users WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setInt(1, userId);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("is_logged_in");
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user is logged in", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
    
    /**
     * Checks if an email already exists in the database
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, email);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email existence", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
}
