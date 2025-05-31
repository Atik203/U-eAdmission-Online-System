package com.ueadmission.managestudent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.application.model.Application;
import com.ueadmission.application.model.ApplicationStatus;
import com.ueadmission.application.model.PaymentStatus;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Controller for the Manage Students page
 * This page allows administrators to view all student applications
 */
public class ManageStudentsController {
    
    private static final Logger LOGGER = Logger.getLogger(ManageStudentsController.class.getName());
    
    @FXML
    private ListView<Application> applicationListView;
    
    @FXML
    private Label noApplicationsLabel;
    
    @FXML
    private StackPane loaderContainer;
    
    @FXML
    private VBox applicationContainer;
    
    @FXML
    private MFXButton homeButton;
    
    @FXML
    private MFXButton aboutButton;
    
    @FXML
    private MFXButton admissionButton;
    
    @FXML
    private MFXButton examPortalButton;
    
    @FXML
    private MFXButton contactButton;
    
    @FXML
    private HBox loginButtonContainer;
    
    @FXML
    private HBox profileButtonContainer;
    
    @FXML
    private MFXButton refreshButton;
    
    @FXML
    private MFXSpinner<?> spinner;
    
    // Application details labels
    @FXML private Label programLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label addressLabel;
    @FXML private Label cityLabel;
    @FXML private Label postalCodeLabel;
    @FXML private Label sscGpaLabel;
    @FXML private Label hscGpaLabel;
    @FXML private Label sscYearLabel;
    @FXML private Label hscYearLabel;
    @FXML private Label fatherNameLabel;
    @FXML private Label fatherOccupationLabel;
    @FXML private Label motherNameLabel;
    @FXML private Label motherOccupationLabel;
    @FXML private Label guardianPhoneLabel;
    @FXML private Label guardianEmailLabel;
    @FXML private Label statusLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Label applicationDateLabel;
    
    // Observable list to hold applications
    private final ObservableList<Application> applications = FXCollections.observableArrayList();
    
    // Currently selected application
    private final ObjectProperty<Application> selectedApplication = new SimpleObjectProperty<>();
    
    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up ListView with custom cell factory
        applicationListView.setItems(applications);
        applicationListView.setCellFactory(createCellFactory());
        
