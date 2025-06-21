package com.ueadmission.exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.questionPaper.Question;
import com.ueadmission.questionPaper.QuestionOption;
import com.ueadmission.questionPaper.QuestionPaper;
import com.ueadmission.questionPaper.QuestionPaperDAO;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Controller for the Exam page
 * Handles user interactions and navigation for the Exam screen
 */
public class ExamController {

    private static final Logger LOGGER = Logger.getLogger(ExamController.class.getName());
    private static final Logger STATIC_LOGGER = Logger.getLogger(ExamController.class.getName());
    private Consumer<AuthState> authStateListener;

    // UI Elements - Navigation
    @FXML private MFXButton homeButton;
    @FXML private MFXButton aboutButton;
    @FXML private MFXButton admissionButton;
    @FXML private MFXButton examPortalButton;
    @FXML private MFXButton contactButton;
    @FXML private MFXButton loginButton;

    // Authentication UI elements
    @FXML private HBox loginButtonContainer;
    @FXML private HBox profileButtonContainer;
    @FXML private ProfileButton profileButton;

    // Exam UI Elements
    @FXML private VBox examInfoSection;
    @FXML private VBox questionsSection;
    @FXML private Label schoolNameLabel;
    @FXML private Label timeRemainingLabel;
        @FXML private Label totalMarksLabel;
    @FXML private VBox questionListContainer;
    @FXML private MFXButton submitButton;

    // Exam Data
    private String selectedSchool;
    private static String selectedSchoolStatic;
    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> userAnswers = new HashMap<>();
    private Timer countdownTimer;
    private int remainingTimeInSeconds;
    private int totalMarks;
    private boolean showingResults = false;

    @FXML
    public void initialize() {
        // Configure navigation buttons
        homeButton.setOnAction(event -> navigateToHome(event));
        aboutButton.setOnAction(event -> navigateToAbout(event));
        examPortalButton.setOnAction(event -> navigateToExamPortal(event));
        contactButton.setOnAction(event -> navigateToContact(event));

        // Configure login button if it exists
        if (loginButton != null) {
            loginButton.setOnAction(event -> navigateToLogin(event));
        }

        // Initialize containers by looking them up in the scene
        javafx.application.Platform.runLater(() -> {
            try {
                initializeContainersIfNull();
                LOGGER.info("Initialized containers on startup");

                // After initialization, immediately update UI with current auth state
                refreshUI();

                // Initialize exam UI
                initializeExamUI();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error initializing containers during startup: {0}", e.getMessage());
            }
        });

        // Configure admission button with authentication check
        if (admissionButton != null) {
            admissionButton.setOnAction(event -> {
                if (AuthStateManager.getInstance().isAuthenticated()) {
                    navigateToAdmission(event);
                } else {
                    navigateToLogin(event);
                }
            });
        }

        // Subscribe to auth state changes
        subscribeToAuthStateChanges();

        // Refresh UI with current auth state
        refreshUI();

        // Call onSceneActive to ensure UI is updated when scene is shown
        javafx.application.Platform.runLater(this::onSceneActive);
    }

    /**
     * Initialize the exam UI
     */
    private void initializeExamUI() {
        // Extract school from URL parameters or use default
        extractSchoolFromExamPortal();

        // Set exam parameters based on school
        setExamParametersForSchool(selectedSchool);

        // Retrieve questions for the selected school
        retrieveQuestionsForSchool(selectedSchool);

        // Display questions
        displayQuestionsWithWebView();

        // Start countdown timer
        startCountdownTimer();
    }

    /**
     * Extract school from exam portal URL parameters
     */
    private void extractSchoolFromExamPortal() {
        // Check if we have a school name in the static field
        if (selectedSchoolStatic != null && !selectedSchoolStatic.isEmpty()) {
            selectedSchool = selectedSchoolStatic;
            schoolNameLabel.setText(selectedSchool);
            LOGGER.info("Using school from exam portal: " + selectedSchool);
        } else {
            // Default to Engineering if no school is set
            selectedSchool = "School of Engineering & Technology";
            schoolNameLabel.setText(selectedSchool);
            LOGGER.info("No school set by exam portal, using default: " + selectedSchool);
        }
    }

