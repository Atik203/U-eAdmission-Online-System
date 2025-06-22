package com.ueadmission.publishResult;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the publish result page
 */
public class PublishResultController {

    private static final Logger LOGGER = Logger.getLogger(PublishResultController.class.getName());

    // Navigation buttons
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
    private ProfileButton profileButton;

    // Global settings
    @FXML
    private GridPane globalSettingsGrid;

    @FXML
    private MFXTextField passMarkField;

    @FXML
    private MFXButton applyPassMarkButton;


    // Engineering & Technology school
    @FXML
    private Label engTotalStudents;

    @FXML
    private Label engCompletedExams;

    @FXML
    private Label engAverageScore;

    @FXML
    private Label engPassRate;

    @FXML
    private MFXTextField engPassMarkField;

    @FXML
    private Label engPublishStatus;

    @FXML
    private MFXButton engPublishButton;

    @FXML
    private Label engPercentLabel;

    // Business & Economics school
    @FXML
    private Label busTotalStudents;

    @FXML
    private Label busCompletedExams;

    @FXML
    private Label busAverageScore;

    @FXML
    private Label busPassRate;

    @FXML
    private MFXTextField busPassMarkField;

    @FXML
    private Label busPublishStatus;

    @FXML
    private MFXButton busPublishButton;

    @FXML
    private Label busPercentLabel;

    // Humanities & Social Sciences school
    @FXML
    private Label humTotalStudents;

    @FXML
    private Label humCompletedExams;

    @FXML
    private Label humAverageScore;

    @FXML
    private Label humPassRate;

    @FXML
    private MFXTextField humPassMarkField;

    @FXML
    private Label humPublishStatus;

    @FXML
    private MFXButton humPublishButton;

    @FXML
    private Label humPercentLabel;

    // Life Sciences school
    @FXML
    private Label lifeTotalStudents;

    @FXML
    private Label lifeCompletedExams;

    @FXML
    private Label lifeAverageScore;

    @FXML
    private Label lifePassRate;

    @FXML
    private MFXTextField lifePassMarkField;

    @FXML
    private Label lifePublishStatus;

    @FXML
    private MFXButton lifePublishButton;

    @FXML
    private Label lifePercentLabel;

    // Global action buttons
    @FXML
    private MFXButton publishAllButton;

    @FXML
    private MFXButton refreshButton;

    // Loader
    @FXML
    private StackPane loaderContainer;

    // Old fields (kept for compatibility)
    @FXML
    private MFXTextField examNameField;

    @FXML
    private MFXTextField examDateField;

    @FXML
    private MFXTextField departmentField;

    @FXML
    private MFXTextField semesterField;

    @FXML
    private MFXTextField resultFileField;

    @FXML
    private MFXTextField publicationDateField;

    @FXML
    private MFXTextField notesField;

    @FXML
    private MFXButton publishButton;

    @FXML
    private MFXButton clearButton;

    @FXML
    private MFXButton browseButton;

    @FXML
    private GridPane resultFormGrid;

    // Class variables
    private static final int selectedExamTypeId = 2; // Only using Actual Exam (id=2)
    private Map<Integer, Boolean> schoolPublishStatus = new HashMap<>();
    private Map<Integer, Double> schoolPassMarks = new HashMap<>();
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up navigation button handlers using NavigationUtil
        homeButton.setOnAction(e -> navigateToHome(e));
        aboutButton.setOnAction(e -> navigateToAbout(e));

        // Add authentication check before navigating to admission
        admissionButton.setOnAction(e -> {
            if (AuthStateManager.getInstance().isAuthenticated()) {
                navigateToAdmission(e);
            } else {
                // Redirect to login if not authenticated
                navigateToLogin(e);
            }
        });

        examPortalButton.setOnAction(e -> navigateToExamPortal(e));
        contactButton.setOnAction(e -> navigateToContact(e));

        // Set up global settings handlers
        applyPassMarkButton.setOnAction(e -> applyGlobalPassMark());

        // Set up school-specific publish buttons
        engPublishButton.setOnAction(e -> publishSchoolResults(1)); // Engineering id=1
        busPublishButton.setOnAction(e -> publishSchoolResults(2)); // Business id=2
        humPublishButton.setOnAction(e -> publishSchoolResults(3)); // Humanities id=3
        lifePublishButton.setOnAction(e -> publishSchoolResults(4)); // Life Sciences id=4

