package com.ueadmission.examMonitoring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.navigation.NavigationUtil.AuthStateAware;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Controller for the Exam Monitoring page.
 * This page displays students with warning counts during exams.
 */
public class ExamMonitoringController implements AuthStateAware {
    private static final Logger LOGGER = Logger.getLogger(ExamMonitoringController.class.getName());

    // UI Components
    @FXML private MFXButton homeButton;
    @FXML private MFXButton aboutButton;
    @FXML private MFXButton admissionButton;
    @FXML private MFXButton examPortalButton;
    @FXML private MFXButton contactButton;
    @FXML private ProfileButton profileButton;
    @FXML private MFXButton refreshButton;

    @FXML private TableView<StudentWarning> studentTable;
    @FXML private TableColumn<StudentWarning, Integer> idColumn;
    @FXML private TableColumn<StudentWarning, String> nameColumn;
    @FXML private TableColumn<StudentWarning, String> examColumn;
    @FXML private TableColumn<StudentWarning, Integer> warningColumn;
    @FXML private TableColumn<StudentWarning, String> statusColumn;
    @FXML private TableColumn<StudentWarning, Void> actionColumn;

    @FXML private StackPane loaderContainer;
    @FXML private VBox noDataContainer;

    // Data
    private ObservableList<StudentWarning> studentWarnings = FXCollections.observableArrayList();

    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Set up navigation button handlers
        homeButton.setOnAction(this::navigateToHome);
        aboutButton.setOnAction(this::navigateToAbout);
        admissionButton.setOnAction(this::navigateToAdmission);
        examPortalButton.setOnAction(this::navigateToExamPortal);
        contactButton.setOnAction(this::navigateToContact);
        refreshButton.setOnAction(e -> loadStudentWarnings());

        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStudentId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));
        examColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExamName()));
        warningColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getWarningCount()).asObject());

        // Status column with custom styling based on warning count
        statusColumn.setCellValueFactory(cellData -> {
            int warningCount = cellData.getValue().getWarningCount();
            String status = warningCount == 3 ? "CHEATED" : "WARNING";
            return new SimpleStringProperty(status);
        });

        statusColumn.setCellFactory(column -> new TableCell<StudentWarning, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Apply styling based on warning count
                    StudentWarning warning = getTableView().getItems().get(getIndex());
                    int warningCount = warning.getWarningCount();

                    if (warningCount == 1) {
                        getStyleClass().add("warning-status-low");
                    } else if (warningCount == 2) {
                        getStyleClass().add("warning-status-medium");
                    } else if (warningCount == 3) {
                        getStyleClass().add("warning-status-high");
                    }
                }
            }
        });

        // Action column with reset button
        actionColumn.setCellFactory(createActionButtonCellFactory());

        // Set table data
        studentTable.setItems(studentWarnings);

        // Subscribe to auth state changes
        subscribeToAuthStateChanges();

        // Load student warnings
        loadStudentWarnings();
    }

    /**
     * Create a cell factory for the action column.
     * @return The cell factory.
     */
    private Callback<TableColumn<StudentWarning, Void>, TableCell<StudentWarning, Void>> createActionButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<StudentWarning, Void> call(final TableColumn<StudentWarning, Void> param) {
                return new TableCell<>() {
                    private final MFXButton expellButton = new MFXButton("Expell Student");

                    {
                        expellButton.getStyleClass().add("mfx-button-primary");
                        expellButton.setOnAction(event -> {
                            StudentWarning warning = getTableView().getItems().get(getIndex());
                            expellStudent(warning.getStudentId(), warning.getExamSessionId());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(expellButton);
                        }
                    }
                };
            }
        };
    }

    /**
     * Expell a student from an exam by deleting their exam session record.
     * @param studentId The student ID.
     * @param examSessionId The exam session ID.
     */
    private void expellStudent(int studentId, int examSessionId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM exam_sessions WHERE id = ? AND student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, examSessionId);
                stmt.setInt(2, studentId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    MFXNotifications.showSuccess("Success", "Student expelled successfully");
                    loadStudentWarnings(); // Refresh the data
                } else {
                    MFXNotifications.showError("Error", "Failed to expell student");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error expelling student", e);
            MFXNotifications.showError("Error", "Database error: " + e.getMessage());
        }
    }

    /**
     * Load student warnings from the database.
     */
    private void loadStudentWarnings() {
        // Show loader
        loaderContainer.setVisible(true);
        loaderContainer.setManaged(true);
        noDataContainer.setVisible(false);
        noDataContainer.setManaged(false);

        // Clear existing data
        studentWarnings.clear();

        // Load data in a background thread
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT es.id as session_id, es.student_id, u.first_name, u.last_name, " +
                             "qp.title as exam_name, es.warning_count " +
                             "FROM exam_sessions es " +
                             "JOIN users u ON es.student_id = u.id " +
                             "JOIN question_papers qp ON es.question_paper_id = qp.id " +
                             "WHERE es.warning_count > 0 " +
                             "ORDER BY es.warning_count DESC, u.last_name, u.first_name";

                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    boolean hasData = false;

                    while (rs.next()) {
                        hasData = true;
                        int sessionId = rs.getInt("session_id");
                        int studentId = rs.getInt("student_id");
                        String firstName = rs.getString("first_name");
                        String lastName = rs.getString("last_name");
                        String examName = rs.getString("exam_name");
                        int warningCount = rs.getInt("warning_count");

                        StudentWarning warning = new StudentWarning(
                            sessionId, studentId, firstName + " " + lastName, examName, warningCount
                        );

                        // Add to the list on the JavaFX thread
                        Platform.runLater(() -> studentWarnings.add(warning));
                    }

                    // Update UI on the JavaFX thread
                    final boolean finalHasData = hasData;
                    Platform.runLater(() -> {
                        loaderContainer.setVisible(false);
                        loaderContainer.setManaged(false);

                        if (!finalHasData) {
                            noDataContainer.setVisible(true);
                            noDataContainer.setManaged(true);
                        }
                    });
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading student warnings", e);

                // Show error on the JavaFX thread
                Platform.runLater(() -> {
                    loaderContainer.setVisible(false);
                    loaderContainer.setManaged(false);
                    MFXNotifications.showError("Error", "Database error: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Subscribe to authentication state changes.
     */
    private void subscribeToAuthStateChanges() {
        AuthStateManager.getInstance().subscribe(authState -> {
            Platform.runLater(() -> {
                boolean isAuthenticated = authState != null && authState.isAuthenticated();
                boolean isAdmin = isAuthenticated && authState.getUser() != null && "ADMIN".equals(authState.getUser().getRole());

                // Only admins should access this page
                if (!isAdmin) {
                    // Navigate back to home if not admin
                    NavigationUtil.navigateToHome(new ActionEvent());
                    MFXNotifications.showWarning("Access Denied", "Only administrators can access the exam monitoring page");
                }
            });
        });
    }

    /**
     * Called when the scene becomes active.
     */
    public void onSceneActive() {
        // Refresh data when scene becomes active
        loadStudentWarnings();
    }

    /**
     * Refresh the UI based on the current authentication state.
     */
    @Override
    public void refreshUI() {
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = currentState != null && currentState.isAuthenticated();
        boolean isAdmin = isAuthenticated && currentState.getUser() != null && "ADMIN".equals(currentState.getUser().getRole());

        // Only admins should access this page
        if (!isAdmin) {
            // Navigate back to home if not admin
            NavigationUtil.navigateToHome(new ActionEvent());
            MFXNotifications.showWarning("Access Denied", "Only administrators can access the exam monitoring page");
        }
    }

    /**
     * Get the current scene.
     * @return The scene.
     */
    private Scene getScene() {
        if (homeButton != null && homeButton.getScene() != null) {
            return homeButton.getScene();
        }
        return null;
    }

    // Navigation methods

    @FXML
    private void navigateToHome(ActionEvent event) {
        NavigationUtil.navigateToHome(event);
    }

    @FXML
    private void navigateToAbout(ActionEvent event) {
        NavigationUtil.navigateToAbout(event);
    }

    @FXML
    private void navigateToAdmission(ActionEvent event) {
        NavigationUtil.navigateToAdmission(event);
    }

    @FXML
    private void navigateToExamPortal(ActionEvent event) {
        NavigationUtil.navigateToExamPortal(event);
    }

    @FXML
    private void navigateToContact(ActionEvent event) {
        NavigationUtil.navigateToContact(event);
    }

    /**
     * Student warning data class.
     */
    public static class StudentWarning {
        private final int examSessionId;
        private final int studentId;
        private final String studentName;
        private final String examName;
        private final int warningCount;

        public StudentWarning(int examSessionId, int studentId, String studentName, String examName, int warningCount) {
            this.examSessionId = examSessionId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.examName = examName;
            this.warningCount = warningCount;
        }

        public int getExamSessionId() {
            return examSessionId;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getExamName() {
            return examName;
        }

        public int getWarningCount() {
            return warningCount;
        }
    }
}
