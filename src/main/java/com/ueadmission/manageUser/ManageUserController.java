package com.ueadmission.manageUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Controller for the Manage Users page
 * This page allows administrators to view all users in the system
 */
public class ManageUserController {

    private static final Logger LOGGER = Logger.getLogger(ManageUserController.class.getName());

    @FXML
    private ListView<User> userListView;

    @FXML
    private Label noUsersLabel;

    @FXML
    private StackPane loaderContainer;

    @FXML
    private VBox userContainer;

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
    private MFXSpinner<?> spinner;

    // User details labels
    @FXML private Label idLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label cityLabel;
    @FXML private Label countryLabel;
    @FXML private Label roleLabel;
    @FXML private Label ipAddressLabel;
    @FXML private Label lastLoginTimeLabel;
    @FXML private Label isLoggedInLabel;

    // Observable list to hold users
    private final ObservableList<User> users = FXCollections.observableArrayList();

    // Currently selected user
    private final ObjectProperty<User> selectedUser = new SimpleObjectProperty<>();

    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up ListView with custom cell factory
        userListView.setItems(users);
        userListView.setCellFactory(createCellFactory());

        // Add selection change listener to update details when a row is selected
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser.set(newVal);
            updateUserDetails(newVal);
        });

        // Set up navigation buttons with null checks
        if (homeButton != null) {
            homeButton.setOnAction(this::navigateToHome);
        }
        if (aboutButton != null) {
            aboutButton.setOnAction(this::navigateToAbout);
        }
        if (admissionButton != null) {
            admissionButton.setOnAction(this::navigateToAdmission);
        }
        if (examPortalButton != null) {
            examPortalButton.setOnAction(this::navigateToExamPortal);
        }
        if (contactButton != null) {
            contactButton.setOnAction(this::navigateToContact);
        }
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> refreshUsers());
        }

        // Load users data
        showLoader();
        loadAllUsers();

        // Update UI based on authentication state
        refreshUI();
    }

    /**
     * Create the cell factory for the user list view
     */
    private Callback<ListView<User>, ListCell<User>> createCellFactory() {
        return listView -> new ListCell<User>() {
            private final HBox container = new HBox();
            private final Label idLabel = new Label();
            private final Label nameLabel = new Label();
            private final Label emailLabel = new Label();
            private final Label phoneLabel = new Label();
            private final Label roleLabel = new Label();
            private final Label statusLabel = new Label();

            {
                // Configure the cell layout
                container.getStyleClass().add("user-list-item");
                container.setSpacing(10);
                container.setAlignment(Pos.CENTER_LEFT);

                // Configure the labels with specific column classes
                idLabel.getStyleClass().addAll("cell-label", "id-column");
                nameLabel.getStyleClass().addAll("cell-label", "name-column");
                emailLabel.getStyleClass().addAll("cell-label", "email-column");
                phoneLabel.getStyleClass().addAll("cell-label", "phone-column");
                roleLabel.getStyleClass().addAll("cell-label", "role-column");
                statusLabel.getStyleClass().addAll("cell-label", "status-column");

                // Add to container
                container.getChildren().addAll(
                    idLabel, nameLabel, emailLabel, phoneLabel, roleLabel, statusLabel);
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Update the labels with user data
                idLabel.setText(String.valueOf(user.getId()));
                nameLabel.setText(user.getFullName());
                emailLabel.setText(user.getEmail());
                phoneLabel.setText(user.getPhoneNumber());
                roleLabel.setText(user.getRole());

                // Set status with appropriate style
                if (user.isLoggedIn()) {
                    statusLabel.setText("Online");
                    statusLabel.getStyleClass().setAll("cell-label", "status-column", "status-approved");
                } else {
                    statusLabel.setText("Offline");
                    statusLabel.getStyleClass().setAll("cell-label", "status-column", "status-pending");
                }

                setGraphic(container);
            }
        };
    }

    /**
     * Show the loader animation
     */
    private void showLoader() {
        userContainer.setVisible(false);
        loaderContainer.setVisible(true);
        noUsersLabel.setVisible(false);
    }

    /**
     * Hide the loader animation
     */
    private void hideLoader() {
        loaderContainer.setVisible(false);

        // Show appropriate container based on users list
        if (users.isEmpty()) {
            noUsersLabel.setVisible(true);
            userContainer.setVisible(true);
        } else {
            noUsersLabel.setVisible(false);
            userContainer.setVisible(true);
        }
    }

    /**
     * Load all users from the database
     * This method fetches all users for admin view
     */
    private void loadAllUsers() {
        CompletableFuture.supplyAsync(() -> {
            List<User> userList = new ArrayList<>();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT * FROM users ORDER BY id";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            userList.add(mapResultSetToUser(rs));
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error fetching all users", e);
            }

            return userList;
        }).thenAccept(userList -> {
            Platform.runLater(() -> {
                users.clear();
                users.addAll(userList);
                hideLoader();

                // Select the first user if available
                if (!users.isEmpty()) {
                    userListView.getSelectionModel().selectFirst();
                }

                LOGGER.log(Level.INFO, "Loaded {0} users", userList.size());
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                hideLoader();
                showErrorAlert("Failed to load users", "Database error occurred.");
                LOGGER.log(Level.SEVERE, "Error loading users: {0}", ex.getMessage());
            });
            return null;
        });
    }

    /**
     * Map a ResultSet row to a User object
     * 
     * @param rs The ResultSet
     * @return The User object
     * @throws SQLException If an error occurs
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String phoneNumber = rs.getString("phone");
        String address = rs.getString("address");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String role = rs.getString("role");
        String ipAddress = rs.getString("ip_address");

        // Handle nullable timestamp
        LocalDateTime lastLoginTime = null;
        if (rs.getTimestamp("last_login_time") != null) {
            lastLoginTime = rs.getTimestamp("last_login_time").toLocalDateTime();
        }

        boolean isLoggedIn = rs.getBoolean("is_logged_in");

        return new User(id, firstName, lastName, email, phoneNumber, 
                        address, city, country, role, 
                        ipAddress, lastLoginTime, isLoggedIn);
    }

    /**
     * Refresh users list
     */
    @FXML
    private void refreshUsers() {
        showLoader();
        loadAllUsers();
    }

    /**
     * Update the user details panel with the selected user
     */
    private void updateUserDetails(User user) {
        if (user == null) {
            return;
        }

        // Update labels if they exist
        if (idLabel != null) idLabel.setText(String.valueOf(user.getId()));
        if (firstNameLabel != null) firstNameLabel.setText(user.getFirstName());
        if (lastNameLabel != null) lastNameLabel.setText(user.getLastName());
        if (emailLabel != null) emailLabel.setText(user.getEmail());
        if (phoneLabel != null) phoneLabel.setText(user.getPhoneNumber());
        if (addressLabel != null) addressLabel.setText(user.getAddress());
        if (cityLabel != null) cityLabel.setText(user.getCity());
        if (countryLabel != null) countryLabel.setText(user.getCountry());
        if (roleLabel != null) roleLabel.setText(user.getRole());
        if (ipAddressLabel != null) ipAddressLabel.setText(user.getIpAddress() != null ? user.getIpAddress() : "N/A");

        // Format last login time if available
        if (lastLoginTimeLabel != null) {
            if (user.getLastLoginTime() != null) {
                lastLoginTimeLabel.setText(user.getLastLoginTime().format(dateFormatter));
            } else {
                lastLoginTimeLabel.setText("Never");
            }
        }

        // Set login status with appropriate style
        if (isLoggedInLabel != null) {
            if (user.isLoggedIn()) {
                isLoggedInLabel.setText("Online");
                isLoggedInLabel.getStyleClass().setAll("detail-value", "status-approved");
            } else {
                isLoggedInLabel.setText("Offline");
                isLoggedInLabel.getStyleClass().setAll("detail-value", "status-pending");
            }
        }
    }

    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
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
        refreshUsers();
    }

    /**
     * Refresh the UI based on the current auth state
     */
    public void refreshUI() {
        AuthState state = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (state != null && state.isAuthenticated());
        boolean isAdmin = isAuthenticated && "admin".equalsIgnoreCase(state.getUser().getRole());

        // Update visibility based on auth state - Add null checks
        if (loginButtonContainer != null) {
            loginButtonContainer.setVisible(!isAuthenticated);
            loginButtonContainer.setManaged(!isAuthenticated);
        }

        if (profileButtonContainer != null) {
            profileButtonContainer.setVisible(isAuthenticated);
            profileButtonContainer.setManaged(isAuthenticated);
        }

        // If not authenticated or not admin, redirect to home
        if (!isAuthenticated || !isAdmin) {
            Platform.runLater(() -> {
                if (userListView != null && userListView.getScene() != null) {
                    NavigationUtil.navigateToHome(new ActionEvent(userListView, null));
                    showErrorAlert("Access Denied", "You must be logged in as an administrator to access this page.");
                }
            });
        }
    }

    /**
     * Cleanup resources before navigating away
     */
    private void cleanup() {
        if (userListView != null && userListView.getScene() != null && 
                userListView.getScene().getRoot() != null) {
            userListView.getScene().getRoot().setOpacity(1.0);
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
