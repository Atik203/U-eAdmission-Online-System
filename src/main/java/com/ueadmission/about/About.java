package com.ueadmission.about;

import com.ueadmission.Main;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class About {
    
    /**
     * Prepares the About UIU window but doesn't show it yet
     * @param width The width of the current window
     * @param height The height of the current window
     * @param x The x position of the current window
     * @param y The y position of the current window
     * @param maximized Whether the current window is maximized
     * @return The prepared Stage that can be shown when ready
     */
    public static Stage prepareAboutWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("/com.ueadmission/about/about.fxml"));
            Parent root = loader.load();
            
            // Apply a subtle fade effect to the root node
            root.setOpacity(0.0);
            
            Stage stage = new Stage();
            stage.setTitle("About UIU");
            
            // Set the icon
            Image icon = new Image(About.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
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
            
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load about.fxml: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Opens the About UIU window
     * @param width The width of the current window
     * @param height The height of the current window
     * @param x The x position of the current window
     * @param y The y position of the current window
     * @param maximized Whether the current window is maximized
     */
    public static void openAboutWindow(double width, double height, double x, double y, boolean maximized) {
        Stage stage = prepareAboutWindow(width, height, x, y, maximized);
        if (stage != null) {
            stage.show();
            
            // Apply fade-in animation
            Parent root = stage.getScene().getRoot();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
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
            FXMLLoader loader = new FXMLLoader(About.class.getResource("/com.ueadmission/main.fxml"));
            Parent root = loader.load();
            
            // Apply a subtle fade effect to the root node
            root.setOpacity(0.0);
            
            Stage stage = new Stage();
            stage.setTitle("UeAdmission - Home");
            
            // Set the icon
            Image icon = new Image(About.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
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
            
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load main.fxml: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Opens the main application window with smooth transition
     * @param width The width of the current window
     * @param height The height of the current window
     * @param x The x position of the current window
     * @param y The y position of the current window
     * @param maximized Whether the current window is maximized
     */
    public static void openMainWindow(double width, double height, double x, double y, boolean maximized) {
        Stage stage = prepareMainWindow(width, height, x, y, maximized);
        if (stage != null) {
            stage.show();
            
            // Apply fade-in animation
            Parent root = stage.getScene().getRoot();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

   
}
