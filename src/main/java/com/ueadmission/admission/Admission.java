package com.ueadmission.admission;

import com.ueadmission.about.About;
import javafx.animation.FadeTransition;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Admission {

    public static Stage prepareAdmissionWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("/com.ueadmission/admission/admission.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("Admission");
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

    public static void openAdmissionWindow(double width, double height, double x, double y, boolean maximized) {
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


    // ✅ Used by MainController
    @Nullable
    public static Stage prepareAboutWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(Admission.class.getResource("/com.ueadmission/admission/admission.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("Admission");
            Image icon = new Image(Admission.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
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

    // 🔵 OPTIONAL: Used for launching full admission UI
    public static void show(Stage stage, HostServices hostServices) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.getStyleClass().add("root");

        Label title = new Label("UIU Undergraduate Programs");
        title.getStyleClass().add("hero-title");
        mainContent.getChildren().add(title);

        mainContent.getChildren().add(createSchoolSection("School of Business and Economics (SoBE)", new String[][]{
                {"BBA", "https://www.uiu.ac.bd/program/bba/"},
                {"BBA in AIS", "https://www.uiu.ac.bd/program/bba-in-ais/"},
                {"Economics", "https://www.uiu.ac.bd/program/bsc-in-economics/"}
        }, hostServices));

        mainContent.getChildren().add(createSchoolSection("School of Science and Engineering (SoSE)", new String[][]{
                {"Civil Engineering", "https://www.uiu.ac.bd/program/bsc-in-civil-engineering/"},
                {"CSE", "https://www.uiu.ac.bd/program/bsc-in-cse/"},
                {"Data Science", "https://www.uiu.ac.bd/program/bsc-in-data-science/"},
                {"EEE", "https://www.uiu.ac.bd/program/bsc-in-eee/"}
        }, hostServices));

        mainContent.getChildren().add(createSchoolSection("School of Humanities and Social Sciences (SoHSS)", new String[][]{
                {"EDS", "https://www.uiu.ac.bd/program/bss-in-environment-and-development-studies/"},
                {"MSJ", "https://www.uiu.ac.bd/program/bss-in-msj/"},
                {"English", "https://www.uiu.ac.bd/program/ba-in-english/"}
        }, hostServices));

        mainContent.getChildren().add(createSchoolSection("School of Life Sciences (SoLS)", new String[][]{
                {"Pharmacy", "https://www.uiu.ac.bd/program/bachelor-of-pharmacy/"},
                {"Biotech & Genetic Engineering", "https://www.uiu.ac.bd/program/biotechnology-genetic-engineering/"}
        }, hostServices));

        Button applyNow = new Button("Apply Now");
        applyNow.getStyleClass().add("uiu-button-primary");
        applyNow.setOnAction(e -> hostServices.showDocument("https://admission.uiu.ac.bd/"));

        HBox applyBtnBox = new HBox(applyNow);
        applyBtnBox.setPadding(new Insets(30, 0, 30, 0));
        applyBtnBox.setStyle("-fx-alignment: center;");
        mainContent.getChildren().add(applyBtnBox);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 1000, 700);
        scene.getStylesheets().add(Admission.class.getResource("/css/uiu-theme.css").toExternalForm());

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setTitle("UIU Admission Portal");
        stage.show();
    }

    private static VBox createSchoolSection(String schoolName, String[][] departments, HostServices hostServices) {
        VBox section = new VBox(10);
        Label schoolLabel = new Label(schoolName);
        schoolLabel.getStyleClass().add("section-title");
        section.getChildren().add(schoolLabel);

        FlowPane flowPane = new FlowPane(10, 10);
        for (String[] dept : departments) {
            Button deptButton = new Button(dept[0]);
            deptButton.getStyleClass().add("uiu-button-outline");
            deptButton.setOnAction(e -> hostServices.showDocument(dept[1]));
            flowPane.getChildren().add(deptButton);
        }

        section.getChildren().add(flowPane);
        return section;
    }

}
