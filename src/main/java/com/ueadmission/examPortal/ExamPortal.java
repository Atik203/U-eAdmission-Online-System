package com.ueadmission.examPortal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.utils.TransitionUtil;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Helper class for creating and displaying the Exam Portal window
 */
public class ExamPortal {
    
    private static final Logger LOGGER = Logger.getLogger(ExamPortal.class.getName());
    
    /**
     * Prepare the Exam Portal window
     * @param width Initial width for the window
     * @param height Initial height for the window
     * @param x Initial x position for the window
     * @param y Initial y position for the window
     * @param maximized Whether the window should be maximized
     * @return The prepared Stage
     */
    public static Stage prepareExamPortalWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(ExamPortal.class.getResource("/com.ueadmission/examPortal/exam-portal.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            ExamPortalController controller = loader.getController();
              // Create and configure the Stage
            Stage stage = new Stage();
            stage.setTitle("University Admission Exam Portal");
            
            // Set application icon
            try {
                Image icon = new Image(ExamPortal.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load application icon", e);
            }
            
            // Create and configure the Scene
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            // Set position and size
            stage.setX(x);
            stage.setY(y);
            
            // Apply maximized state if the previous window was maximized
            if (maximized) {
                stage.setMaximized(true);
            }
              // Apply a closing transition and clean up resources
            stage.setOnCloseRequest(event -> {
                controller.cleanup();
                TransitionUtil.applyFadeInTransition(stage.getScene().getRoot());
            });
            
            return stage;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Exam Portal", e);            return null;
        }
    }
    
    /**
     * Opens the Exam Portal window with a fade-in transition
     * @param width Initial width for the window
     * @param height Initial height for the window
     * @param x Initial x position for the window
     * @param y Initial y position for the window
     * @param maximized Whether the window should be maximized
     */
    public static void openExamPortalWindow(double width, double height, double x, double y, boolean maximized) {
        Stage stage = prepareExamPortalWindow(width, height, x, y, maximized);
        if (stage != null) {
            stage.show();
            Parent root = stage.getScene().getRoot();
            
            // Apply fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }
}