    /**
     * Set static selected school (for use in static methods)
     */
    public static void setSelectedSchoolStatic(String school) {
        selectedSchoolStatic = school;
        STATIC_LOGGER.info("Static selected school set to: " + selectedSchoolStatic);
    }

    /**
     * Set selected school
     */
    public void setSelectedSchool(String school) {
        this.selectedSchool = school;
        schoolNameLabel.setText(school);
        LOGGER.info("Selected school set to: " + school);
    }

    /**
     * Set exam parameters based on school
     */
    private void setExamParametersForSchool(String school) {
        // Set time limit and total marks based on school
        if (school.contains("Engineering")) {
            remainingTimeInSeconds = 75 * 60; // 75 minutes
            totalMarks = 75;
        } else {
            remainingTimeInSeconds = 75 * 60; // Default to 75 minutes
            totalMarks = 75; // Default to 75 marks
        }

        // Update timer display
        updateTimerDisplay();

        // Update total marks display
        totalMarksLabel.setText(String.valueOf(totalMarks));
    }

    /**
     * Retrieve questions for the selected school
     */
    private void retrieveQuestionsForSchool(String school) {
        try {
            // Get the most recent question paper for this school that is not a mock exam
            QuestionPaper questionPaper = QuestionPaperDAO.getMostRecentQuestionPaper(false, school);

            if (questionPaper != null) {
                // Get questions
                questions = QuestionPaperDAO.getQuestionsForPaper(questionPaper.getId());
                LOGGER.info("Retrieved " + questions.size() + " questions for " + school);

                // Update total marks based on the number of questions
                totalMarks = questions.size();
                totalMarksLabel.setText(String.valueOf(totalMarks));
                LOGGER.info("Total marks set to: " + totalMarks);

                // Shuffle questions to randomize order
                shuffleQuestions();
            } else {
                LOGGER.warning("No question paper found for " + school);
                // Use sample questions for testing
                questions = new ArrayList<>();
                // Add sample questions here if needed
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving questions: " + e.getMessage(), e);
        }
    }

    /**
     * Shuffle questions to randomize order
     */
    private void shuffleQuestions() {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        // Use Collections.shuffle to randomize the order
        java.util.Collections.shuffle(questions);
        LOGGER.info("Questions shuffled");
    }

    /**
     * Display questions using WebView
     */
    private void displayQuestionsWithWebView() {
        // Clear existing questions
        questionListContainer.getChildren().clear();

        // Add each question
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            WebView webView = createQuestionWebView(question, false, i + 1);
            questionListContainer.getChildren().add(webView);
        }
    }

