package com.ueadmission.auth;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Auth utility class that handles navigation between authentication-related screens
 */
public class Auth {
    
    /**
     * Prepares the Login window but doesn't show it yet
     * @param width The width of the current window
     * @param height The height of the current window
     * @param x The x position of the current window
     * @param y The y position of the current window
     * @param maximized Whether the current window is maximized
     * @return The prepared Stage that can be shown when ready
     */
    public static Stage prepareLoginWindow(double width, double height, double x, double y, boolean maximized) {
        try {
                // Create a loader without explicitly setting the controller
                // The controller is already defined in the FXML file
                FXMLLoader loader = new FXMLLoader(Auth.class.getResource("/com.ueadmission/auth/login.fxml"));
            Parent root = loader.load();
            
            // Apply a subtle fade effect to the root node
            root.setOpacity(0.0);
            
            Stage stage = new Stage();
            stage.setTitle("Login - UeAdmission");
            
            // Set the icon
            Image icon = new Image(Auth.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);
            
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            // Set the position to match the previous window
            stage.setX(x);
            stage.setY(y);
            
            // Handle maximized state
            if (maximized) {
                stage.setMaximized(true);
            }
            
            // Initialize notification center for this stage
            com.ueadmission.utils.MFXNotifications.initialize(stage);
            
            // Make sure the notification pane is added to the scene's root
            if (stage.getScene().getRoot() instanceof Pane) {
                com.ueadmission.utils.MFXNotifications.addToPane((Pane) stage.getScene().getRoot());
            }
            
            // Add window close handler to ensure proper logout when window is closed
            stage.setOnCloseRequest(event -> {
                System.out.println("Login window closing, checking for logout needs");
                com.ueadmission.auth.state.AuthStateManager authStateManager = 
                    com.ueadmission.auth.state.AuthStateManager.getInstance();
                if (authStateManager.isAuthenticated() && authStateManager.getState().getUser() != null) {
                    int userId = authStateManager.getState().getUser().getId();
                    com.ueadmission.auth.UserDAO.logoutUser(userId);
                    System.out.println("Logged out user ID: " + userId + " on login window close");
                }
            });
            
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load login.fxml: " + e.getMessage());
            return null;
        }
    }
        

    /**
     * Prepares the Registration window but doesn't show it yet
     * @param width The width of the current window
     * @param height The height of the current window
     * @param x The x position of the current window
     * @param y The y position of the current window
     * @param maximized Whether the current window is maximized
     * @return The prepared Stage that can be shown when ready
     */
    public static Stage prepareRegistrationWindow(double width, double height, double x, double y, boolean maximized) {
        try {
                // Create a loader without explicitly setting the controller
                // The controller should be defined in the FXML file
                FXMLLoader loader = new FXMLLoader(Auth.class.getResource("/com.ueadmission/auth/registration.fxml"));
            Parent root = loader.load();
            
            // Apply a subtle fade effect to the root node
            root.setOpacity(0.0);
            
            Stage stage = new Stage();
            stage.setTitle("Register - UeAdmission");
            
            // Set the icon
            Image icon = new Image(Auth.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);
            
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            // Set the position to match the previous window
            stage.setX(x);
            stage.setY(y);
            
            // Handle maximized state
            if (maximized) {
                stage.setMaximized(true);
            }
            
            // Initialize notification center for this stage
            com.ueadmission.utils.MFXNotifications.initialize(stage);
            
            // Add window close handler to ensure proper logout when window is closed
            stage.setOnCloseRequest(event -> {
                System.out.println("Registration window closing, checking for logout needs");
                com.ueadmission.auth.state.AuthStateManager authStateManager = 
                    com.ueadmission.auth.state.AuthStateManager.getInstance();
                if (authStateManager.isAuthenticated() && authStateManager.getState().getUser() != null) {
                    int userId = authStateManager.getState().getUser().getId();
                    com.ueadmission.auth.UserDAO.logoutUser(userId);
                    System.out.println("Logged out user ID: " + userId + " on registration window close");
                }
            });
                
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load registration.fxml: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Prepares the main application window but doesn't show it yet
     * @param width The width of the current window
     * @param height The height of the current window
     * @param x The x position of the current window
     * @param y The y position of the current window
     * @param maximized Whether the current window is maximized
     * @return The prepared Stage that can be shown when ready
     */
    public static Stage prepareMainWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(Auth.class.getResource("/com.ueadmission/main.fxml"));
            Parent root = loader.load();
            
            // Apply a subtle fade effect to the root node
            root.setOpacity(0.0);
            
            Stage stage = new Stage();
            stage.setTitle("UeAdmission - Home");
            
            // Set the icon
            Image icon = new Image(Auth.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);
            
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            // Set the position to match the previous window
            stage.setX(x);
            stage.setY(y);
            
            // Handle maximized state
            if (maximized) {
                stage.setMaximized(true);
            }
            
            // Initialize notification center for this stage
            com.ueadmission.utils.MFXNotifications.initialize(stage);
            
            // Add window close handler to ensure proper logout when window is closed
            stage.setOnCloseRequest(event -> {
                System.out.println("Main window closing, checking for logout needs");
                com.ueadmission.auth.state.AuthStateManager authStateManager = 
                    com.ueadmission.auth.state.AuthStateManager.getInstance();
                if (authStateManager.isAuthenticated() && authStateManager.getState().getUser() != null) {
                    int userId = authStateManager.getState().getUser().getId();
                    com.ueadmission.auth.UserDAO.logoutUser(userId);
                    System.out.println("Logged out user ID: " + userId + " on main window close");
                }
            });
            
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load main.fxml: " + e.getMessage());
            return null;
        }
    }
}
