package com.ueadmission.publishResult;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.components.ProfileButton;
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

    @FXML
    private MFXButton publishButton;

    @FXML
    private MFXButton clearButton;

    @FXML
    private MFXButton browseButton;

    @FXML
    private StackPane loaderContainer;

    @FXML
    private MFXSpinner spinner;

    @FXML
    private GridPane resultFormGrid;

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
        publishButton.setOnAction(e -> handlePublishResult());
        clearButton.setOnAction(e -> handleClearForm());
        browseButton.setOnAction(e -> handleBrowseFile());

        // Make the form grid initially hidden until data is loaded
        resultFormGrid.setVisible(false);
        resultFormGrid.setManaged(false);

        // Show loader and initialize form
        showLoader();
        initializeForm();
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
            // Initialize form if authenticated
            initializeFormFields();
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