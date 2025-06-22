package com.ueadmission.result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the result page
 * Displays student examination results and admission status
 */
public class ResultController {

    private static final Logger LOGGER = Logger.getLogger(ResultController.class.getName());

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

    // Result containers
    @FXML
    private VBox noResultsContainer;

    @FXML
    private VBox resultsListContainer;

    // Action buttons
    @FXML
    private MFXButton refreshButton;

    @FXML
    private MFXButton contactAdmissionButton;

    // Loader
    @FXML
    private StackPane loaderContainer;

    // Class variables
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private User currentUser;
    private boolean hasResults = false;

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

        // Set up action buttons
        refreshButton.setOnAction(e -> refreshData());
        contactAdmissionButton.setOnAction(e -> navigateToContact(e));

        // Show loader and load data
        showLoader();
        loadUserData();
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
     * Load user data from authentication state
     */
    private void loadUserData() {
        AuthState authState = AuthStateManager.getInstance().getState();
        if (authState != null && authState.isAuthenticated()) {
            currentUser = authState.getUser();
            if (currentUser != null) {
                // Load results
                loadResults();
            } else {
                hideLoader();
                showNoResults("User information not available");
            }
        } else {
            hideLoader();
            showNoResults("Please log in to view your results");

            // Redirect to login after a delay
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        if (homeButton.getScene() != null) {
                            NavigationUtil.navigateToLogin(new ActionEvent(homeButton, null));
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    /**
     * Load results from the database
     */
    private void loadResults() {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate network delay
                Thread.sleep(1000);

                // Load data on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        if (currentUser == null) {
                            hideLoader();
                            showNoResults("User information not available");
                            return;
                        }

                        // Load results for the student using user ID
                        loadStudentResults(String.valueOf(currentUser.getId()));

                        // Hide loader
                        hideLoader();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error loading results", e);
                        hideLoader();
                        showNoResults("Failed to load results: " + e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.warning("Data loading interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Load results for a specific student
     */
    private void loadStudentResults(String studentId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to get exam results for the student with school name
            String sql = "SELECT es.*, qp.school_id, qp.pass_mark, qp.is_result_published, qp.title, " +
                         "s.name as school_name, et.name as exam_type_name " +
                         "FROM exam_sessions es " +
                         "JOIN question_papers qp ON es.question_paper_id = qp.id " +
                         "JOIN schools s ON qp.school_id = s.id " +
                         "JOIN exam_types et ON qp.exam_type_id = et.id " +
                         "WHERE es.student_id = ? AND es.status = 'completed' " +
                         "ORDER BY es.end_time DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean hasAnyResults = false;

                    // Clear any existing result panels
                    resultsListContainer.getChildren().clear();
                    // Only keep the first child (noResultsContainer)
                    if (resultsListContainer.getChildren().size() > 1) {
                        resultsListContainer.getChildren().subList(1, resultsListContainer.getChildren().size()).clear();
                    }

                    while (rs.next()) {
                        int schoolId = rs.getInt("school_id");
                        boolean isPublished = rs.getBoolean("is_result_published");
                        String schoolName = rs.getString("school_name");
                        String examTitle = rs.getString("title");

                        // Skip if results are not published
                        if (!isPublished) continue;

                        hasAnyResults = true;

                        // Process results for this school
                        processResult(rs, schoolId, schoolName, examTitle);
                    }

                    // Show or hide results containers
                    if (hasAnyResults) {
                        showResults();
                    } else {
                        showNoResults("No published results available yet");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading student results", e);
            showNoResults("Failed to load results: Database error");
        }
    }

    /**
     * Process result for any school
     */
    private void processResult(ResultSet rs, int schoolId, String schoolName, String examTitle) throws SQLException {
        // Get result data
        double score = rs.getDouble("score");
        double maxScore = rs.getDouble("max_score");
        double passMark = rs.getDouble("pass_mark");
        String completionTime = rs.getString("end_time");
        String examTypeName = rs.getString("exam_type_name");

        // Calculate percentage score
        double percentageScore = (score / maxScore) * 100;
        boolean passed = percentageScore >= passMark;

        // Create a new result panel for this school
        VBox resultPanel = createResultPanel(schoolName, examTitle, completionTime, percentageScore, passMark, passed);

        // Add the result panel to the results list container
        resultsListContainer.getChildren().add(resultPanel);
    }

    /**
     * Create a result panel for a school
     */
    private VBox createResultPanel(String schoolName, String examTitle, String examDate, double score, double passMark, boolean passed) {
        // Create the main panel
        VBox resultPanel = new VBox();
        resultPanel.getStyleClass().add("result-panel");
        resultPanel.setPadding(new Insets(20, 25, 20, 25));

        // Create the school title
        Label schoolTitleLabel = new Label(schoolName);
        schoolTitleLabel.getStyleClass().add("school-title");

        // Create the grid for result details
        GridPane resultGrid = new GridPane();
        resultGrid.getStyleClass().add("result-grid");
        resultGrid.setHgap(20);
        resultGrid.setVgap(10);

        // Add column constraints
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(150);
        col1.setPrefWidth(180);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        resultGrid.getColumnConstraints().addAll(col1, col2);

        // Add exam title row
        Label examTitleLabel = new Label("Exam Title:");
        examTitleLabel.getStyleClass().add("result-label");
        Label examTitleValue = new Label(examTitle);
        examTitleValue.getStyleClass().add("result-value");
        resultGrid.add(examTitleLabel, 0, 0);
        resultGrid.add(examTitleValue, 1, 0);

        // Add exam date row
        Label examDateLabel = new Label("Exam Date:");
        examDateLabel.getStyleClass().add("result-label");
        Label examDateValue = new Label(examDate.substring(0, 10)); // Format date better in a real app
        examDateValue.getStyleClass().add("result-value");
        resultGrid.add(examDateLabel, 0, 1);
        resultGrid.add(examDateValue, 1, 1);

        // Add score row
        Label scoreLabel = new Label("Your Score:");
        scoreLabel.getStyleClass().add("result-label");
        Label scoreValue = new Label(decimalFormat.format(score) + "%");
        scoreValue.getStyleClass().add("result-value");
        resultGrid.add(scoreLabel, 0, 2);
        resultGrid.add(scoreValue, 1, 2);

        // Add pass mark row
        Label passMarkLabel = new Label("Pass Mark:");
        passMarkLabel.getStyleClass().add("result-label");
        Label passMarkValue = new Label(decimalFormat.format(passMark) + "%");
        passMarkValue.getStyleClass().add("result-value");
        resultGrid.add(passMarkLabel, 0, 3);
        resultGrid.add(passMarkValue, 1, 3);

        // Add status row
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("result-label");
        Label statusValue = new Label(passed ? "PASSED" : "WAITING");
        statusValue.getStyleClass().add(passed ? "status-passed" : "status-waiting");
        resultGrid.add(statusLabel, 0, 4);

        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.getChildren().add(statusValue);
        resultGrid.add(statusBox, 1, 4);

        // Create message container based on status
        VBox messageContainer = new VBox();
        messageContainer.setAlignment(Pos.CENTER);

        if (passed) {
            // Congratulations message
            messageContainer.getStyleClass().add("congratulations-container");

            Label congratsTitle = new Label("Congratulations!");
            congratsTitle.getStyleClass().add("congratulations-title");

            Label congratsText = new Label("You have successfully passed the entrance examination. Please check your email for further instructions on the admission process.");
            congratsText.getStyleClass().add("congratulations-text");
            congratsText.setWrapText(true);

            messageContainer.getChildren().addAll(congratsTitle, congratsText);
        } else {
            // Waiting message
            messageContainer.getStyleClass().add("waiting-container");

            Label waitingTitle = new Label("You are on the waiting list");
            waitingTitle.getStyleClass().add("waiting-title");

            Label waitingText = new Label("Your application is being considered. Please check your email regularly for updates on your admission status.");
            waitingText.getStyleClass().add("waiting-text");
            waitingText.setWrapText(true);

            messageContainer.getChildren().addAll(waitingTitle, waitingText);
        }

        // Add all components to the panel
        resultPanel.getChildren().addAll(schoolTitleLabel, resultGrid, messageContainer);

        return resultPanel;
    }

    /**
     * Show results container and hide no results message
     */
    private void showResults() {
        hasResults = true;
        noResultsContainer.setVisible(false);
        noResultsContainer.setManaged(false);
        resultsListContainer.setVisible(true);
        resultsListContainer.setManaged(true);
    }

    /**
     * Show no results message and hide results container
     */
    private void showNoResults(String message) {
        hasResults = false;
        noResultsContainer.setVisible(true);
        noResultsContainer.setManaged(true);
        resultsListContainer.setVisible(false);
        resultsListContainer.setManaged(false);

        // Update message if provided
        if (message != null && !message.isEmpty()) {
            // Find the label in the container and update it
            noResultsContainer.getChildren().stream()
                .filter(node -> node instanceof Label && ((Label) node).getStyleClass().contains("no-results-text"))
                .findFirst()
                .ifPresent(label -> ((Label) label).setText(message));
        }
    }

    /**
     * Refresh the data from the database
     */
    private void refreshData() {
        showLoader();
        loadResults();
    }

    /**
     * Called when scene becomes visible or active
     * This method is called by NavigationUtil when scene changes
     */
    public void onSceneActive() {
        LOGGER.info("Result scene became active");
        refreshUI();
    }

    /**
     * Refresh the UI with current auth state
     */
    public void refreshUI() {
        loadUserData();
    }

    /**
     * Cleanup resources before navigating away
     */
    private void cleanup() {
        LOGGER.info("Cleaning up ResultController before navigation");
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
