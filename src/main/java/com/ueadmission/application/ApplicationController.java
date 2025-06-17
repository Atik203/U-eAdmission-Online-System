package com.ueadmission.application;

import static java.lang.Integer.parseInt;

import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.application.model.Application;
import com.ueadmission.application.model.ApplicationStatus;
import com.ueadmission.application.model.PaymentStatus;
import com.ueadmission.application.service.ApplicationService;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.payment.SSLCommerzPayment;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Controller for the Applications page
 */
public class ApplicationController {

    private static final Logger LOGGER = Logger.getLogger(ApplicationController.class.getName());

    @FXML
    private ListView<Application> applicationListView;

    @FXML
    private Label noApplicationsLabel;

    @FXML
    private StackPane loaderContainer;

    @FXML
    private VBox applicationContainer;

    @FXML
    private MFXButton homeButton;

    @FXML
    private MFXButton aboutButton;

    @FXML
    private MFXButton admissionButton;

    @FXML
    private MFXButton examPortalButton;

    @FXML
    private MFXButton contactButton;

    @FXML
    private HBox loginButtonContainer;

    @FXML
    private HBox profileButtonContainer;

    @FXML
    private MFXButton refreshButton;

    @FXML
    private MFXButton makePaymentButton;

    @FXML
    private MFXButton trackStatusButton;

    @FXML
    private MFXSpinner<?> spinner;

    // Application details labels - removing applicationDetailsAccordion reference
    @FXML private Label programLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label addressLabel;
    @FXML private Label cityLabel;
    @FXML private Label postalCodeLabel;
    @FXML private Label sscGpaLabel;
    @FXML private Label hscGpaLabel;
    @FXML private Label sscYearLabel;
    @FXML private Label hscYearLabel;
    @FXML private Label fatherNameLabel;
    @FXML private Label fatherOccupationLabel;
    @FXML private Label motherNameLabel;
    @FXML private Label motherOccupationLabel;
    @FXML private Label guardianPhoneLabel;
    @FXML private Label guardianEmailLabel;
    @FXML private Label statusLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Label applicationDateLabel;

    // Service for application operations
    private final ApplicationService applicationService = new ApplicationService();

    // Observable list to hold applications
    private final ObservableList<Application> applications = FXCollections.observableArrayList();

    // Currently selected application
    private final ObjectProperty<Application> selectedApplication = new SimpleObjectProperty<>();

    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up ListView with custom cell factory
        applicationListView.setItems(applications);
        applicationListView.setCellFactory(createCellFactory());

