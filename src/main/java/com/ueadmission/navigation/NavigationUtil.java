package com.ueadmission.navigation;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.context.ApplicationContext;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for navigating between windows while preserving auth state
 */
public class NavigationUtil {
    private static final Logger LOGGER = Logger.getLogger(NavigationUtil.class.getName());
    
    /**
     * Navigate to a new window, preserving auth state and window properties
     * @param currentStage The current stage
     * @param fxmlPath The path to the FXML file for the new window
     * @param title The title for the new window
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToWindow(Stage currentStage, String fxmlPath, String title) {
        try {
            // Save current window properties
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Load new scene
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Apply the same dimensions
            Scene scene = new Scene(root, width, height);
            
            // Create a new stage
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle(title);
            
            // Apply same position and size
            newStage.setX(x);
            newStage.setY(y);
            newStage.setMaximized(maximized);
            
            // Apply smooth transition
            applyTransition(currentStage, newStage);
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to new window: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Apply a smooth transition between stages
     * @param oldStage The stage to close
     * @param newStage The stage to show
     */
    private static void applyTransition(Stage oldStage, Stage newStage) {
        // Set new stage opacity to 0 initially
        newStage.setOpacity(0);
        newStage.show();
        
        // Fade-in animation for new stage
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newStage.getScene().getRoot());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Fade-out animation for old stage
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), oldStage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        // Close old stage when fade-out completes
        fadeOut.setOnFinished(e -> oldStage.close());
        
        // Play animations
        fadeIn.play();
        fadeOut.play();
    }
}
