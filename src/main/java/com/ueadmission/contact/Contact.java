package com.ueadmission.contact;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Contact page manager class
 * Responsible for preparing and showing the Contact screen
 */
public class Contact {
    
    /**
     * Prepares the Contact window with specified dimensions
     * 
     * @param width The width of the window
     * @param height The height of the window
     * @param x The x position of the window
     * @param y The y position of the window
     * @param maximized Whether the window should be maximized
     * @return The prepared Contact window Stage or null if an error occurred
     */
    public static Stage prepareContactWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            // Load the Contact FXML
            FXMLLoader loader = new FXMLLoader(Contact.class.getResource("/com.ueadmission/contact/contact.fxml"));
            Parent root = loader.load();
            
            // Create the scene
            Scene scene = new Scene(root, width, height);
            
            // Create and configure the stage
            Stage contactStage = new Stage();
            contactStage.setScene(scene);
            contactStage.setTitle("Contact - UeAdmission");
            
            // Set window properties
            contactStage.setX(x);
            contactStage.setY(y);
            contactStage.setMaximized(maximized);
            
            // Set application icon
            contactStage.getIcons().add(new Image(Contact.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png")));
            
            // Apply fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
            return contactStage;
        } catch (IOException e) {
            System.err.println("Error loading contact.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
