package com.ueadmission.about;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class About {

    public static Stage prepareAboutWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("/com.ueadmission/about/about.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("About UIU");
            Image icon = new Image(About.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setX(x);
            stage.setY(y);

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

    public static void openAboutWindow(double width, double height, double x, double y, boolean maximized) {
        Stage stage = prepareAboutWindow(width, height, x, y, maximized);
        if (stage != null) {
            stage.show();
            Parent root = stage.getScene().getRoot();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    public static Stage prepareMainWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("/com.ueadmission/main.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("UeAdmission - Home");
            Image icon = new Image(About.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setX(x);
            stage.setY(y);

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

    public static void openMainWindow(double width, double height, double x, double y, boolean maximized) {
        Stage stage = prepareMainWindow(width, height, x, y, maximized);
        if (stage != null) {
            stage.show();
            Parent root = stage.getScene().getRoot();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    // 🔵 ADDED: Prepare Admission Window
    public static Stage prepareAdmissionWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("/com.ueadmission/admission/admission.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("UIU Admission Portal");
            Image icon = new Image(About.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setX(x);
            stage.setY(y);

            if (maximized) {
                stage.setMaximized(true);
            }

            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load admission.fxml: " + e.getMessage());
            return null;
        }
    }

    // 🔵 ADDED: Open Admission Window
    public static void openAdmissionWindow(double width, double height, double x, double y, boolean maximized) {
        Stage stage = prepareAdmissionWindow(width, height, x, y, maximized);
        if (stage != null) {
            stage.show();
            Parent root = stage.getScene().getRoot();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

}

