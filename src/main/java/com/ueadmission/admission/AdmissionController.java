package com.ueadmission.admission;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.payment.SSLCommerzPayment;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdmissionController {
    private HostServices hostServices;

    private static final Logger LOGGER = Logger.getLogger(AdmissionController.class.getName());
    private Consumer<AuthState> authStateListener;
    
    // Store the current application ID for payment processing
    private int currentApplicationId = -1;

    @FXML
    private ImageView campusImage;

    @FXML
    private MFXButton visitWebsiteBtn;

    @FXML
    private MFXButton homeButton;

    @FXML
    private MFXButton loginButton;

    @FXML
    private MFXButton aboutButton;

    @FXML
    private MFXButton examPortalButton;

    @FXML
    private MFXButton contactButton;

    @FXML
    private HBox loginButtonContainer;

    @FXML
    private HBox profileButtonContainer;

    @FXML
    private ProfileButton profileButton;

    @FXML
    private VBox root;

    @FXML
    private Button applyNowBtn;

    @FXML
    private Label titleLabel;

    @FXML
    private Button btnSoBE;

    @FXML
    private Button btnSoSE;

    @FXML
    private Button btnSoHSS;

    @FXML
    private Button btnSoLS;

    @FXML
    private VBox infoBox;

    @FXML
    private VBox applicationFormContainer;

    // Personal Information Fields
    @FXML
    private MFXTextField firstNameField;

    @FXML
    private MFXTextField lastNameField;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXTextField phoneField;

    @FXML
    private DatePicker dobPicker;
    
    @FXML
    private MFXComboBox<String> genderComboBox;
    
    @FXML
    private MFXTextField addressField;
    
    @FXML
    private MFXTextField cityField;
    
    @FXML
    private MFXTextField postalCodeField;

    // Guardian Information Fields
    @FXML
    private MFXTextField fatherNameField;
    
    @FXML
    private MFXTextField fatherOccupationField;
    
    @FXML
    private MFXTextField motherNameField;
    
    @FXML
    private MFXTextField motherOccupationField;
    
    @FXML
    private MFXTextField guardianPhoneField;
    
    @FXML
    private MFXTextField guardianEmailField;

    // Academic Information Fields
    @FXML
    private MFXComboBox<String> programComboBox;

    @FXML
    private MFXTextField institutionField;

    @FXML
    private MFXTextField sscGpaField;

    @FXML
    private MFXTextField hscGpaField;
    
    @FXML
    private MFXTextField sscYearField;
    
    @FXML
    private MFXTextField hscYearField;
    
    @FXML
    private MFXCheckbox declarationCheckbox;

    @FXML
    void openBBA(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://sobe.uiu.ac.bd/bba/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openBBAAIS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://sobe.uiu.ac.bd/bba-in-ais/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEconomics(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://sobe.uiu.ac.bd/economics/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCivil(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://ce.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCSE(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://cse.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openDS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://datascience.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEEE(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://eee.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEDS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://eds.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openMSJ(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://msj.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEnglish(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://english.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openPharmacy(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://pharmacy.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openBiotech(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://bge.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showApplicationForm(ActionEvent event) {
        // Auto-populate fields from the current user data
        populateUserData();
        
        applicationFormContainer.setVisible(true);
        applicationFormContainer.setManaged(true);
        applicationFormContainer.toFront();
    }
    
    /**
     * Auto-populate form fields from the current user data
     */
    private void populateUserData() {
        User currentUser = AuthStateManager.getInstance().getState().getUser();
        
        if (currentUser != null) {
            // Populate personal information fields
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
            addressField.setText(currentUser.getAddress());
            cityField.setText(currentUser.getCity());
            
            // Make email field read-only
            emailField.setEditable(false);
            // Apply a style to show it's read-only
            emailField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.9;");
        }
    }

    @FXML
    void hideApplicationForm(ActionEvent event) {
        applicationFormContainer.setVisible(false);
        applicationFormContainer.setManaged(false);
        applicationFormContainer.toBack();
    }

    @FXML
    void resetForm(ActionEvent event) {
        // Clear all form fields - Personal Information
        firstNameField.clear();
        lastNameField.clear();
        
        // Don't clear email field, it will be auto-populated and read-only
        
        phoneField.clear();
        dobPicker.setValue(null); 
        genderComboBox.clear();
        addressField.clear();
        cityField.clear();
        postalCodeField.clear();
        
        // Clear Guardian Information
        fatherNameField.clear();
        fatherOccupationField.clear();
        motherNameField.clear();
        motherOccupationField.clear();
        guardianPhoneField.clear();
        guardianEmailField.clear();
        
        // Clear Academic Information
        programComboBox.clear();
        institutionField.clear();
        sscGpaField.clear();
        hscGpaField.clear();
        sscYearField.clear();
        hscYearField.clear();
        
        // Uncheck declaration
        if (declarationCheckbox != null) {
            declarationCheckbox.setSelected(false);
        }
        
        // Re-populate user data after reset
        populateUserData();
    }

    @FXML
    void submitForm(ActionEvent event) {
        // Basic validation
        if (firstNameField.getText().isEmpty() || 
            lastNameField.getText().isEmpty() || 
            emailField.getText().isEmpty() ||
            phoneField.getText().isEmpty() ||
            dobPicker.getValue() == null ||
            genderComboBox.getValue() == null ||
            addressField.getText().isEmpty() ||
            fatherNameField.getText().isEmpty() ||
            motherNameField.getText().isEmpty() ||
            guardianPhoneField.getText().isEmpty() ||
            programComboBox.getValue() == null ||
            institutionField.getText().isEmpty() ||
            sscGpaField.getText().isEmpty() ||
            hscGpaField.getText().isEmpty()) {
            
            // Show error message or alert
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "Please fill in all required fields marked with *.",
                ButtonType.OK
            );
            alert.setHeaderText("Form Validation Error");
            alert.show();
            return;
        }
        
        // Check if declaration is checked
        if (declarationCheckbox != null && !declarationCheckbox.isSelected()) {
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "You must agree to the terms and conditions to submit the application.",
                ButtonType.OK
            );
            alert.setHeaderText("Declaration Required");
            alert.show();
            return;
        }
        
        // Validate email format
        if (!isValidEmail(emailField.getText())) {
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "Please enter a valid email address.",
                ButtonType.OK
            );
            alert.setHeaderText("Invalid Email");
            alert.show();
            return;
        }
        
        // Validate GPA values
        double sscGpa, hscGpa;
        try {
            sscGpa = Double.parseDouble(sscGpaField.getText());
            hscGpa = Double.parseDouble(hscGpaField.getText());
            
            if (sscGpa < 0 || sscGpa > 5 || hscGpa < 0 || hscGpa > 5) {
                Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "GPA values must be between 0 and 5.",
                    ButtonType.OK
                );
                alert.setHeaderText("Invalid GPA");
                alert.show();
                return;
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "GPA values must be numeric.",
                ButtonType.OK
            );
            alert.setHeaderText("Invalid GPA Format");
            alert.show();
            return;
        }
        
        // If validation passes, save application to database
        try {
            User currentUser = AuthStateManager.getInstance().getState().getUser();
            
            if (currentUser == null) {
                Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "You must be logged in to submit an application.",
                    ButtonType.OK
                );
                alert.setHeaderText("Authentication Required");
                alert.show();
                return;
            }
            
            // Create Application object
            Application application = new Application(
                currentUser.getId(),
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                dobPicker.getValue(),
                genderComboBox.getValue(),
                addressField.getText(),
                cityField.getText(),
                postalCodeField.getText(),
                fatherNameField.getText(),
                fatherOccupationField.getText(),
                motherNameField.getText(),
                motherOccupationField.getText(),
                guardianPhoneField.getText(),
                guardianEmailField.getText(),
                programComboBox.getValue(),
                institutionField.getText(),
                Double.parseDouble(sscGpaField.getText()),
                Double.parseDouble(hscGpaField.getText()),
                sscYearField.getText(),
                hscYearField.getText()
            );
            
            // Save to database
            currentApplicationId = ApplicationDAO.createApplication(application);
            
            if (currentApplicationId > 0) {
                // Show success message with payment button
                showSuccessWithPaymentOption();
            } else {
                // Show error message
                Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Failed to submit application. Please try again later.",
                    ButtonType.OK
                );
                alert.setHeaderText("Submission Error");
                alert.show();
            }
        } catch (Exception e) {
            LOGGER.severe("Error submitting application: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "An error occurred while submitting your application: " + e.getMessage(),
                ButtonType.OK
            );
            alert.setHeaderText("Submission Error");
            alert.show();
        }
    }
    
    private void showSuccessWithPaymentOption() {
        // First, hide the application form to ensure it's closed before showing the dialog
        hideApplicationForm(null);
        
        // Create a standard JavaFX dialog styled to match our theme
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Application Submitted");
        
        // Create the DialogPane (where we can apply styling)
        DialogPane dialogPane = dialog.getDialogPane();
        
        // Apply CSS styling from your application theme
        dialogPane.getStylesheets().add(getClass().getResource("/com.ueadmission/common.css").toExternalForm());
        dialogPane.getStylesheets().add(getClass().getResource("/com.ueadmission/main.css").toExternalForm());
        
        // Custom styles for this specific dialog to match your theme
        dialogPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0, 0, 4);");
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setMinWidth(450);
        content.setMaxWidth(450);
        
        // Create styled components
        Label message = new Label("Application Submitted Successfully!");
        message.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2D8E36;");
        
        Label paymentLabel = new Label("To finalize your application, please complete the payment.");
        paymentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        paymentLabel.setWrapText(true);
        
        Label amountLabel = new Label("Application Fee: 1000 Tk");
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");
        
        // Add components to content
        content.getChildren().addAll(message, paymentLabel, amountLabel);
        
        // Set custom content to the dialog
        dialogPane.setContent(content);
        
        // Remove the header text that comes by default
        dialog.setHeaderText(null);
        
        // Create custom buttons with your app's styling
        ButtonType payButtonType = new ButtonType("Pay Now (1000 Tk)", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialogPane.getButtonTypes().addAll(closeButtonType, payButtonType);
        
        // Style the buttons to match your theme
        Button payButton = (Button) dialogPane.lookupButton(payButtonType);
        payButton.setStyle("-fx-background-color: #fa4506; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 5;");
        payButton.setPrefWidth(200);
        
        Button closeButton = (Button) dialogPane.lookupButton(closeButtonType);
        closeButton.setStyle("-fx-background-color: #dddddd; -fx-text-fill: #333333; -fx-padding: 12 20; -fx-background-radius: 5;");
        closeButton.setPrefWidth(120);
        
        // Show dialog and handle the result
        try {
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == payButtonType) {
                initiateSSLCommerzPayment();
            }
        } catch (Exception e) {
            LOGGER.severe("Error showing payment dialog: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple notification if dialog fails
            MFXNotifications.showSuccess("Application Submitted", 
                "Your application has been submitted successfully. Please proceed to payment.");
                
            // Try direct payment initiation
            initiateSSLCommerzPayment();
        }
    }

    /**
     * Initiate payment via SSLCommerz
     */
    private void initiateSSLCommerzPayment() {
        if (currentApplicationId <= 0) {
            MFXNotifications.showError("Payment Error", "Cannot process payment. Application ID is invalid.");
            return;
        }
        
        // Get current user info for the payment
        User currentUser = AuthStateManager.getInstance().getState().getUser();
        if (currentUser == null) {
            MFXNotifications.showError("Payment Error", "User information is not available.");
            return;
        }
        
        // Create SSLCommerz payment object
        SSLCommerzPayment payment = new SSLCommerzPayment(1000.0, "UIU Admission Application Fee")
            .withCustomer(
                currentUser.getFullName(),
                currentUser.getEmail(),
                currentUser.getPhoneNumber()
            )
            .withParam("application_id", String.valueOf(currentApplicationId));
        
        // Start payment process
        payment.startPayment(result -> {
            if (result.isSuccessful()) {
                // Update payment status in database
                boolean success = ApplicationDAO.updatePaymentStatus(currentApplicationId, true);
                
                if (success) {
                    MFXNotifications.showSuccess("Payment Successful", 
                        "Your payment of 1000 Tk has been processed successfully.\n" +
                        "Transaction ID: " + result.getTransactionId() + "\n" +
                        "Your application status is now Approved.");
                } else {
                    MFXNotifications.showError("Database Error", 
                        "Payment was successful, but we couldn't update your application status.\n" +
                        "Please contact support with your Transaction ID: " + result.getTransactionId());
                }
            } else {
                MFXNotifications.showError("Payment Failed", 
                    "Failed to process payment: " + result.getMessage() + "\n" +
                    "Please try again later.");
            }
        });
    }

    /**
     * Process payment for the application
     * @deprecated This method is replaced by initiateSSLCommerzPayment()
     */
    @Deprecated
    private void processPayment() {
        // Replaced by SSLCommerz integration
        initiateSSLCommerzPayment();
    }

    /**
     * Simple email validation method
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private void displayInfo(String schoolTitle, String[] items) {
        infoBox.getChildren().clear();

        Label header = new Label(schoolTitle);
        header.getStyleClass().add("section-title");
        infoBox.getChildren().add(header);

        for (String item : items) {
            Label label = new Label(item);
            label.getStyleClass().add("info-label");
            infoBox.getChildren().add(label);
        }
    }

    @FXML
    private void initialize() {
        // Set up the button action to open the UIU website
        visitWebsiteBtn.setOnAction(event -> openWebsite("https://www.uiu.ac.bd/"));

        // Configure navigation buttons
        homeButton.setOnAction(event -> navigateToHome(event));

        // Configure login button if it exists
        if (loginButton != null) {
            loginButton.setOnAction(event -> navigateToLogin(event));
        }

        // Configure other navigation buttons if they exist
        if (aboutButton != null) {
            aboutButton.setOnAction(event -> navigateToAbout(event));
        }

        if (contactButton != null) {
            contactButton.setOnAction(event -> navigateToContact(event));
        }

        // Subscribe to auth state changes
        subscribeToAuthStateChanges();

        // Refresh UI with current auth state
        refreshUI();

        // Call onSceneActive to ensure UI is updated when scene is shown
        javafx.application.Platform.runLater(this::onSceneActive);

        // Initialize the program combo box
        if (programComboBox != null) {
            programComboBox.getItems().addAll(
                "BBA", "BBA in AIS", "Economics", 
                "Civil Engineering", "CSE", "Data Science", "EEE",
                "EDS", "MSJ", "English",
                "Pharmacy", "Biotech and Genetic Engineering"
            );
        }
        
        // Initialize gender combo box
        if (genderComboBox != null) {
            genderComboBox.getItems().addAll("Male", "Female", "Other");
        }
        
        // Configure DatePicker format
        if (dobPicker != null) {
            // Set the prompt text and converter for the standard DatePicker
            dobPicker.setPromptText("MM/DD/YYYY");
            // You can also set a converter to format the displayed date
            dobPicker.setConverter(new javafx.util.StringConverter<java.time.LocalDate>() {
                private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

                @Override
                public String toString(java.time.LocalDate date) {
                    if (date != null) {
                        return dateFormatter.format(date);
                    } else {
                        return "";
                    }
                }

                @Override
                public java.time.LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        try {
                            return java.time.LocalDate.parse(string, dateFormatter);
                        } catch (Exception e) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            });
        }
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Admission screen
     */
    private void cleanup() {
        LOGGER.info("Cleaning up AdmissionController before navigation");

        // Reset opacity on the scene root if available
        if (visitWebsiteBtn != null && visitWebsiteBtn.getScene() != null &&
                visitWebsiteBtn.getScene().getRoot() != null) {
            visitWebsiteBtn.getScene().getRoot().setOpacity(1.0);
            LOGGER.info("Reset opacity to 1.0 during cleanup");
        }

        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
            LOGGER.info("Unsubscribed from auth state changes during cleanup");
        }
    }

    private void subscribeToAuthStateChanges() {
        // Create auth state listener
        authStateListener = newState -> {
            LOGGER.info("Auth state change detected in AboutController");

            boolean isAuthenticated = (newState != null && newState.isAuthenticated() && newState.getUser() != null);

            // Ensure we're on the JavaFX Application Thread for UI updates
            if (!javafx.application.Platform.isFxApplicationThread()) {
                javafx.application.Platform.runLater(() -> {
                    // Update UI based on new state
                    updateContainersVisibility(isAuthenticated);

                    // Update profile button if authenticated
                    if (isAuthenticated && profileButton != null) {
                        profileButton.updateUIFromAuthState(newState);
                        LOGGER.info("Updated profile button from auth state listener");
                    }
                });
            } else {
                // Update UI based on new state
                updateContainersVisibility(isAuthenticated);

                // Update profile button if authenticated
                if (isAuthenticated && profileButton != null) {
                    profileButton.updateUIFromAuthState(newState);
                    LOGGER.info("Updated profile button from auth state listener");
                }
            }
        };

        // Subscribe to auth state changes
        AuthStateManager.getInstance().subscribe(authStateListener);
        LOGGER.info("Subscribed to auth state changes in AboutController");

        // Force an initial update
        AuthState currentState = AuthStateManager.getInstance().getState();
        if (currentState != null) {
            authStateListener.accept(currentState);
        }
    }

    /**
     * Called when scene becomes visible or active
     * This ensures the UI is updated with current auth state
     */
    public void onSceneActive() {
        LOGGER.info("About scene became active, refreshing auth UI");
        refreshUI();
    }

    /**
     * Manually refresh the UI state based on current auth state
     */
    public void refreshUI() {
        System.out.println("Refreshing AboutController UI");

        // Get the current auth state
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated()
                && currentState.getUser() != null);

        System.out.println("Current auth state in About refreshUI: " + isAuthenticated);

        // Ensure we're on the JavaFX Application Thread for UI updates
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> {
                updateUIComponents(currentState, isAuthenticated);
            });
        } else {
            updateUIComponents(currentState, isAuthenticated);
        }
    }

    /**
     * Helper method to update UI components based on auth state
     */
    private void updateUIComponents(AuthState currentState, boolean isAuthenticated) {
        try {
            // Update container visibility
            updateContainersVisibility(isAuthenticated);

            // Update profile button if it exists
            if (profileButton != null) {
                profileButton.updateUIFromAuthState(currentState);
                System.out.println("Updated profile button in Admission page");
            } else {
                System.out.println("Profile button is null in AdmissionController");

                // Try to find profile button by lookup
                if (visitWebsiteBtn != null && visitWebsiteBtn.getScene() != null) {
                    Node foundProfileButton = visitWebsiteBtn.getScene().lookup(".profile-button-container");
                    if (foundProfileButton instanceof ProfileButton) {
                        System.out.println("Found profile button by lookup in scene");
                        ((ProfileButton) foundProfileButton).updateUIFromAuthState(currentState);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating UI components in AboutController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update containers visibility based on authentication status
     */
    private void updateContainersVisibility(boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    System.out.println("About page: Updated login container visibility: " + !isAuthenticated);
                }

                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    System.out.println("About page: Updated profile container visibility: " + isAuthenticated);
                }

                // If containers are null, try to find by ID
                if ((loginButtonContainer == null || profileButtonContainer == null) &&
                        visitWebsiteBtn != null && visitWebsiteBtn.getScene() != null) {

                    if (loginButtonContainer == null) {
                        Node loginContainer = visitWebsiteBtn.getScene().lookup("#loginButtonContainer");
                        if (loginContainer != null) {
                            loginContainer.setVisible(!isAuthenticated);
                            loginContainer.setManaged(!isAuthenticated);
                            System.out.println("Found and updated login container by lookup");
                        }
                    }

                    if (profileButtonContainer == null) {
                        Node profileContainer = visitWebsiteBtn.getScene().lookup("#profileButtonContainer");
                        if (profileContainer != null) {
                            profileContainer.setVisible(isAuthenticated);
                            profileContainer.setManaged(isAuthenticated);
                            System.out.println("Found and updated profile container by lookup");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error updating containers in About page: " + e.getMessage());
            }
        });
    }

    /**
     * Opens the specified URL in the default browser
     * @param url The URL to open
     */
    private void openWebsite(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.err.println("Failed to open website: " + e.getMessage());
        }
    }

    /**
     * Navigates to the home page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToHome(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the About page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToAbout(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigates to the Contact page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToContact(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }

    /**
     * Navigates to the Login page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToLogin(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
    }
}








