package com.ueadmission.application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.BaseController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Application page entry point
 */
public class Application {
    
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    
    /**
     * Prepare the Application window with consistent dimensions
     * 
     * @param width The width to use
     * @param height The height to use
     * @param x The x position
     * @param y The y position
     * @param maximized Whether the window should be maximized
     * @return The prepared stage or null if an error occurred
     */
    public static Stage prepareApplicationWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("/com.ueadmission/application/application.fxml"));
            
            // Create controller factory to ensure singleton controllers
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> controllerClass) {
                    if (controllerClass == ApplicationController.class) {
                        return new ApplicationController();
                    }
                    return BaseController.getControllerInstance(controllerClass);
                }
            });
            
            Parent root = loader.load();
            
            // Set up the stage
            Stage stage = new Stage();
            stage.setTitle("My Applications - UeAdmission");
            
            Scene scene = new Scene(root, width, height);
            scene.setUserData(loader);
            
            stage.setScene(scene);
            stage.setX(x);
            stage.setY(y);
            stage.setMaximized(maximized);
            
            LOGGER.info("Application window prepared successfully");
            return stage;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Application FXML", e);
            return null;
        }
    }
    
    /**
     * Show an application payment success message
     */
    public static void showPaymentSuccess() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText("Payment Processed Successfully");
            alert.setContentText("Your payment has been processed. Thank you!");
            alert.showAndWait();
        });
    }
    
    /**
     * Show a payment error message
     */
    public static void showPaymentError() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Payment Error");
            alert.setHeaderText("Payment Processing Failed");
            alert.setContentText("There was an error processing your payment. Please try again later.");
            alert.showAndWait();
        });
    }
}
