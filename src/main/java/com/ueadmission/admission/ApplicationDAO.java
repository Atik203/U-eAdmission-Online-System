package com.ueadmission.admission;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.db.DatabaseConnection;

/**
 * Data Access Object for application management operations
 */
public class ApplicationDAO {
    private static final Logger LOGGER = Logger.getLogger(ApplicationDAO.class.getName());
    
    /**
     * Create a new application in the database
     * @param application The application data
     * @return The ID of the created application, or -1 if creation failed
     */
    public static int createApplication(Application application) {
        String sql = "INSERT INTO applications (user_id, first_name, last_name, email, phone, " +
                "date_of_birth, gender, address, city, postal_code, " +
                "father_name, father_occupation, mother_name, mother_occupation, " +
                "guardian_phone, guardian_email, program, institution, " +
                "ssc_gpa, hsc_gpa, ssc_year, hsc_year, status, payment_complete, application_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, application.getUserId());
            ps.setString(2, application.getFirstName());
            ps.setString(3, application.getLastName());
            ps.setString(4, application.getEmail());
            ps.setString(5, application.getPhoneNumber());
            ps.setDate(6, application.getDateOfBirth() != null ? 
                      java.sql.Date.valueOf(application.getDateOfBirth()) : null);
            ps.setString(7, application.getGender());
            ps.setString(8, application.getAddress());
            ps.setString(9, application.getCity());
            ps.setString(10, application.getPostalCode());
            ps.setString(11, application.getFatherName());
            ps.setString(12, application.getFatherOccupation());
            ps.setString(13, application.getMotherName());
            ps.setString(14, application.getMotherOccupation());
            ps.setString(15, application.getGuardianPhone());
            ps.setString(16, application.getGuardianEmail());
            ps.setString(17, application.getProgram());
            ps.setString(18, application.getInstitution());
            ps.setDouble(19, application.getSscGpa());
            ps.setDouble(20, application.getHscGpa());
            ps.setString(21, application.getSscYear());
            ps.setString(22, application.getHscYear());
            ps.setString(23, application.getStatus());
            ps.setBoolean(24, application.isPaymentComplete());
            ps.setTimestamp(25, application.getApplicationDate() != null ? 
                            java.sql.Timestamp.valueOf(application.getApplicationDate()) : 
                            java.sql.Timestamp.valueOf(LocalDateTime.now()));
                
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated ID
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating application", e);
            return -1;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
    
    /**
     * Update the payment status of an application
     * @param applicationId The ID of the application
     * @param paymentComplete Whether the payment is complete
     * @return true if update was successful, false otherwise
     */
    public static boolean updatePaymentStatus(int applicationId, boolean paymentComplete) {
        String sql = "UPDATE applications SET payment_complete = ?, status = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setBoolean(1, paymentComplete);
            ps.setString(2, paymentComplete ? "Approved" : "Pending");
            ps.setInt(3, applicationId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating payment status", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }
    
    /**
     * Get an application by its ID
     * @param applicationId The ID of the application
     * @return The application, or null if not found
     */
    public static Application getApplicationById(int applicationId) {
        String sql = "SELECT * FROM applications WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setInt(1, applicationId);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return extractApplicationFromResultSet(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting application", e);
            return null;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
    
    /**
     * Get all applications for a user
     * @param userId The ID of the user
     * @return A list of applications, or an empty list if none found
     */
    public static List<Application> getApplicationsByUserId(int userId) {
        String sql = "SELECT * FROM applications WHERE user_id = ? ORDER BY application_date DESC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Application> applications = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setInt(1, userId);
            
            rs = ps.executeQuery();
            
            while (rs.next()) {
                applications.add(extractApplicationFromResultSet(rs));
            }
            
            return applications;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting applications for user", e);
            return applications;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
    
    /**
     * Helper method to extract an application from a result set
     */
    private static Application extractApplicationFromResultSet(ResultSet rs) throws SQLException {
        Application application = new Application();
        
        application.setId(rs.getInt("id"));
        application.setUserId(rs.getInt("user_id"));
        application.setFirstName(rs.getString("first_name"));
        application.setLastName(rs.getString("last_name"));
        application.setEmail(rs.getString("email"));
        application.setPhoneNumber(rs.getString("phone"));
        
        Date dobDate = rs.getDate("date_of_birth");
        if (dobDate != null) {
            application.setDateOfBirth(dobDate.toLocalDate());
        }
        
        application.setGender(rs.getString("gender"));
        application.setAddress(rs.getString("address"));
        application.setCity(rs.getString("city"));
        application.setPostalCode(rs.getString("postal_code"));
        application.setFatherName(rs.getString("father_name"));
        application.setFatherOccupation(rs.getString("father_occupation"));
        application.setMotherName(rs.getString("mother_name"));
        application.setMotherOccupation(rs.getString("mother_occupation"));
        application.setGuardianPhone(rs.getString("guardian_phone"));
        application.setGuardianEmail(rs.getString("guardian_email"));
        application.setProgram(rs.getString("program"));
        application.setInstitution(rs.getString("institution"));
        application.setSscGpa(rs.getDouble("ssc_gpa"));
        application.setHscGpa(rs.getDouble("hsc_gpa"));
        application.setSscYear(rs.getString("ssc_year"));
        application.setHscYear(rs.getString("hsc_year"));
        application.setStatus(rs.getString("status"));
        application.setPaymentComplete(rs.getBoolean("payment_complete"));
        
        Timestamp applicationDate = rs.getTimestamp("application_date");
        if (applicationDate != null) {
            application.setApplicationDate(applicationDate.toLocalDateTime());
        }
        
        return application;
    }
}