        // Add selection change listener to update details when a row is selected
        applicationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedApplication.set(newVal);
            updateApplicationDetails(newVal);
            updateActionButtons(newVal);
        });

        // Set up action buttons with null checks
        if (makePaymentButton != null) {
            makePaymentButton.setOnAction(e -> {
                Application selected = selectedApplication.get();
                if (selected != null) {
                    showPaymentDialog(selected);
                }
            });
        } else {
            LOGGER.warning("makePaymentButton is null in FXML. Button not found with required ID.");
        }

        if (trackStatusButton != null) {
            trackStatusButton.setOnAction(e -> {
                Application selected = selectedApplication.get();
                if (selected != null) {
                    trackApplicationStatus(selected);
                }
            });
        } else {
            LOGGER.warning("trackStatusButton is null in FXML. Button not found with required ID.");
        }

        // Set up navigation buttons with null checks
        if (homeButton != null) {
            homeButton.setOnAction(this::navigateToHome);
        }
        if (aboutButton != null) {
            aboutButton.setOnAction(this::navigateToAbout);
        }
        if (admissionButton != null) {
            admissionButton.setOnAction(this::navigateToAdmission);
        }        if (examPortalButton != null) {
            examPortalButton.setOnAction(this::navigateToExamPortal);
        }
        if (contactButton != null) {
            contactButton.setOnAction(this::navigateToContact);
        }
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> refreshApplications());
        }

        // Load applications data
        showLoader();
        loadApplications();

        // Update UI based on authentication state
        refreshUI();
    }

    /**
     * Create the cell factory for the application list view
     */
    private Callback<ListView<Application>, ListCell<Application>> createCellFactory() {
        return listView -> new ListCell<Application>() {
            private final HBox container = new HBox();
            private final Label nameLabel = new Label();
            private final Label emailLabel = new Label();
            private final Label programLabel = new Label();
            private final Label semesterLabel = new Label();
            private final Label yearLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label statusLabel = new Label();
            private final HBox paymentContainer = new HBox();
            private final Label paymentLabel = new Label();
            private final MFXButton paymentButton = new MFXButton("Make Payment");

            {
                // Configure the cell layout
                container.getStyleClass().add("application-list-item");
                container.setSpacing(10);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Configure the labels with specific column classes
                nameLabel.getStyleClass().addAll("cell-label", "name-column");
                emailLabel.getStyleClass().addAll("cell-label", "email-column");
                programLabel.getStyleClass().addAll("cell-label", "program-column");
                semesterLabel.getStyleClass().addAll("cell-label", "semester-column");
                yearLabel.getStyleClass().addAll("cell-label", "year-column");
                dateLabel.getStyleClass().addAll("cell-label", "date-column");
                statusLabel.getStyleClass().addAll("cell-label", "status-column");
                paymentContainer.getStyleClass().add("payment-column");
                paymentLabel.getStyleClass().add("cell-label");

                // Configure payment button
                paymentButton.getStyleClass().add("mfx-button-small");
                paymentButton.setStyle("-fx-background-color: #FA4506; -fx-text-fill: white;");
                paymentContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                paymentContainer.getChildren().add(paymentLabel);

                // Add to container
                container.getChildren().addAll(
                    nameLabel, emailLabel, programLabel, semesterLabel, yearLabel, dateLabel, statusLabel, paymentContainer);
            }

            @Override
            protected void updateItem(Application app, boolean empty) {
                super.updateItem(app, empty);

                if (empty || app == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Update the labels with application data
                nameLabel.setText(app.getApplicantName());
                emailLabel.setText(app.getEmail());
                programLabel.setText(app.getProgramName());

                // Extract semester and year from app.getSemesterAndYear() (which returns "Summer 2025")
                String[] semesterYear = app.getSemesterAndYear().split(" ");
                semesterLabel.setText(semesterYear.length > 0 ? semesterYear[0] : "");
                yearLabel.setText(semesterYear.length > 1 ? semesterYear[1] : "");

                dateLabel.setText(app.getApplicationDate().format(dateFormatter));

                // Set status with appropriate style
                statusLabel.setText(app.getStatus().getDisplayName());
                statusLabel.getStyleClass().setAll("cell-label", "status-column", app.getStatus().getStyleClass());

                // Handle payment section - show either label or button
                paymentContainer.getChildren().clear();
                if (app.isPaymentComplete()) {
                    paymentLabel.setText("Completed");
                    paymentLabel.getStyleClass().setAll("cell-label", "status-approved");
                    paymentContainer.getChildren().add(paymentLabel);
                } else {
                    // Configure payment button for this specific cell
                    paymentButton.setText("Make Payment");
                    paymentButton.setOnAction(e -> {
                        e.consume(); // Prevent event from reaching the list cell
                        showPaymentDialog(app);
                    });
                    paymentContainer.getChildren().add(paymentButton);
                }

                setGraphic(container);
            }
        };
    }

    /**
     * Show the loader animation
     */
    private void showLoader() {
        applicationContainer.setVisible(false);
        loaderContainer.setVisible(true);
        noApplicationsLabel.setVisible(false);
    }

    /**
     * Hide the loader animation
     */
    private void hideLoader() {
        loaderContainer.setVisible(false);

        // Show appropriate container based on applications list
        if (applications.isEmpty()) {
            noApplicationsLabel.setVisible(true);
            applicationContainer.setVisible(true);
        } else {
            noApplicationsLabel.setVisible(false);
            applicationContainer.setVisible(true);
        }
    }

    /**
     * Load applications from the service
     */
    private void loadApplications() {
        applicationService.getUserApplications()
            .thenAccept(appList -> {
                Platform.runLater(() -> {
                    applications.clear();
                    applications.addAll(appList);
                    hideLoader();

                    // Select the first application if available
                    if (!applications.isEmpty()) {
                        applicationListView.getSelectionModel().selectFirst();
                    }

                    LOGGER.log(Level.INFO, "Loaded {0} applications", appList.size());
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    hideLoader();
                    showErrorAlert("Failed to load applications", "User information is not available.");
                    LOGGER.log(Level.SEVERE, "Error loading applications: {0}", ex.getMessage());
                });
                return null;
            });
    }

    /**
     * Refresh applications list
     */
    @FXML
    private void refreshApplications() {
        showLoader();
        loadApplications();
    }

    /**
     * Update the application details panel with the selected application
     */
    private void updateApplicationDetails(Application application) {
        if (application == null) {
            return;
        }

        // Update labels if they exist
        if (programLabel != null) programLabel.setText(application.getProgramName());
        if (nameLabel != null) nameLabel.setText(application.getApplicantName());
        if (emailLabel != null) emailLabel.setText(application.getEmail());
        if (phoneLabel != null) phoneLabel.setText(application.getPhoneNumber());

        // Format date of birth if available
        if (dobLabel != null) {
            if (application.getDateOfBirth() != null) {
                dobLabel.setText(application.getDateOfBirth().format(dateFormatter));
            } else {
                dobLabel.setText("-");
            }
        }

        if (genderLabel != null) genderLabel.setText(application.getGender());
        if (addressLabel != null) addressLabel.setText(application.getAddress());
        if (cityLabel != null) cityLabel.setText(application.getCity());
        if (postalCodeLabel != null) postalCodeLabel.setText(application.getPostalCode());

        // Academic information
        if (sscGpaLabel != null) sscGpaLabel.setText(String.format("%.2f", application.getSscGpa()));
        if (hscGpaLabel != null) hscGpaLabel.setText(String.format("%.2f", application.getHscGpa()));
        if (sscYearLabel != null) sscYearLabel.setText(application.getSscYear());
        if (hscYearLabel != null) hscYearLabel.setText(application.getHscYear());

        // Guardian information
        if (fatherNameLabel != null) fatherNameLabel.setText(application.getFatherName());
        if (fatherOccupationLabel != null) fatherOccupationLabel.setText(application.getFatherOccupation());
        if (motherNameLabel != null) motherNameLabel.setText(application.getMotherName());
        if (motherOccupationLabel != null) motherOccupationLabel.setText(application.getMotherOccupation());
        if (guardianPhoneLabel != null) guardianPhoneLabel.setText(application.getGuardianPhone());
        if (guardianEmailLabel != null) guardianEmailLabel.setText(application.getGuardianEmail());

        // Status information
        if (statusLabel != null) {
            statusLabel.setText(application.getStatus().getDisplayName());
            statusLabel.getStyleClass().setAll("detail-value", "status-label", application.getStatus().getStyleClass());
        }

        if (paymentStatusLabel != null) {
            paymentStatusLabel.setText(application.isPaymentComplete() ? "Paid" : "Pending");
            paymentStatusLabel.getStyleClass().setAll("detail-value", "payment-status-label", 
                application.isPaymentComplete() ? "status-approved" : "status-pending");
        }

        // Format application date
        if (applicationDateLabel != null) applicationDateLabel.setText(application.getApplicationDate().format(dateFormatter));
    }

    /**
     * Update the action buttons based on the selected application
     */
    private void updateActionButtons(Application application) {
        boolean hasSelection = (application != null);

        // Add null checks to prevent NullPointerException
        if (makePaymentButton != null) {
            makePaymentButton.setDisable(!hasSelection || application.isPaymentComplete());
        }

        if (trackStatusButton != null) {
            trackStatusButton.setDisable(!hasSelection);
        }
    }

    /**
     * Show payment dialog for an application
     */
    private void showPaymentDialog(Application application) {
        if (application == null) {
            return;
        }

        // Don't allow payments for rejected applications or already paid applications
        if (application.isPaymentComplete()) {
            showInfoAlert("Payment Complete", "This application has already been fully paid.");
            return;
        }


        // Directly process the payment without confirmation dialog
        processPayment(application);
    }

    /**
     * Process payment for an application using SSLCommerz
     */
    private void processPayment(Application application) {
        showLoader();

        // Get current user info for the payment
        User currentUser = AuthStateManager.getInstance().getState().getUser();
        if (currentUser == null) {
            hideLoader();
            showErrorAlert("Payment Error", "User information is not available.");
            return;
        }

        // Create SSLCommerz payment object for 1000 Tk application fee
        SSLCommerzPayment payment = new com.ueadmission.payment.SSLCommerzPayment(1000.0, "UIU Admission Application Fee")
            .withCustomer(
                currentUser.getFullName(),
                currentUser.getEmail(),
                currentUser.getPhoneNumber()
            )
            .withParam("application_id", String.valueOf(application.getId()));

        // Start payment process
        payment.startPayment(result -> {
            Platform.runLater(() -> {
                hideLoader();

                if (result.isSuccessful()) {
                    // Update the database with the new payment status
                    boolean dbUpdateSuccess = com.ueadmission.admission.ApplicationDAO.updatePaymentStatus(
                            parseInt(application.getId()), true);

                    if (!dbUpdateSuccess) {
                        showErrorAlert("Database Error", 
                            "Payment was successful, but we couldn't update your application status in the database.\n" +
                            "Please contact support with your Transaction ID: " + result.getTransactionId());
                        return;
                    }

                    // Update application payment status in memory
                    application.setPaymentStatus(PaymentStatus.PAID);

                    // If payment is complete, update status to Approved
                    if (application.getStatus() == ApplicationStatus.PENDING) {
                        application.setStatus(ApplicationStatus.APPROVED);
                    }

                    // Update the application in the list
                    int index = applications.indexOf(application);
                    if (index >= 0) {
                        applications.set(index, application);
                    }

                    // Update the selected application
                    selectedApplication.set(application);

                    // Update UI to reflect changes (disable payment button)
                    updateActionButtons(application);

                    // Show success dialog styled like in admission screen
                    showPaymentSuccessDialog(result.getTransactionId(), application);

                } else {
                    // Payment failed
                    showErrorAlert("Payment Failed", 
                        "Failed to process payment: " + result.getMessage() + "\n" +
                        "Please try again later.");
                }
            });
        });
    }

    /**
     * Show a custom styled payment success dialog
     */
    private void showPaymentSuccessDialog(String transactionId, Application application) {
        // Create a standard JavaFX dialog styled to match our theme
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Payment Successful");

        // Create the DialogPane (where we can apply styling)
        DialogPane dialogPane = dialog.getDialogPane();

        // Apply CSS styling from application theme
        dialogPane.getStylesheets().add(getClass().getResource("/com.ueadmission/common.css").toExternalForm());
        dialogPane.getStylesheets().add(getClass().getResource("/com.ueadmission/main.css").toExternalForm());

        // Custom styles for this specific dialog
        dialogPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0, 0, 4);");

        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setMinWidth(450);
        content.setMaxWidth(450);

        // Create styled components
        Label message = new Label("Payment Successful!");
        message.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2D8E36;");

        Label detailsLabel = new Label("Your payment of 1000 Tk has been processed successfully.");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        detailsLabel.setWrapText(true);

        Label transactionLabel = new Label("Transaction ID: " + transactionId);
        transactionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

        Label statusLabel = new Label("Your application status is now " + application.getStatus().getDisplayName());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        statusLabel.setWrapText(true);

        // Add components to content
        content.getChildren().addAll(message, detailsLabel, transactionLabel, statusLabel);

        // Set custom content to the dialog
        dialogPane.setContent(content);

        // Remove the header text that comes by default
        dialog.setHeaderText(null);

        // Create custom button with app's styling
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(closeButtonType);

        // Style the button to match theme
        Button closeButton = (Button) dialogPane.lookupButton(closeButtonType);
        closeButton.setStyle("-fx-background-color: #fa4506; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 5;");
        closeButton.setPrefWidth(120);

        // Show dialog
        dialog.showAndWait();

        // Update the application list to reflect the changes
        refreshApplications();
    }

    /**
     * Track the status of an application
     */
    private void trackApplicationStatus(Application application) {
        if (application == null) {
            return;
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Application Status");
        alert.setHeaderText("Status of Application #" + application.getId());

        StringBuilder content = new StringBuilder();
        content.append("Program: ").append(application.getProgramName()).append("\n");
        content.append("Current Status: ").append(application.getStatus().getDisplayName()).append("\n");
        content.append("Application Date: ").append(application.getApplicationDate().format(dateFormatter)).append("\n");
        content.append("Payment Status: ").append(application.isPaymentComplete() ? "Paid" : "Pending").append("\n\n");

        // Add status description based on current status
        ApplicationStatus status = application.getStatus();
        if (status == ApplicationStatus.PENDING) {
            content.append("Your application is pending review. Please ensure your payment is complete to proceed further.");
        } else if (status == ApplicationStatus.APPROVED) {
            content.append("Congratulations! Your application has been approved. Please check your email for further instructions.");
        } else {
            content.append("Status information not available.");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Show an error alert
     */
    private void showErrorAlert(String message, String s) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an information alert
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Called when scene becomes visible or active
     */
    public void onSceneActive() {
        refreshUI();
        refreshApplications();
    }

    /**
     * Refresh the UI based on the current auth state
     */
    public void refreshUI() {
        AuthState state = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (state != null && state.isAuthenticated());

        // Update visibility based on auth state - Add null checks
        if (loginButtonContainer != null) {
            loginButtonContainer.setVisible(!isAuthenticated);
            loginButtonContainer.setManaged(!isAuthenticated);
        }

        if (profileButtonContainer != null) {
            profileButtonContainer.setVisible(isAuthenticated);
            profileButtonContainer.setManaged(isAuthenticated);
        }

        // If not authenticated, redirect to login - Add null check for scene
        if (!isAuthenticated && applicationListView != null && applicationListView.getScene() != null) {
            Platform.runLater(() -> 
                NavigationUtil.navigateToLogin(new ActionEvent(applicationListView, null)));
        }
    }

    /**
     * Cleanup resources before navigating away
     */
    private void cleanup() {
        if (applicationListView != null && applicationListView.getScene() != null && 
                applicationListView.getScene().getRoot() != null) {
            applicationListView.getScene().getRoot().setOpacity(1.0);
        }
    }

    /**
     * Navigate to the Home screen
     */
    @FXML
    private void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigate to the About screen
     */
    @FXML
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigate to the Admission screen
     */
    @FXML
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigate to the Login screen
     */
    @FXML
    private void navigateToLogin(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }
      /**
     * Navigate to the Exam Portal screen
     */
    @FXML
    private void navigateToExamPortal(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Navigate to the Contact screen
     */
    @FXML
    private void navigateToContact(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }
}