    /**
     * Create a WebView for displaying a question with LaTeX support
     * @param question The question to display
     * @param showResults Whether to show correct answers
     * @param questionNumber The sequential number of the question
     * @return A WebView containing the question and options
     */
    private WebView createQuestionWebView(Question question, boolean showResults, int questionNumber) {
        WebView webView = new WebView();
        webView.getStyleClass().add("web-view");
        webView.setPrefWidth(500); // Match MockTestController width
        webView.setPrefHeight(500); // Match MockTestController height

        WebEngine webEngine = webView.getEngine();

        // Add subject as a data attribute for reference
        String subject = question.getSubject();
        if (subject != null && !subject.isEmpty()) {
            webView.getProperties().put("subject", subject);
        }

        // Build HTML content
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html><head>");
        htmlBuilder.append("<meta charset='UTF-8'>");

        // Add subject label if available
        if (subject != null && !subject.isEmpty()) {
            htmlBuilder.append("<div style='background-color: #f0f0f0; padding: 5px; margin-bottom: 10px; border-radius: 4px; font-size: 12px;'>Subject: " + subject + "</div>");
        }

        // Add MathJax for LaTeX support
        htmlBuilder.append("<script type='text/javascript' id='MathJax-script' async ");
        htmlBuilder.append("src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js'></script>");
        htmlBuilder.append("<script type='text/x-mathjax-config'>");
        htmlBuilder.append("MathJax.Hub.Config({tex2jax: {inlineMath: [['\\\\(','\\\\)']], displayMath: [['\\\\[','\\\\]']]},");
        htmlBuilder.append("'HTML-CSS': {linebreaks: {automatic: true}},");
        htmlBuilder.append("CommonHTML: {linebreaks: {automatic: true}},");
        htmlBuilder.append("SVG: {linebreaks: {automatic: true}}");
        htmlBuilder.append("});</script>");

        htmlBuilder.append("<script type='text/javascript'>");
        htmlBuilder.append("function handleRadioClick(radio, questionId, optionText) {");
        htmlBuilder.append("  console.log('Radio clicked: ' + questionId + ' - ' + optionText);");
        htmlBuilder.append("  // Find all option containers in this question");
        htmlBuilder.append("  var optionContainers = document.querySelectorAll('.option');");
        htmlBuilder.append("  // Remove selected class from all options");
        htmlBuilder.append("  for (var i = 0; i < optionContainers.length; i++) {");
        htmlBuilder.append("    optionContainers[i].classList.remove('selected-option');");
        htmlBuilder.append("  }");
        htmlBuilder.append("  // Add selected class to the parent of the selected radio");
        htmlBuilder.append("  radio.parentNode.classList.add('selected-option');");
        htmlBuilder.append("}");
        htmlBuilder.append("</script>");

        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 10px; font-size: 16px; background-color: #fff; }");
        htmlBuilder.append(".question-container { margin-bottom: 15px; border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; }");
        htmlBuilder.append(".question-number { font-weight: bold; font-size: 18px; color: #FA4506; }");
        htmlBuilder.append(".question-text { font-weight: bold; font-size: 16px; margin-bottom: 10px; }");
        htmlBuilder.append(".question-image { max-width: 300px; max-height: 200px; width: auto; height: auto; display: block; margin: 10px auto; }");
        htmlBuilder.append(".options-container { margin-top: 10px; }");
        htmlBuilder.append(".option { margin: 5px 0; padding: 5px; display: flex; align-items: center; padding: 8px; border-radius: 4px; transition: all 0.3s; }");
        htmlBuilder.append(".option-radio { margin-right: 10px; cursor: pointer; width: 18px; height: 18px; }");
        htmlBuilder.append(".option-radio:checked { accent-color: #fa4506; }");
        htmlBuilder.append(".option-radio:hover { cursor: pointer; }");
        htmlBuilder.append(".option:hover { background-color: #f5f5f5; cursor: pointer; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        htmlBuilder.append(".selected-option { background-color: #f0f0f0; border-left: 4px solid #2196F3; padding-left: 12px; }");
        htmlBuilder.append(".mjx-chtml { display: inline-block !important; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head><body>");

        // Question container
        htmlBuilder.append("<div class='question-container'>");

        // Question number and text
        htmlBuilder.append("<span class='question-number'>Question ").append(questionNumber).append(". </span>");

        // Process question text for LaTeX if needed
        String questionText = question.getQuestionText();
        if (question.isHasLatex()) {
            // Replace single $ with \( and \) for inline math
            questionText = questionText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");
            // Replace double $$ with \[ and \] for display math
            questionText = questionText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
        }
        htmlBuilder.append("<span class='question-text'>").append(questionText).append("</span>");

        // Add image if present
        if (question.isHasImage() && question.getImagePath() != null && !question.getImagePath().isEmpty()) {
            String imagePath = question.getImagePath();
            if (imagePath.startsWith("http")) {
                htmlBuilder.append("<div><img src='").append(imagePath).append("' class='question-image' alt='Question Image'></div>");
            }
        }

        // Options container
        htmlBuilder.append("<div class='options-container'>");

        // Get user's answer for this question
        String userAnswer = userAnswers.get(question.getId());

        // Add options with radio buttons
        List<QuestionOption> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            QuestionOption option = options.get(i);
            String optionText = option.getOptionText();
            boolean isSelected = optionText.equals(userAnswer);

            // Determine option class
            String optionClass = isSelected ? "option selected-option" : "option";

            htmlBuilder.append("<div class='").append(optionClass).append("'>");

            // Add radio button
            String checkedAttr = isSelected ? "checked" : "";
            htmlBuilder.append("<input type='radio' name='question-")
                    .append(question.getId())
                    .append("' class='option-radio' onclick='handleRadioClick(this, \"")
                    .append(question.getId()).append("\", \"")
                    .append(optionText.replace("\"", "\\\"")).append("\")' ")
                    .append(checkedAttr).append(">");

            // Process option text for LaTeX if question has LaTeX
            if (question.isHasLatex()) {
                // Replace single $ with \( and \) for inline math
                optionText = optionText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");
                // Replace double $$ with \[ and \] for display math
                optionText = optionText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
            }

            htmlBuilder.append(optionText);
            htmlBuilder.append("</div>");
        }

        htmlBuilder.append("</div>"); // Close options container
        htmlBuilder.append("</div>"); // Close question container

        htmlBuilder.append("</body></html>");

        // Load HTML content
        webEngine.loadContent(htmlBuilder.toString());

        // Setup click handler
        setupWebViewClickHandler(webView, question);

        return webView;
    }

    /**
     * Setup click handler for WebView options
     */
    private void setupWebViewClickHandler(WebView webView, Question question) {
        webView.setOnMouseClicked(event -> {
            if (showingResults) {
                return; // Don't allow changes when showing results
            }

            // Get the WebEngine for checking radio button states
            WebEngine engine = webView.getEngine();

            // We can't directly access the radio buttons, so we'll need to poll them
            // Schedule a task to check which radio button is selected after a short delay
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        // For each option, check if its radio button is selected
                        for (int i = 0; i < question.getOptions().size(); i++) {
                            QuestionOption option = question.getOptions().get(i);
                            String checkScript = "document.querySelectorAll('input[name=\"question-" + question.getId() + "\"]')[" + i + "].checked;";
                            Boolean checked = (Boolean) engine.executeScript(checkScript);

                            if (checked) {
                                userAnswers.put(question.getId(), option.getOptionText());
                                LOGGER.info("User selected answer for question " + question.getId() + ": " + option.getOptionText());
                                break;
                            } else if (i == question.getOptions().size() - 1) {
                                // If we've checked all options and none are selected, remove the answer
                                userAnswers.remove(question.getId());
                            }
                        }
                    });
                }
            }, 100); // Short delay to allow the radio button state to update
        });
    }

    /**
     * Handle submit answers button click
     */
    @FXML
    public void submitAnswers() {
        // Stop the timer
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }

        try {
            // Get current user ID from auth state
            AuthState authState = AuthStateManager.getInstance().getState();
            if (authState == null || !authState.isAuthenticated() || authState.getUser() == null) {
                LOGGER.warning("User not authenticated, cannot submit exam");
                MFXNotifications.showError("Authentication Error", "You must be logged in to submit the exam");
                return;
            }

            int studentId = authState.getUser().getId();

            // Get question paper ID
            List<QuestionPaper> allPapers = QuestionPaperDAO.getAllQuestionPapers();
            QuestionPaper questionPaper = null;

            // Find the question paper for the selected school
            for (QuestionPaper paper : allPapers) {
                if (paper.getSchool().equals(selectedSchool) && !paper.isMockExam()) {
                    questionPaper = paper;
                    break;
                }
            }

            if (questionPaper == null) {
                LOGGER.warning("No question paper found for " + selectedSchool);
                MFXNotifications.showError("Error", "No question paper found for " + selectedSchool);
                return;
            }

            // Store exam session in database
            Connection connection = null;
            PreparedStatement sessionStmt = null;
            PreparedStatement responseStmt = null;

            try {
                // Get database connection
                connection = DatabaseConnection.getConnection();

                // Begin transaction
                connection.setAutoCommit(false);

                // Calculate score
                int correctAnswers = 0;
                for (Map.Entry<Integer, String> entry : userAnswers.entrySet()) {
                    int questionId = entry.getKey();
                    String selectedOptionText = entry.getValue();

                    // Find the question and check if the answer is correct
                    for (Question q : questions) {
                        if (q.getId() == questionId) {
                            for (QuestionOption option : q.getOptions()) {
                                if (option.getOptionText().equals(selectedOptionText) && option.isCorrect()) {
                                    correctAnswers++;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                // Calculate score as a decimal
                double score = (double) correctAnswers;
                double maxScore = (double) totalMarks;

                LOGGER.info("Calculated score: " + score + " out of " + maxScore);

                // Create exam session with score
                String sessionSql = "INSERT INTO exam_sessions (student_id, question_paper_id, end_time, status, score, max_score) VALUES (?, ?, NOW(), 'completed', ?, ?)";
                sessionStmt = connection.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS);
                sessionStmt.setInt(1, studentId);
                sessionStmt.setInt(2, questionPaper.getId());
                sessionStmt.setDouble(3, score);
                sessionStmt.setDouble(4, maxScore);
                sessionStmt.executeUpdate();

                // Get generated session ID
                ResultSet generatedKeys = sessionStmt.getGeneratedKeys();
                int sessionId = -1;
                if (generatedKeys.next()) {
                    sessionId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get session ID");
                }

                // Store student responses
                String responseSql = "INSERT INTO student_responses (student_id, question_id, selected_option_id, is_correct) VALUES (?, ?, ?, ?)";
                responseStmt = connection.prepareStatement(responseSql);

                for (Map.Entry<Integer, String> entry : userAnswers.entrySet()) {
                    int questionId = entry.getKey();
                    String selectedOptionText = entry.getValue();

                    // Find the selected option ID and whether it's correct
                    Question question = null;
                    for (Question q : questions) {
                        if (q.getId() == questionId) {
                            question = q;
                            break;
                        }
                    }

                    if (question != null) {
                        List<QuestionOption> options = question.getOptions();
                        for (QuestionOption option : options) {
                            if (option.getOptionText().equals(selectedOptionText)) {
                                // Set parameters
                                responseStmt.setInt(1, studentId);
                                responseStmt.setInt(2, questionId);
                                responseStmt.setInt(3, option.getId());
                                responseStmt.setBoolean(4, option.isCorrect());
                                responseStmt.addBatch();
                                break;
                            }
                        }
                    }
                }

                // Execute batch
                responseStmt.executeBatch();

                // Commit transaction
                connection.commit();

                // Calculate score percentage
                double scorePercentage = maxScore > 0 ? (score * 100.0 / maxScore) : 0;

                // Show success message without score
                String title = "Exam Submitted Successfully";
                MFXNotifications.showSuccess(title, "Your exam has been submitted successfully. Results will be published later.");

                // Clear the question list container
                questionListContainer.getChildren().clear();

                // Add success message without score
                VBox successBox = new VBox(20);
                successBox.setAlignment(javafx.geometry.Pos.CENTER);
                successBox.setPadding(new Insets(30));
                successBox.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 10;");

                Label successTitle = new Label("Exam Submitted Successfully");
                successTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

                Label successMessage = new Label("Thank you for completing the exam. Your responses have been recorded.");
                successMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333; -fx-wrap-text: true;");

                Label resultInfo = new Label("Results will be published later.");
                resultInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555; -fx-wrap-text: true;");

                MFXButton returnButton = new MFXButton("Return to Exam Portal");
                returnButton.getStyleClass().add("mfx-button-primary");
                returnButton.setOnAction(event -> navigateToExamPortal(event));

                successBox.getChildren().addAll(successTitle, successMessage, resultInfo, returnButton);
                questionListContainer.getChildren().add(successBox);

                // Update submit button
                submitButton.setVisible(false);
                submitButton.setManaged(false);

            } catch (SQLException e) {
                // Rollback transaction on error
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, "Error rolling back transaction: " + ex.getMessage(), ex);
                    }
                }

                LOGGER.log(Level.SEVERE, "Error storing exam data: " + e.getMessage(), e);
                MFXNotifications.showError("Error", "Failed to submit exam: " + e.getMessage());
            } finally {
                // Close resources
                if (responseStmt != null) {
                    try {
                        responseStmt.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error closing statement: " + e.getMessage(), e);
                    }
                }

                if (sessionStmt != null) {
                    try {
                        sessionStmt.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error closing statement: " + e.getMessage(), e);
                    }
                }

                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error closing connection: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting exam: " + e.getMessage(), e);
            MFXNotifications.showError("Error", "Failed to submit exam: " + e.getMessage());
        }
    }

    /**
     * Start countdown timer
     */
    private void startCountdownTimer() {
        // Cancel existing timer if any
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        // Create new timer
        countdownTimer = new Timer(true);

        // Schedule timer task
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingTimeInSeconds > 0) {
                    remainingTimeInSeconds--;

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> updateTimerDisplay());
                } else {
                    // Time's up - submit answers automatically
                    Platform.runLater(() -> submitAnswers());

                    // Cancel timer
                    this.cancel();
                }
            }
        }, 0, 1000); // Update every second
    }

    /**
     * Update timer display
     */
    private void updateTimerDisplay() {
        // Calculate minutes and seconds
        int minutes = remainingTimeInSeconds / 60;
        int seconds = remainingTimeInSeconds % 60;

        // Update label
        timeRemainingLabel.setText(String.format("%02d:%02d", minutes, seconds));

        // Change color to red when less than 5 minutes remaining
        if (remainingTimeInSeconds < 300) {
            timeRemainingLabel.setStyle("-fx-text-fill: red;");
        } else {
            timeRemainingLabel.setStyle("-fx-text-fill: -primary-color;");
        }
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Exam screen
     */
    void cleanup() {
        LOGGER.info("Cleaning up ExamController before navigation");

        // Reset opacity on the scene root if available
        if (homeButton != null && homeButton.getScene() != null && 
                homeButton.getScene().getRoot() != null) {
            homeButton.getScene().getRoot().setOpacity(1.0);
            LOGGER.info("Reset opacity to 1.0 during cleanup");
        }

        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
            LOGGER.info("Unsubscribed from auth state changes during cleanup");
        }
    }

    /**
     * Subscribe to authentication state changes to update UI
     */
    private void subscribeToAuthStateChanges() {
        // Create auth state listener
        authStateListener = newState -> {
            LOGGER.info("Auth state change detected in ExamController");

            boolean isAuthenticated = (newState != null && newState.isAuthenticated() && newState.getUser() != null);
            String userEmail = "none";
            if (isAuthenticated && newState != null && newState.getUser() != null) {
                userEmail = newState.getUser().getEmail();
            }
            LOGGER.log(Level.INFO, "Auth state changed - authenticated: {0}, user: {1}", 
                new Object[]{isAuthenticated, userEmail});

            // Ensure we're on the JavaFX Application Thread for UI updates
            if (!javafx.application.Platform.isFxApplicationThread()) {
                javafx.application.Platform.runLater(() -> {
                    // Update UI based on new state
                    updateContainersVisibility(isAuthenticated);

                    // Update profile button if authenticated
                    if (profileButton != null) {
                        profileButton.updateUIFromAuthState(newState);
                        LOGGER.info("Updated profile button from auth state listener");
                    }
                });
            } else {
                // Update UI based on new state
                updateContainersVisibility(isAuthenticated);

                // Update profile button if authenticated
                if (profileButton != null) {
                    profileButton.updateUIFromAuthState(newState);
                    LOGGER.info("Updated profile button from auth state listener");
                }
            }
        };

        // Subscribe to auth state changes
        AuthStateManager.getInstance().subscribe(authStateListener);
        LOGGER.info("Subscribed to auth state changes in ExamController");

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
        LOGGER.info("Exam scene became active, refreshing auth UI");

        // Check authentication status first
        boolean isAuthenticated = AuthStateManager.getInstance().isAuthenticated();
        LOGGER.log(Level.INFO, "Authentication status on scene activation: {0}", isAuthenticated);

        // Try to initialize containers if they are null - do this first to ensure we have the containers
        try {
            initializeContainersIfNull();
            LOGGER.info("Containers initialized during scene activation");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error initializing containers on scene activation: {0}", e.getMessage());
        }

        // Refresh UI with current auth state
        refreshUI();

        // Check authentication as this is a private route
        if (!isAuthenticated) {
            LOGGER.info("User not authenticated on scene activation, redirecting to login");
            javafx.application.Platform.runLater(() -> {
                // Create a dummy ActionEvent with the homeButton as the source
                ActionEvent event = new ActionEvent(homeButton, ActionEvent.NULL_SOURCE_TARGET);
                navigateToLogin(event);
            });
        }
    }

    /**
     * Manually refresh the UI state based on current auth state
     */
    public void refreshUI() {
        LOGGER.info("Refreshing ExamController UI");

        // Get the current auth state
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated()
                && currentState.getUser() != null);

        LOGGER.log(Level.INFO, "Current auth state in refreshUI: {0}", isAuthenticated);

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
                LOGGER.info("Updated profile button in Exam page");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating UI components in ExamController: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update containers visibility based on authentication status
     */
    private void updateContainersVisibility(boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Try to initialize containers if they are null first to ensure we have the containers
                initializeContainersIfNull();

                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    LOGGER.log(Level.INFO, "Login button container visibility set to: {0}", !isAuthenticated);
                }

                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    LOGGER.log(Level.INFO, "Profile button container visibility set to: {0}", isAuthenticated);
                }

                // Update profile button if we have it
                if (profileButton != null && isAuthenticated) {
                    profileButton.refreshAuthState();
                    LOGGER.info("Refreshed profile button during container update");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating containers in ExamController: {0}", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Try to initialize the containers if they are null
     */
    private void initializeContainersIfNull() {
        try {
            javafx.scene.Scene scene = getScene();
            if (scene == null) {
                LOGGER.warning("Scene is null, cannot initialize containers");
                return;
            }

            // Find loginButtonContainer if it's null
            if (loginButtonContainer == null) {
                Node node = scene.lookup("#loginButtonContainer");
                if (node instanceof HBox) {
                    loginButtonContainer = (HBox) node;
                    LOGGER.info("Initialized loginButtonContainer via scene lookup: " + node.getId());
                }
            }

            // Find profileButtonContainer if it's null
            if (profileButtonContainer == null) {
                Node node = scene.lookup("#profileButtonContainer");
                if (node instanceof HBox) {
                    profileButtonContainer = (HBox) node;
                    LOGGER.info("Initialized profileButtonContainer via scene lookup: " + node.getId());
                }
            }

            // Try to find profile button if it's null
            if (profileButton == null) {
                Node node = scene.lookup(".profile-button-container");
                if (node instanceof ProfileButton) {
                    profileButton = (ProfileButton) node;
                    LOGGER.info("Initialized profileButton via scene lookup with class");

                    // Initialize the profile button with current auth state
                    profileButton.refreshAuthState();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing containers: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the current scene
     * @return The current scene or null if not available
     */
    private javafx.scene.Scene getScene() {
        // Try different UI elements to get the scene
        if (homeButton != null && homeButton.getScene() != null) {
            return homeButton.getScene();
        } else if (loginButton != null && loginButton.getScene() != null) {
            return loginButton.getScene();
        } else if (profileButton != null && profileButton.getScene() != null) {
            return profileButton.getScene();
        }

        // No scene found
        return null;
    }

    /**
     * Navigates to the Home screen
     * @param event The event that triggered this action
     */
    private void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the About screen
     * @param event The event that triggered this action
     */
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigates to the Admission screen
     * @param event The event that triggered this action
     */
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigates to the Exam Portal screen
     * @param event The event that triggered this action
     */
    private void navigateToExamPortal(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Navigates to the Contact screen
     * @param event The event that triggered this action
     */
    private void navigateToContact(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }

    /**
     * Navigates to the Login screen
     * @param event The event that triggered this action
     */
    private void navigateToLogin(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }

    /**
     * Handle mouse click navigation to Home in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToHome(javafx.scene.input.MouseEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Handle mouse click navigation to About in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToAbout(javafx.scene.input.MouseEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Handle mouse click navigation to Admission in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToAdmission(javafx.scene.input.MouseEvent event) {
        // Check authentication before navigating
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to login");
            NavigationUtil.navigateToLogin(event);
            return;
        }

        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Handle mouse click navigation to Exam Portal in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToExamPortal(javafx.scene.input.MouseEvent event) {
        // Check authentication before navigating
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to login");
            NavigationUtil.navigateToLogin(event);
            return;
        }

        cleanup();
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Handle mouse click navigation to Contact in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToContact(javafx.scene.input.MouseEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }
}
