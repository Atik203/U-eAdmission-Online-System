package com.ueadmission.MockTest;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class MockTest {
    private static final Logger LOGGER = Logger.getLogger(MockTest.class.getName());

    @FXML
    public void openMockTestPage(ActionEvent event) {
        try {
            // Get current stage properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Prepare the Mock Test window
            Stage mockTestStage = new Stage();

            // Load FXML with proper path
            URL fxmlUrl = getClass().getResource("/com.ueadmission/mock_test/mockTest.fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found at: /com.ueadmission/mock_test/mockTest.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Set up the new stage
            mockTestStage.setScene(new Scene(root));
            mockTestStage.setTitle("UIU Admission Mock Test");
            mockTestStage.setWidth(width);
            mockTestStage.setHeight(height);
            mockTestStage.setX(x);
            mockTestStage.setY(y);
            mockTestStage.setMaximized(maximized);

            // Store the loader in the scene for later access
            mockTestStage.getScene().setUserData(loader);

            // Make the new stage ready but not visible yet
            mockTestStage.setOpacity(0.0);
            mockTestStage.show();

            // Use fade transitions for smooth window switching
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mockTestStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                mockTestStage.setOpacity(1.0);
                fadeIn.play();
                fadeIn.setOnFinished(f -> currentStage.close());
            });

            fadeOut.play();

        } catch (Exception e) {
            LOGGER.severe("Failed to open Mock Test Page: " + e.getMessage());
            e.printStackTrace();

            // Simple fallback navigation
            try {
                URL fallbackUrl = getClass().getResource("/com.ueadmission/mock_test/mockTest.fxml");
                if (fallbackUrl == null) {
                    LOGGER.severe("Fallback FXML file not found either");
                    return;
                }

                Parent root = FXMLLoader.load(fallbackUrl);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("UIU Admission Mock Test");
                stage.show();
                ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
            } catch (IOException ex) {
                LOGGER.severe("Fallback navigation failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}