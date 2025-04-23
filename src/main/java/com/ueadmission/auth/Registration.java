package com.ueadmission.auth;

import com.ueadmission.Main;
import com.ueadmission.about.About;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class Registration {
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField addressField;
    
    @FXML
    private TextField cityField;
    
    @FXML
    private ComboBox<String> countryComboBox;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private CheckBox termsCheckbox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    public void initialize() {
        // Initialize the country dropdown with some common countries
        countryComboBox.getItems().addAll(
            "Bangladesh", "India", "Pakistan", "Sri Lanka", "Nepal", "Bhutan", "Maldives",
            "USA", "UK", "Canada", "Australia", "Germany", "France", "Japan", "China"
        );
    }

    /**
     * Navigates to the Home screen
     */
    @FXML
    public void navigateToHome(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();
        double x = currentStage.getX();
        double y = currentStage.getY();
        boolean maximized = currentStage.isMaximized();
        
        Stage mainWindow = Auth.prepareMainWindow(width, height, x, y, maximized);
        if (mainWindow != null) {
            currentStage.close();
            mainWindow.show();
        }
    }
    
    /**
     * Navigates to the About screen
     */
    @FXML
    public void navigateToAbout(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();
        double x = currentStage.getX();
        double y = currentStage.getY();
        boolean maximized = currentStage.isMaximized();
        
        Stage aboutWindow = About.prepareAboutWindow(width, height, x, y, maximized);
        if (aboutWindow != null) {
            currentStage.close();
            aboutWindow.show();
        }
    }
    
    /**
     * Navigates to the Login screen
     */
    @FXML
    public void navigateToLogin(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();
        double x = currentStage.getX();
        double y = currentStage.getY();
        boolean maximized = currentStage.isMaximized();
        
        Stage loginWindow = Auth.prepareLoginWindow(width, height, x, y, maximized);
        if (loginWindow != null) {
            currentStage.close();
            loginWindow.show();
        }
    }

    /**
     * Handles the form submission for registration
     */
    @FXML
    public void handleRegistration(ActionEvent event) {
        // Reset error state
        errorLabel.setVisible(false);
        
        // Validate form inputs
        if (isFormValid()) {
            // Process registration (in a real app, this would connect to a backend)
            System.out.println("Registration successful!");
            
            // Redirect to login
            navigateToLogin(event);
        } else {
            // Show error message
            errorLabel.setVisible(true);
        }
    }
    
    /**
     * Validates all form inputs
     * @return true if all required fields are filled and valid
     */
    private boolean isFormValid() {
        // Check required fields
        if (isEmpty(firstNameField.getText()) || 
            isEmpty(lastNameField.getText()) ||
            isEmpty(emailField.getText()) ||
            isEmpty(phoneField.getText()) ||
            isEmpty(addressField.getText()) ||
            isEmpty(cityField.getText()) ||
            countryComboBox.getValue() == null ||
            isEmpty(passwordField.getText()) ||
            isEmpty(confirmPasswordField.getText())) {
            return false;
        }
        
        // Check email format
        if (!isValidEmail(emailField.getText())) {
            return false;
        }
        
        // Check password match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            return false;
        }
        
        // Check terms agreement
        return termsCheckbox.isSelected();
    }
    
    /**
     * Checks if a string is empty or null
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