        // Add selection change listener to update details when a row is selected
        applicationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedApplication.set(newVal);
            updateApplicationDetails(newVal);
        });
        
        // Set up navigation buttons with null checks
        if (homeButton != null) {
            homeButton.setOnAction(this::navigateToHome);
        }
        if (aboutButton != null) {
            aboutButton.setOnAction(this::navigateToAbout);
        }
        if (admissionButton != null) {
            admissionButton.setOnAction(this::navigateToAdmission);
        }
        if (examPortalButton != null) {
            examPortalButton.setOnAction(this::navigateToExamPortal);
        }
        if (contactButton != null) {
            contactButton.setOnAction(this::navigateToContact);
        }
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> refreshApplications());
        }
        
        // Load applications data
        showLoader();
        loadAllApplications();
        
        // Update UI based on authentication state
        refreshUI();
    }
    
    /**
     * Create the cell factory for the application list view
     */
    private Callback<ListView<Application>, ListCell<Application>> createCellFactory() {
        return listView -> new ListCell<Application>() {
            private final HBox container = new HBox();
            private final Label nameLabel = new Label();
            private final Label emailLabel = new Label();
            private final Label programLabel = new Label();
            private final Label semesterLabel = new Label();
            private final Label yearLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label statusLabel = new Label();
            private final Label paymentLabel = new Label();
            
            {
                // Configure the cell layout
                container.getStyleClass().add("application-list-item");
                container.setSpacing(10);
                container.setAlignment(Pos.CENTER_LEFT);
                
                // Configure the labels with specific column classes
                nameLabel.getStyleClass().addAll("cell-label", "name-column");
                emailLabel.getStyleClass().addAll("cell-label", "email-column");
                programLabel.getStyleClass().addAll("cell-label", "program-column");
                semesterLabel.getStyleClass().addAll("cell-label", "semester-column");
                yearLabel.getStyleClass().addAll("cell-label", "year-column");
                dateLabel.getStyleClass().addAll("cell-label", "date-column");
                statusLabel.getStyleClass().addAll("cell-label", "status-column");
                paymentLabel.getStyleClass().addAll("cell-label", "payment-column");
                
                // Add to container
                container.getChildren().addAll(
                    nameLabel, emailLabel, programLabel, semesterLabel, yearLabel, dateLabel, statusLabel, paymentLabel);
            }
            
            @Override
            protected void updateItem(Application app, boolean empty) {
                super.updateItem(app, empty);
                
                if (empty || app == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                
                // Update the labels with application data
                nameLabel.setText(app.getApplicantName());
                emailLabel.setText(app.getEmail());
                programLabel.setText(app.getProgramName());
                
                // Extract semester and year from app.getSemesterAndYear() (which returns "Summer 2025")
                String[] semesterYear = app.getSemesterAndYear().split(" ");
                semesterLabel.setText(semesterYear.length > 0 ? semesterYear[0] : "");
                yearLabel.setText(semesterYear.length > 1 ? semesterYear[1] : "");
                
                dateLabel.setText(app.getApplicationDate().format(dateFormatter));
                
                // Set status with appropriate style
                statusLabel.setText(app.getStatus().getDisplayName());
                statusLabel.getStyleClass().setAll("cell-label", "status-column", app.getStatus().getStyleClass());
                
                // Set payment status
                if (app.isPaymentComplete()) {
                    paymentLabel.setText("Paid");
                    paymentLabel.getStyleClass().setAll("cell-label", "status-approved");
                } else {
                    paymentLabel.setText("Pending");
                    paymentLabel.getStyleClass().setAll("cell-label", "status-pending");
                }
                
                setGraphic(container);
            }
        };
    }
    
    /**
     * Show the loader animation
     */
    private void showLoader() {
        applicationContainer.setVisible(false);
        loaderContainer.setVisible(true);
        noApplicationsLabel.setVisible(false);
    }
    
    /**
     * Hide the loader animation
     */
    private void hideLoader() {
        loaderContainer.setVisible(false);
        
        // Show appropriate container based on applications list
        if (applications.isEmpty()) {
            noApplicationsLabel.setVisible(true);
            applicationContainer.setVisible(true);
        } else {
            noApplicationsLabel.setVisible(false);
            applicationContainer.setVisible(true);
        }
    }
    
    /**
     * Load all applications from the database
     * This method fetches all applications for admin view
     */
    private void loadAllApplications() {
        CompletableFuture.supplyAsync(() -> {
            List<Application> appList = new ArrayList<>();
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT * FROM applications ORDER BY application_date DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            appList.add(mapResultSetToApplication(rs));
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error fetching all applications", e);
            }
            
            return appList;
        }).thenAccept(appList -> {
            Platform.runLater(() -> {
                applications.clear();
                applications.addAll(appList);
                hideLoader();
                
                // Select the first application if available
                if (!applications.isEmpty()) {
                    applicationListView.getSelectionModel().selectFirst();
                }
                
                LOGGER.log(Level.INFO, "Loaded {0} applications", appList.size());
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                hideLoader();
                showErrorAlert("Failed to load applications", "Database error occurred.");
                LOGGER.log(Level.SEVERE, "Error loading applications: {0}", ex.getMessage());
            });
            return null;
        });
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
    
    /**
     * Refresh applications list
     */
    @FXML
    private void refreshApplications() {
        showLoader();
        loadAllApplications();
    }
    
    /**
     * Update the application details panel with the selected application
     */
    private void updateApplicationDetails(Application application) {
        if (application == null) {
            return;
        }
        
        // Update labels if they exist
        if (programLabel != null) programLabel.setText(application.getProgramName());
        if (nameLabel != null) nameLabel.setText(application.getApplicantName());
        if (emailLabel != null) emailLabel.setText(application.getEmail());
        if (phoneLabel != null) phoneLabel.setText(application.getPhoneNumber());
        
        // Format date of birth if available
        if (dobLabel != null) {
            if (application.getDateOfBirth() != null) {
                dobLabel.setText(application.getDateOfBirth().format(dateFormatter));
            } else {
                dobLabel.setText("-");
            }
        }
        
        if (genderLabel != null) genderLabel.setText(application.getGender());
        if (addressLabel != null) addressLabel.setText(application.getAddress());
        if (cityLabel != null) cityLabel.setText(application.getCity());
        if (postalCodeLabel != null) postalCodeLabel.setText(application.getPostalCode());
        
        // Academic information
        if (sscGpaLabel != null) sscGpaLabel.setText(String.format("%.2f", application.getSscGpa()));
        if (hscGpaLabel != null) hscGpaLabel.setText(String.format("%.2f", application.getHscGpa()));
        if (sscYearLabel != null) sscYearLabel.setText(application.getSscYear());
        if (hscYearLabel != null) hscYearLabel.setText(application.getHscYear());
        
        // Guardian information
        if (fatherNameLabel != null) fatherNameLabel.setText(application.getFatherName());
        if (fatherOccupationLabel != null) fatherOccupationLabel.setText(application.getFatherOccupation());
        if (motherNameLabel != null) motherNameLabel.setText(application.getMotherName());
        if (motherOccupationLabel != null) motherOccupationLabel.setText(application.getMotherOccupation());
        if (guardianPhoneLabel != null) guardianPhoneLabel.setText(application.getGuardianPhone());
        if (guardianEmailLabel != null) guardianEmailLabel.setText(application.getGuardianEmail());
        
        // Status information
        if (statusLabel != null) {
            statusLabel.setText(application.getStatus().getDisplayName());
            statusLabel.getStyleClass().setAll("detail-value", "status-label", application.getStatus().getStyleClass());
        }
        
        if (paymentStatusLabel != null) {
            paymentStatusLabel.setText(application.isPaymentComplete() ? "Paid" : "Pending");
            paymentStatusLabel.getStyleClass().setAll("detail-value", "payment-status-label", 
                application.isPaymentComplete() ? "status-approved" : "status-pending");
        }
        
        // Format application date
        if (applicationDateLabel != null) applicationDateLabel.setText(application.getApplicationDate().format(dateFormatter));
    }
    
    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Called when scene becomes visible or active
     */
    public void onSceneActive() {
        refreshUI();
        refreshApplications();
    }
    
    /**
     * Refresh the UI based on the current auth state
     */
    public void refreshUI() {
        AuthState state = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (state != null && state.isAuthenticated());
        boolean isAdmin = isAuthenticated && "admin".equalsIgnoreCase(state.getUser().getRole());
        
        // Update visibility based on auth state - Add null checks
        if (loginButtonContainer != null) {
            loginButtonContainer.setVisible(!isAuthenticated);
            loginButtonContainer.setManaged(!isAuthenticated);
        }
        
        if (profileButtonContainer != null) {
            profileButtonContainer.setVisible(isAuthenticated);
            profileButtonContainer.setManaged(isAuthenticated);
        }
        
        // If not authenticated or not admin, redirect to home
        if (!isAuthenticated || !isAdmin) {
            Platform.runLater(() -> {
                if (applicationListView != null && applicationListView.getScene() != null) {
                    NavigationUtil.navigateToHome(new ActionEvent(applicationListView, null));
                    showErrorAlert("Access Denied", "You must be logged in as an administrator to access this page.");
                }
            });
        }
    }
    
    /**
     * Cleanup resources before navigating away
     */
    private void cleanup() {
        if (applicationListView != null && applicationListView.getScene() != null && 
                applicationListView.getScene().getRoot() != null) {
            applicationListView.getScene().getRoot().setOpacity(1.0);
        }
    }
    
    /**
     * Navigate to the Home screen
     */
    @FXML
    private void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }
    
    /**
     * Navigate to the About screen
     */
    @FXML
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }
    
    /**
     * Navigate to the Admission screen
     */
    @FXML
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }
    
    /**
     * Navigate to the Exam Portal screen
     */
    @FXML
    private void navigateToExamPortal(ActionEvent event) {
        cleanup();
        // Not implemented yet
        System.out.println("Navigate to Exam Portal page (not implemented yet)");
    }
    
    /**
     * Navigate to the Contact screen
     */
    @FXML
    private void navigateToContact(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }
}