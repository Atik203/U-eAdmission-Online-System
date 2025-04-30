package com.ueadmission;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.context.ApplicationContext;
import com.ueadmission.db.DatabaseManager;
import com.ueadmission.utils.MFXNotifications;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) {


        try {
            // Try to initialize database, but continue even if it fails
            try {
                boolean dbInitialized = DatabaseManager.initializeDatabase();
                if (dbInitialized) {
                    LOGGER.info("Database initialized successfully");
                } else {
                    LOGGER.warning("Database initialization failed but continuing");
                }
            } catch (Exception e) {
                // Log but continue without database for UI testing
                LOGGER.log(Level.WARNING, "Database connection failed, continuing without database", e);
            }

            // Load the main FXML directly
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com.ueadmission/main.fxml"));
            Parent root = fxmlLoader.load();

            // Create the scene
            Scene scene = new Scene(root, 1280, 768);

            // Set application icon
            Image icon = new Image(getClass().getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            primaryStage.getIcons().add(icon);

            // Set the main stage in NavigationManager for future navigation
            com.ueadmission.navigation.NavigationManager.setMainStage(primaryStage);

            // Configure the stage
            primaryStage.setMaximized(true);
            primaryStage.setTitle("UeAdmission - Home");
            primaryStage.setScene(scene);

            // Initialize notification center before showing the stage
            MFXNotifications.initialize(primaryStage);

            // Add notification pane if root is a Pane
            if (root instanceof Pane) {
                MFXNotifications.addToPane((Pane) root);
            }

            // Initialize ApplicationContext
            ApplicationContext.getInstance().setInitialized(true);

            // Store the main stage in a static variable for easy access
            ApplicationContext context = ApplicationContext.getInstance();
            try {
                java.lang.reflect.Field stageField = ApplicationContext.class.getDeclaredField("currentStage");
                stageField.setAccessible(true);
                stageField.set(context, primaryStage);
                LOGGER.info("Stored primary stage in ApplicationContext");
            } catch (Exception e) {
                LOGGER.warning("Could not store stage in ApplicationContext: " + e.getMessage());
            }

            // Check if user is already authenticated
            AuthStateManager authManager = AuthStateManager.getInstance();

            // Actively try to restore session first
            boolean sessionRestored = authManager.restoreSession();

            // Force notification to update UI components
            authManager.notifySubscribers();

            if (authManager.isAuthenticated()) {
                // User is already logged in
                AuthState authState = authManager.getState();
                LOGGER.info("User already authenticated: " +
                        authState.getUser().getEmail() + " (Role: " +
                        authState.getUser().getRole() + ")");

                // Show a welcome back notification
                MFXNotifications.showInfo("Welcome Back",
                        "Welcome back, " + authState.getUser().getFirstName() + "!");

                // Log restoration method
                if (sessionRestored) {
                    LOGGER.info("Session was restored from persistent storage");
                } else {
                    LOGGER.info("Session was already available in memory");
                }

                // Get controller and update UI directly if possible
                try {
                    Object controller = fxmlLoader.getController();
                    if (controller != null) {
                        System.out.println("Main controller class: " + controller.getClass().getName());

                        // Look for updateAuthUI method and call it
                        try {
                            java.lang.reflect.Method updateMethod =
                                    controller.getClass().getMethod("updateAuthUI", AuthState.class);
                            updateMethod.invoke(controller, authState);
                            System.out.println("Successfully called updateAuthUI on controller");
                        } catch (Exception e) {
                            System.err.println("Could not call updateAuthUI: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error getting controller: " + e.getMessage());
                }
            }

            // Pass HostServices to the controller
            MainController controller = fxmlLoader.getController();
            controller.setHostServices(getHostServices());

            // Show the stage
            primaryStage.show();
            LOGGER.info("Main window shown successfully");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + e.getMessage(), e);
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Failed to load application UI: " + e.getMessage(),
                    ButtonType.OK);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to Start Application");
            alert.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application: " + e.getMessage(), e);
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Application error: " + e.getMessage(),
                    ButtonType.OK);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to Start Application");
            alert.showAndWait();
        }
    }

    @Override
    public void stop() {
        try {
            // Cleanup resources when application closes
            com.ueadmission.db.DatabaseConnection.closeConnection();
            LOGGER.info("Application closed, resources cleaned up");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during application shutdown", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