        // Set up global action buttons
        publishAllButton.setOnAction(e -> publishAllResults());
        refreshButton.setOnAction(e -> refreshData());

        // Initialize school pass mark fields with default values
        engPassMarkField.setText("40.00");
        busPassMarkField.setText("40.00");
        humPassMarkField.setText("40.00");
        lifePassMarkField.setText("40.00");

        // Set percent labels text
        engPercentLabel.setText("%");
        busPercentLabel.setText("%");
        humPercentLabel.setText("%");
        lifePercentLabel.setText("%");

        // Initialize school publish status maps
        for (int i = 1; i <= 4; i++) {
            schoolPublishStatus.put(i, false);
            schoolPassMarks.put(i, 40.00);
        }

        // Set up old action buttons (kept for compatibility)
        if (publishButton != null) publishButton.setOnAction(e -> handlePublishResult());
        if (clearButton != null) clearButton.setOnAction(e -> handleClearForm());
        if (browseButton != null) browseButton.setOnAction(e -> handleBrowseFile());

        // Hide old form grid if it exists
        if (resultFormGrid != null) {
            resultFormGrid.setVisible(false);
            resultFormGrid.setManaged(false);
        }

        // Show loader and load data
        showLoader();
        loadExamData();
    }

    /**
     * Show the loader animation
     */
    private void showLoader() {
        loaderContainer.setVisible(true);
        loaderContainer.setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loaderContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        LOGGER.info("Showing loader animation");
    }

    /**
     * Hide the loader spinner with animation
     */
    private void hideLoader() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loaderContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            loaderContainer.setVisible(false);
            loaderContainer.setManaged(false);
        });
        fadeOut.play();
    }

    /**
     * Simulate loading data with a delay
     */
    private void simulateDataLoading() {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate a network delay
                Thread.sleep(1000);

                // Load data on the JavaFX Application Thread
                Platform.runLater(() -> {
                    initializeFormFields();
                    hideLoader();
                });
            } catch (InterruptedException e) {
                LOGGER.warning("Data loading simulation interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Initialize the form with default values
     */
    private void initializeForm() {
        // Hide the form initially
        resultFormGrid.setVisible(false);
        resultFormGrid.setManaged(false);

        // Use simulateDataLoading to initialize form with a delay and animations
        simulateDataLoading();

        LOGGER.info("Started initializing form with animation");
    }

    /**
     * Initialize form fields with default values
     */
    private void initializeFormFields() {
        // Set default values or clear fields
        examNameField.clear();
        examDateField.clear();
        departmentField.clear();
        semesterField.clear();
        resultFileField.clear();
        publicationDateField.clear();
        notesField.clear();

        // Show form after initialization
        resultFormGrid.setVisible(true);
        resultFormGrid.setManaged(true);
    }

    /**
     * Handle the publish result button click
     */
    private void handlePublishResult() {
        // This is a placeholder for the publish result functionality
        // In a real implementation, this would publish the result to a database or file system
        PublishResult.showPublishSuccess();
    }

    /**
     * Handle the clear form button click
     */
    private void handleClearForm() {
        // Clear all form fields
        examNameField.clear();
        examDateField.clear();
        departmentField.clear();
        semesterField.clear();
        resultFileField.clear();
        publicationDateField.clear();
        notesField.clear();
    }

    /**
     * Handle the browse file button click
     */
    private void handleBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Result File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Get the stage from any control
        Stage stage = (Stage) browseButton.getScene().getWindow();

        // Show open file dialog
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            resultFileField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Called when scene becomes visible or active
     * This method is called by NavigationUtil when scene changes
     */
    public void onSceneActive() {
        LOGGER.info("Publish Result scene became active");
        refreshUI();
    }

    /**
     * Refresh the UI with current auth state
     */
    public void refreshUI() {
        AuthState authState = AuthStateManager.getInstance().getState();
        if (authState != null && authState.isAuthenticated()) {
            // Check if user is admin
            User user = authState.getUser();
            if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
                // Load exam data if user is admin
                loadExamData();
            } else {
                LOGGER.warning("User is not an admin, redirecting to home");
                // If user is not an admin, redirect to home
                Platform.runLater(() -> {
                    if (homeButton.getScene() != null) {
                        NavigationUtil.navigateToHome(new ActionEvent(homeButton, null));
                    }
                });
            }
        } else {
            LOGGER.warning("User not authenticated, redirecting to login");
            // If somehow we got to the publish result page without authentication, redirect to login
            Platform.runLater(() -> {
                if (homeButton.getScene() != null) {
                    NavigationUtil.navigateToLogin(new ActionEvent(homeButton, null));
                }
            });
        }
    }

    /**
     * Load exam data from the database
     */
    private void loadExamData() {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate network delay
                Thread.sleep(1000);

                // Load data on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        // Load publish status and pass marks for each school
                        loadSchoolPublishStatus();

                        // Load analytics data for each school
                        loadSchoolAnalytics();

                        // Update UI with loaded data
                        updateSchoolUI();

                        // Hide loader
                        hideLoader();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error loading exam data", e);
                        PublishResult.showPublishError("Failed to load exam data: " + e.getMessage());
                        hideLoader();
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.warning("Data loading interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Load publish status and pass marks for each school
     */
    private void loadSchoolPublishStatus() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT school_id, is_result_published, pass_mark FROM question_papers " +
                         "WHERE exam_type_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedExamTypeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int schoolId = rs.getInt("school_id");
                        boolean isPublished = rs.getBoolean("is_result_published");
                        double passMark = rs.getDouble("pass_mark");

                        schoolPublishStatus.put(schoolId, isPublished);
                        schoolPassMarks.put(schoolId, passMark);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading school publish status", e);
            throw new RuntimeException("Failed to load school publish status", e);
        }
    }

    /**
     * Load analytics data for each school
     */
    private void loadSchoolAnalytics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            for (int schoolId = 1; schoolId <= 4; schoolId++) {
                loadSchoolAnalytics(conn, schoolId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading school analytics", e);
            throw new RuntimeException("Failed to load school analytics", e);
        }
    }

    /**
     * Load analytics data for a specific school
     */
    private void loadSchoolAnalytics(Connection conn, int schoolId) throws SQLException {
        // Get total students who took the exam
        String totalStudentsSql = 
            "SELECT COUNT(DISTINCT es.student_id) as total_students " +
            "FROM exam_sessions es " +
            "JOIN question_papers qp ON es.question_paper_id = qp.id " +
            "WHERE qp.school_id = ? AND qp.exam_type_id = ?";

        // Get completed exams
        String completedExamsSql = 
            "SELECT COUNT(*) as completed_exams " +
            "FROM exam_sessions es " +
            "JOIN question_papers qp ON es.question_paper_id = qp.id " +
            "WHERE qp.school_id = ? AND qp.exam_type_id = ? AND es.status = 'completed'";

        // Get average score
        String averageScoreSql = 
            "SELECT AVG(es.score / es.max_score * 100) as average_score " +
            "FROM exam_sessions es " +
            "JOIN question_papers qp ON es.question_paper_id = qp.id " +
            "WHERE qp.school_id = ? AND qp.exam_type_id = ? AND es.status = 'completed'";

        // Get pass rate
        String passRateSql = 
            "SELECT COUNT(*) as passed_students " +
            "FROM exam_sessions es " +
            "JOIN question_papers qp ON es.question_paper_id = qp.id " +
            "WHERE qp.school_id = ? AND qp.exam_type_id = ? AND es.status = 'completed' " +
            "AND (es.score / es.max_score * 100) >= ?";

        int totalStudents = 0;
        int completedExams = 0;
        double averageScore = 0.0;
        int passedStudents = 0;

        // Get total students
        try (PreparedStatement stmt = conn.prepareStatement(totalStudentsSql)) {
            stmt.setInt(1, schoolId);
            stmt.setInt(2, selectedExamTypeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalStudents = rs.getInt("total_students");
                }
            }
        }

        // Get completed exams
        try (PreparedStatement stmt = conn.prepareStatement(completedExamsSql)) {
            stmt.setInt(1, schoolId);
            stmt.setInt(2, selectedExamTypeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    completedExams = rs.getInt("completed_exams");
                }
            }
        }

        // Get average score
        try (PreparedStatement stmt = conn.prepareStatement(averageScoreSql)) {
            stmt.setInt(1, schoolId);
            stmt.setInt(2, selectedExamTypeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    averageScore = rs.getDouble("average_score");
                    if (rs.wasNull()) {
                        averageScore = 0.0;
                    }
                }
            }
        }

        // Get pass rate
        try (PreparedStatement stmt = conn.prepareStatement(passRateSql)) {
            stmt.setInt(1, schoolId);
            stmt.setInt(2, selectedExamTypeId);
            stmt.setDouble(3, schoolPassMarks.getOrDefault(schoolId, 40.0));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    passedStudents = rs.getInt("passed_students");
                }
            }
        }

        // Calculate pass rate
        double passRate = (completedExams > 0) ? ((double) passedStudents / completedExams) * 100 : 0.0;

        // Store analytics data
        switch (schoolId) {
            case 1: // Engineering
                engTotalStudents.setText(String.valueOf(totalStudents));
                engCompletedExams.setText(String.valueOf(completedExams));
                engAverageScore.setText(decimalFormat.format(averageScore));
                engPassRate.setText(decimalFormat.format(passRate) + "%");
                break;
            case 2: // Business
                busTotalStudents.setText(String.valueOf(totalStudents));
                busCompletedExams.setText(String.valueOf(completedExams));
                busAverageScore.setText(decimalFormat.format(averageScore));
                busPassRate.setText(decimalFormat.format(passRate) + "%");
                break;
            case 3: // Humanities
                humTotalStudents.setText(String.valueOf(totalStudents));
                humCompletedExams.setText(String.valueOf(completedExams));
                humAverageScore.setText(decimalFormat.format(averageScore));
                humPassRate.setText(decimalFormat.format(passRate) + "%");
                break;
            case 4: // Life Sciences
                lifeTotalStudents.setText(String.valueOf(totalStudents));
                lifeCompletedExams.setText(String.valueOf(completedExams));
                lifeAverageScore.setText(decimalFormat.format(averageScore));
                lifePassRate.setText(decimalFormat.format(passRate) + "%");
                break;
        }
    }

    /**
     * Update the UI with the loaded data
     */
    private void updateSchoolUI() {
        // Update pass mark fields
        engPassMarkField.setText(decimalFormat.format(schoolPassMarks.getOrDefault(1, 40.0)));
        busPassMarkField.setText(decimalFormat.format(schoolPassMarks.getOrDefault(2, 40.0)));
        humPassMarkField.setText(decimalFormat.format(schoolPassMarks.getOrDefault(3, 40.0)));
        lifePassMarkField.setText(decimalFormat.format(schoolPassMarks.getOrDefault(4, 40.0)));

        // Update publish status labels
        updatePublishStatus(1, engPublishStatus);
        updatePublishStatus(2, busPublishStatus);
        updatePublishStatus(3, humPublishStatus);
        updatePublishStatus(4, lifePublishStatus);

        // Update global pass mark field
        passMarkField.setText("40.00");
    }

    /**
     * Update the publish status label for a school
     */
    private void updatePublishStatus(int schoolId, Label statusLabel) {
        boolean isPublished = schoolPublishStatus.getOrDefault(schoolId, false);
        if (isPublished) {
            statusLabel.setText("Published");
            statusLabel.getStyleClass().remove("status-not-published");
            statusLabel.getStyleClass().add("status-published");
        } else {
            statusLabel.setText("Not Published");
            statusLabel.getStyleClass().remove("status-published");
            statusLabel.getStyleClass().add("status-not-published");
        }
    }

    /**
     * Apply the global pass mark to all schools
     */
    private void applyGlobalPassMark() {
        try {
            double passMarkValue = Double.parseDouble(passMarkField.getText());
            if (passMarkValue < 0 || passMarkValue > 100) {
                PublishResult.showPublishError("Pass mark must be between 0 and 100");
                return;
            }

            // Update all school pass mark fields
            engPassMarkField.setText(decimalFormat.format(passMarkValue));
            busPassMarkField.setText(decimalFormat.format(passMarkValue));
            humPassMarkField.setText(decimalFormat.format(passMarkValue));
            lifePassMarkField.setText(decimalFormat.format(passMarkValue));

            PublishResult.showPublishSuccess();
        } catch (NumberFormatException e) {
            PublishResult.showPublishError("Invalid pass mark value");
        }
    }


    /**
     * Refresh the data from the database
     */
    private void refreshData() {
        showLoader();
        loadExamData();
    }

    /**
     * Publish results for a specific school
     */
    private void publishSchoolResults(int schoolId) {
        try {
            // Get the pass mark for this school
            double passMarkValue = 40.0;
            switch (schoolId) {
                case 1: // Engineering
                    passMarkValue = Double.parseDouble(engPassMarkField.getText());
                    break;
                case 2: // Business
                    passMarkValue = Double.parseDouble(busPassMarkField.getText());
                    break;
                case 3: // Humanities
                    passMarkValue = Double.parseDouble(humPassMarkField.getText());
                    break;
                case 4: // Life Sciences
                    passMarkValue = Double.parseDouble(lifePassMarkField.getText());
                    break;
            }

            // Validate pass mark
            if (passMarkValue < 0 || passMarkValue > 100) {
                PublishResult.showPublishError("Pass mark must be between 0 and 100");
                return;
            }

            // Update the database
            updateSchoolPublishStatus(schoolId, true, passMarkValue);

            // Update the UI
            schoolPublishStatus.put(schoolId, true);
            schoolPassMarks.put(schoolId, passMarkValue);
            updateSchoolUI();

            // Show success message
            PublishResult.showPublishSuccess();
        } catch (NumberFormatException e) {
            PublishResult.showPublishError("Invalid pass mark value");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing results for school " + schoolId, e);
            PublishResult.showPublishError("Failed to publish results: " + e.getMessage());
        }
    }

    /**
     * Update the publish status and pass mark for a school in the database
     */
    private void updateSchoolPublishStatus(int schoolId, boolean isPublished, double passMark) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE question_papers SET is_result_published = ?, pass_mark = ? " +
                         "WHERE school_id = ? AND exam_type_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, isPublished);
                stmt.setDouble(2, passMark);
                stmt.setInt(3, schoolId);
                stmt.setInt(4, selectedExamTypeId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No rows updated, question paper may not exist for this school and exam type");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating school publish status", e);
            throw new RuntimeException("Failed to update school publish status", e);
        }
    }

    /**
     * Publish results for all schools
     */
    private void publishAllResults() {
        try {
            // Get pass marks for all schools
            double engPassMark = Double.parseDouble(engPassMarkField.getText());
            double busPassMark = Double.parseDouble(busPassMarkField.getText());
            double humPassMark = Double.parseDouble(humPassMarkField.getText());
            double lifePassMark = Double.parseDouble(lifePassMarkField.getText());

            // Validate pass marks
            if (engPassMark < 0 || engPassMark > 100 ||
                busPassMark < 0 || busPassMark > 100 ||
                humPassMark < 0 || humPassMark > 100 ||
                lifePassMark < 0 || lifePassMark > 100) {
                PublishResult.showPublishError("Pass marks must be between 0 and 100");
                return;
            }

            // Update the database for each school
            updateSchoolPublishStatus(1, true, engPassMark);
            updateSchoolPublishStatus(2, true, busPassMark);
            updateSchoolPublishStatus(3, true, humPassMark);
            updateSchoolPublishStatus(4, true, lifePassMark);

            // Update the UI
            schoolPublishStatus.put(1, true);
            schoolPublishStatus.put(2, true);
            schoolPublishStatus.put(3, true);
            schoolPublishStatus.put(4, true);

            schoolPassMarks.put(1, engPassMark);
            schoolPassMarks.put(2, busPassMark);
            schoolPassMarks.put(3, humPassMark);
            schoolPassMarks.put(4, lifePassMark);

            updateSchoolUI();

            // Show success message
            PublishResult.showPublishSuccess();
        } catch (NumberFormatException e) {
            PublishResult.showPublishError("Invalid pass mark value");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing results for all schools", e);
            PublishResult.showPublishError("Failed to publish results: " + e.getMessage());
        }
    }

    /**
     * Cleanup resources before navigating away
     */
    private void cleanup() {
        LOGGER.info("Cleaning up PublishResultController before navigation");
        // Reset opacity on the scene root if available
        if (homeButton != null && homeButton.getScene() != null && 
                homeButton.getScene().getRoot() != null) {
            homeButton.getScene().getRoot().setOpacity(1.0);
        }
    }

    /**
     * Navigates to the Home screen
     */
    private void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the About screen
     */
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigates to the Admission screen
     */
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigates to the Exam Portal screen
     */
    private void navigateToExamPortal(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Navigates to the Contact screen
     */
    private void navigateToContact(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }

    /**
     * Redirects to the Login screen
     */
    private void navigateToLogin(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }

    /**
     * Navigates to the Profile screen
     */
    private void navigateToProfile(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToProfile(event);
    }
}
