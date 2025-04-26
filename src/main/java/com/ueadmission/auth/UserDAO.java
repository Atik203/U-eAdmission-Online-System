package com.ueadmission.auth;

import com.ueadmission.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
