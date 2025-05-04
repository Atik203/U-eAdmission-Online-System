package com.ueadmission.application.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.application.model.Application;
import com.ueadmission.application.model.ApplicationStatus;
import com.ueadmission.application.model.PaymentStatus;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.db.DatabaseConnection;

/**
 * Service class for handling application-related operations
 */
public class ApplicationService {
    
    private static final Logger LOGGER = Logger.getLogger(ApplicationService.class.getName());
    
    /**
     * Get all applications for the current user
     * 
     * @return A CompletableFuture containing a list of applications
     */
    public CompletableFuture<List<Application>> getUserApplications() {
        return CompletableFuture.supplyAsync(() -> {
            // Convert int ID to String
            int userIdInt = AuthStateManager.getInstance().getState().getUser().getId();
            String userId = String.valueOf(userIdInt);
            List<Application> applications = new ArrayList<>();
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT * FROM applications WHERE user_id = ? ORDER BY application_date DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, userId);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            applications.add(mapResultSetToApplication(rs));
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error fetching user applications", e);
            }
            
            return applications;
        });
    }
    
    /**
     * Process a payment for an application
     * 
     * @param applicationId The ID of the application
     * @param amount The payment amount
     * @return A CompletableFuture containing the updated application
     */
    public CompletableFuture<Application> processPayment(String applicationId, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Start a transaction
                conn.setAutoCommit(false);
                
                try {
                    // Update the application's paid amount
                    String updateQuery = "UPDATE applications SET paid_amount = paid_amount + ?, payment_status = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setDouble(1, amount);
                        
                        // Check the current application to determine new payment status
                        Optional<Application> appOpt = getApplicationById(applicationId);
                        if (!appOpt.isPresent()) {
                            throw new SQLException("Application not found: " + applicationId);
                        }
                        
                        Application app = appOpt.get();
                        double newTotal = app.getPaidAmount() + amount;
                        PaymentStatus newStatus = determinePaymentStatus(newTotal, app.getTotalFee());
                        
                        updateStmt.setString(2, newStatus.name());
                        updateStmt.setString(3, applicationId);
                        
                        int rowsAffected = updateStmt.executeUpdate();
                        if (rowsAffected == 0) {
                            throw new SQLException("No rows affected in update");
                        }
                    }
                    
                    // Insert payment record
                    String insertPaymentQuery = "INSERT INTO payments (application_id, amount, payment_date) VALUES (?, ?, ?)";
                    try (PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentQuery)) {
                        paymentStmt.setString(1, applicationId);
                        paymentStmt.setDouble(2, amount);
                        paymentStmt.setObject(3, LocalDate.now());
                        
                        paymentStmt.executeUpdate();
                    }
                    
                    // Commit the transaction
                    conn.commit();
                    
                    // Return the updated application
                    return getApplicationById(applicationId).orElseThrow(
                        () -> new SQLException("Could not retrieve updated application"));
                    
                } catch (Exception e) {
                    // Roll back the transaction in case of errors
                    conn.rollback();
                    throw e;
                } finally {
                    // Restore auto-commit
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error processing payment", e);
                return null;
            }
        });
    }
    
    /**
     * Determine payment status based on the amount paid and total fee
     * 
     * @param paidAmount The amount paid
     * @param totalFee The total fee
     * @return The payment status
     */
    private PaymentStatus determinePaymentStatus(double paidAmount, double totalFee) {
        if (paidAmount >= totalFee) {
            return PaymentStatus.PAID;
        } else if (paidAmount > 0) {
            return PaymentStatus.PARTIAL;
        } else {
            return PaymentStatus.UNPAID;
        }
    }
    
    /**
     * Get an application by ID
     * 
     * @param applicationId The ID of the application
     * @return An Optional containing the application if found
     */
    public Optional<Application> getApplicationById(String applicationId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM applications WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, applicationId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToApplication(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching application by ID", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Map a ResultSet row to an Application object
     * 
     * @param rs The ResultSet
     * @return The Application object
     * @throws SQLException If an error occurs
     */
    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String fullName = firstName + " " + lastName;
        
        // Safely convert status string to enum - handle case properly
        ApplicationStatus status;
        try {
            // Convert to uppercase to match enum constant naming convention
            status = ApplicationStatus.valueOf(rs.getString("status").toUpperCase());
        } catch (IllegalArgumentException e) {
            // Log the issue and default to PENDING if conversion fails
            LOGGER.warning("Invalid status value in database: " + rs.getString("status") + 
                          ". Defaulting to PENDING. Error: " + e.getMessage());
            status = ApplicationStatus.PENDING;
        }
        
        return new Application(
            rs.getString("id"),
            rs.getString("program"),
            "Summer 2025", // Static semester and year as requested
            rs.getDate("application_date").toLocalDate(),
            status, // Use the safely converted status
            rs.getBoolean("payment_complete") ? PaymentStatus.PAID : PaymentStatus.UNPAID,
            5000.00, // Default fee for now
            rs.getBoolean("payment_complete") ? 5000.00 : 0.0,
            fullName,
            rs.getString("user_id"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getDate("date_of_birth").toLocalDate(),
            rs.getString("gender"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("postal_code"),
            rs.getDouble("ssc_gpa"),
            rs.getDouble("hsc_gpa"),
            rs.getString("ssc_year"),
            rs.getString("hsc_year"),
            rs.getString("father_name"),
            rs.getString("father_occupation"),
            rs.getString("mother_name"),
            rs.getString("mother_occupation"),
            rs.getString("guardian_phone"),
            rs.getString("guardian_email")
        );
    }
}