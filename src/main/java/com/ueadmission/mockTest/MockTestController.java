package com.ueadmission.mockTest;

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
 * Controller for the Mock Test page
 * Handles user interactions and navigation for the Mock Test screen
 */
public class MockTestController {

    private static final Logger LOGGER = Logger.getLogger(MockTestController.class.getName());
    private static final Logger STATIC_LOGGER = Logger.getLogger(MockTestController.class.getName());
    private Consumer<AuthState> authStateListener;

    // Mock Test UI Elements
    @FXML private VBox examInfoSection;
    @FXML private VBox questionsSection;
    @FXML private Label schoolNameLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private Label totalMarksLabel;
    @FXML private VBox questionListContainer;
    @FXML private MFXButton submitButton;

    // Mock Test Data
    private String selectedSchool;
    private static String selectedSchoolStatic;
    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> userAnswers = new HashMap<>();
    private Timer countdownTimer;
    private int remainingTimeInSeconds;
    private int totalMarks;
    private boolean showingResults = false;

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

                // Initialize mock test UI
                initializeMockTestUI();
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
     * Initialize the mock test UI elements
     */
    private void initializeMockTestUI() {
        // Extract the school name from the exam card that was clicked in the exam portal
        extractSchoolFromExamPortal();

        // If no school is selected, default to Engineering
        if (selectedSchool == null || selectedSchool.isEmpty()) {
            selectedSchool = "School of Engineering & Technology";
            LOGGER.info("No school selected, defaulting to: " + selectedSchool);
        }

        // Update UI with school name
        if (schoolNameLabel != null) {
            schoolNameLabel.setText(selectedSchool);
        }

        // Initialize sections visibility
        if (examInfoSection != null) {
            examInfoSection.setVisible(true);
            examInfoSection.setManaged(true);
        }

        if (questionsSection != null) {
            questionsSection.setVisible(true);
            questionsSection.setManaged(true);
        }

        if (submitButton != null) {
            submitButton.setVisible(true);
            submitButton.setManaged(true);
        }

        // Start the exam automatically
        startExam();

        LOGGER.info("Mock test UI initialized");
    }

    /**
     * Extract the school name from the exam portal
     * This uses a static field that can be set by the ExamPortalController
     */
    private void extractSchoolFromExamPortal() {
        // Check if we have a school name in the static field
        if (selectedSchoolStatic != null && !selectedSchoolStatic.isEmpty()) {
            selectedSchool = selectedSchoolStatic;
            LOGGER.info("Using school from exam portal: " + selectedSchool);
        } else {
            // Default to Engineering if no school is set
            LOGGER.info("No school set by exam portal, using default");
        }
    }

    /**
     * Set the selected school (static method that can be called from ExamPortalController)
     * @param school The selected school
     */
    public static void setSelectedSchoolStatic(String school) {
        selectedSchoolStatic = school;
        STATIC_LOGGER.info("Static school set to: " + selectedSchoolStatic);
    }

    /**
     * Start the exam for the selected school
     * Retrieves questions for the selected school and displays them
     */
    public void startExam() {
        if (selectedSchool == null || selectedSchool.isEmpty()) {
            MFXNotifications.showError("Error", "No school selected");
            return;
        }

        LOGGER.info("Starting exam for school: " + selectedSchool);

        // Update UI
        schoolNameLabel.setText(selectedSchool);

        // Set exam parameters based on school
        setExamParametersForSchool(selectedSchool);

        // Retrieve questions for the selected school
        retrieveQuestionsForSchool(selectedSchool);

        // Display questions using WebView
        displayQuestionsWithWebView();

        // Start countdown timer
        startCountdownTimer();
    }

    /**
     * Set the selected school (called from ExamPortalController)
     * @param school The selected school
     */
    public void setSelectedSchool(String school) {
        this.selectedSchool = school;
        LOGGER.info("School set to: " + selectedSchool);
    }

    /**
     * Set exam parameters (time limit, total marks) based on selected school
     * @param school The selected school
     */
    private void setExamParametersForSchool(String school) {
        // Default values for mock exam
        int timeLimitMinutes = 75;
        totalMarks = 75;

        // Update UI
        totalMarksLabel.setText(String.valueOf(totalMarks));

        // Set remaining time in seconds
        remainingTimeInSeconds = timeLimitMinutes * 60;
        updateTimerDisplay();
    }

    /**
     * Retrieve questions for the selected school from the database
     * @param school The selected school
     */
    private void retrieveQuestionsForSchool(String school) {
        // Clear previous questions
        questions.clear();

        try {
            // Get mock exam question papers for the selected school
            List<QuestionPaper> papers = QuestionPaperDAO.getAllQuestionPapers();
            QuestionPaper mockPaper = null;

            // Find a mock exam paper for the selected school
            for (QuestionPaper paper : papers) {
                if (paper.getSchool().equals(school) && paper.isMockExam()) {
                    mockPaper = paper;
                    break;
                }
            }

            // If no mock paper found, show error
            if (mockPaper == null) {
                LOGGER.warning("No mock exam found for school: " + school);
                MFXNotifications.showError("Error", "No mock exam found for " + school);
                return;
            }

            // Get questions for the paper
            questions = QuestionPaperDAO.getQuestionsForPaper(mockPaper.getId());
            LOGGER.info("Retrieved " + questions.size() + " questions for school: " + school);

            // If no questions found, show error
            if (questions.isEmpty()) {
                MFXNotifications.showWarning("Warning", "No questions found for this mock exam");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving questions", e);
            MFXNotifications.showError("Error", "Failed to retrieve questions: " + e.getMessage());
        }
    }

    /**
     * Display questions using WebView with LaTeX support in a two-column layout
     */
    private void displayQuestionsWithWebView() {
        // Clear previous content
        questionListContainer.getChildren().clear();

        // If no questions, show message
        if (questions.isEmpty()) {
            Label noQuestionsLabel = new Label("No questions available for this mock exam.");
            noQuestionsLabel.getStyleClass().add("placeholder-text");
            questionListContainer.getChildren().add(noQuestionsLabel);
            return;
        }

        // Create a two-column layout using GridPane
        javafx.scene.layout.GridPane gridPane = new javafx.scene.layout.GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);

        // Set column constraints for equal width columns
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setPercentWidth(50);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col1, col2);

        // Add the grid to the container
        questionListContainer.getChildren().add(gridPane);

        // Prepare all questions in a flat list
        List<Question> allQuestions = new ArrayList<>(questions);

        // Calculate how many questions should go in each column
        int totalQuestions = allQuestions.size();
        int questionsPerColumn = (totalQuestions + 1) / 2; // Ceiling division to ensure all questions fit

        // Add questions to the grid
        for (int i = 0; i < totalQuestions; i++) {
            Question question = allQuestions.get(i);

            // Determine column and row
            int column = i < questionsPerColumn ? 0 : 1;
            int row = i < questionsPerColumn ? i : i - questionsPerColumn;

            // Create and add the question WebView
            WebView webView = createQuestionWebView(question, showingResults);
            setupWebViewClickHandler(webView, question);
            gridPane.add(webView, column, row, 1, 1);
        }
    }

    /**
     * Create a WebView for displaying a question with LaTeX support
     * @param question The question to display
     * @param showResults Whether to show correct answers
     * @return A WebView containing the question and options
     */
    private WebView createQuestionWebView(Question question, boolean showResults) {
        // Create a WebView for the question
        WebView webView = new WebView();
        webView.setPrefWidth(400);
        webView.setPrefHeight(300);
        WebEngine engine = webView.getEngine();

        // Build HTML content for the question
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html><html><head>");
        htmlContent.append("<meta charset='UTF-8'>");
        htmlContent.append("<script type='text/javascript' id='MathJax-script' async ");
        htmlContent.append("src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js'></script>");
        htmlContent.append("<script type='text/x-mathjax-config'>");
        htmlContent.append("MathJax.Hub.Config({tex2jax: {inlineMath: [['\\\\(','\\\\)']], displayMath: [['\\\\[','\\\\]']]},");
        htmlContent.append("'HTML-CSS': {linebreaks: {automatic: true}},");
        htmlContent.append("CommonHTML: {linebreaks: {automatic: true}},");
        htmlContent.append("SVG: {linebreaks: {automatic: true}}");
        htmlContent.append("});</script>");
        htmlContent.append("<style>");
        htmlContent.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 10px; font-size: 16px; background-color: #fff; }");
        htmlContent.append(".question-container { margin-bottom: 15px; border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; }");
        htmlContent.append(".question-number { font-weight: bold; font-size: 18px; color: #FA4506; }");
        htmlContent.append(".question-text { font-weight: bold; font-size: 16px; margin-bottom: 10px; }");
        htmlContent.append(".question-image { max-width: 300px; max-height: 200px; width: auto; height: auto; display: block; margin: 10px auto; }");
        htmlContent.append(".options-container { margin-top: 10px; }");
        htmlContent.append(".option { margin: 5px 0; padding: 5px; }");
        htmlContent.append(".option-checkbox { margin-right: 10px; }");
        htmlContent.append(".option-checkbox:checked { accent-color: #fa4506; }"); // Primary color from common.css
        htmlContent.append(".correct-option { color: #4CAF50; font-weight: bold; }");
        htmlContent.append(".selected-option { background-color: #f0f0f0; }");
        htmlContent.append(".correct-and-selected { background-color: #e8f5e9; }");
        htmlContent.append(".incorrect-selected { background-color: #ffebee; }");
        htmlContent.append(".mjx-chtml { display: inline-block !important; }");
        htmlContent.append("</style>");
        htmlContent.append("</head><body>");

        // Question container
        htmlContent.append("<div class='question-container'>");

        // Question number and text
        htmlContent.append("<span class='question-number'>Question ").append(question.getId()).append(". </span>");

        // Process question text for LaTeX if needed
        String questionText = question.getQuestionText();
        if (question.isHasLatex()) {
            // Replace single $ with \( and \) for inline math
            questionText = questionText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");
            // Replace double $$ with \[ and \] for display math
            questionText = questionText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
        }
        htmlContent.append("<span class='question-text'>").append(questionText).append("</span>");

        // Add image if present
        if (question.isHasImage() && question.getImagePath() != null && !question.getImagePath().isEmpty()) {
            String imagePath = question.getImagePath();
            if (imagePath.startsWith("http")) {
                htmlContent.append("<div><img src='").append(imagePath).append("' class='question-image' alt='Question Image'></div>");
            }
        }

        // Options container
        htmlContent.append("<div class='options-container'>");

        // Get user's answer for this question
        String userAnswer = userAnswers.get(question.getId());
        QuestionOption correctOption = question.getCorrectOption();
        String correctOptionText = correctOption != null ? correctOption.getOptionText() : "";

        // Add options with checkboxes
        for (QuestionOption option : question.getOptions()) {
            String optionText = option.getOptionText();
            boolean isCorrect = option.isCorrect();
            boolean isSelected = optionText.equals(userAnswer);

            // Determine option class based on whether we're showing results
            String optionClass = "option";
            if (showResults) {
                if (isCorrect && isSelected) {
                    optionClass = "option correct-and-selected";
                } else if (isCorrect) {
                    optionClass = "option correct-option";
                } else if (isSelected) {
                    optionClass = "option incorrect-selected";
                }
            } else if (isSelected) {
                optionClass = "option selected-option";
            }

            htmlContent.append("<div class='").append(optionClass).append("'>");

            // Add checkbox
            String checkedAttr = isSelected ? "checked" : "";
            htmlContent.append("<input type='checkbox' class='option-checkbox' onclick='handleCheckboxClick(this, \"")
                    .append(question.getId()).append("\", \"")
                    .append(optionText.replace("\"", "\\\"")).append("\")' ")
                    .append(checkedAttr).append(">");

            // Option text with LaTeX processing if needed
            if (question.isHasLatex()) {
                // Replace single $ with \( and \) for inline math
                optionText = optionText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");
                // Replace double $$ with \[ and \] for display math
                optionText = optionText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
            }

            htmlContent.append(optionText);
            htmlContent.append("</div>");
        }

        // Add JavaScript for handling checkbox clicks
        // Since we can't use Java callbacks, we'll use a simpler approach
        htmlContent.append("<script>");
        htmlContent.append("function handleCheckboxClick(checkbox, questionId, optionText) {");
        htmlContent.append("  // Uncheck all other checkboxes in the same question");
        htmlContent.append("  var options = checkbox.parentNode.parentNode.getElementsByTagName('input');");
        htmlContent.append("  for (var i = 0; i < options.length; i++) {");
        htmlContent.append("    if (options[i] !== checkbox) {");
        htmlContent.append("      options[i].checked = false;");
        htmlContent.append("    }");
        htmlContent.append("  }");
        htmlContent.append("  // Add a class to the parent div to show it's selected");
        htmlContent.append("  var optionDivs = checkbox.parentNode.parentNode.getElementsByClassName('option');");
        htmlContent.append("  for (var i = 0; i < optionDivs.length; i++) {");
        htmlContent.append("    optionDivs[i].classList.remove('selected-option');");
        htmlContent.append("  }");
        htmlContent.append("  if (checkbox.checked) {");
        htmlContent.append("    checkbox.parentNode.classList.add('selected-option');");
        htmlContent.append("  }");
        htmlContent.append("}");
        htmlContent.append("</script>");

        htmlContent.append("</div>"); // Close options container
        htmlContent.append("</div>"); // Close question container
        htmlContent.append("</body></html>");

        // Load the HTML content into the WebView
        engine.loadContent(htmlContent.toString());

        // Since we can't use JavaScript bridge due to module restrictions,
        // we'll make the checkboxes read-only in results view
        if (showResults) {
            // Disable checkboxes when showing results
            engine.executeScript("document.querySelectorAll('input[type=\"checkbox\"]').forEach(function(cb) { cb.disabled = true; });");
        }

        return webView;
    }

    /**
     * Update user answers based on checkbox selection
     * This method is called when a WebView is clicked
     * @param webView The WebView that was clicked
     * @param question The question associated with the WebView
     */
    private void setupWebViewClickHandler(WebView webView, Question question) {
        webView.setOnMouseClicked(event -> {
            if (showingResults) {
                return; // Don't allow changes when showing results
            }

            // Get all checkboxes in the WebView
            WebEngine engine = webView.getEngine();
            String script = "document.querySelectorAll('input[type=\"checkbox\"]');";
            Object result = engine.executeScript(script);

            // We can't directly access the checkboxes, so we'll need to poll them
            // Schedule a task to check which checkbox is selected after a short delay
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        // For each option, check if its checkbox is selected
                        for (int i = 0; i < question.getOptions().size(); i++) {
                            QuestionOption option = question.getOptions().get(i);
                            String checkScript = "document.querySelectorAll('input[type=\"checkbox\"]')[" + i + "].checked;";
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
            }, 100); // Short delay to allow the checkbox state to update
        });
    }

    /**
     * Start the countdown timer
     */
    private void startCountdownTimer() {
        // Cancel any existing timer
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        // Create new timer
        countdownTimer = new Timer(true);

        // Schedule timer task to update every second
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingTimeInSeconds > 0) {
                    remainingTimeInSeconds--;
                    Platform.runLater(() -> updateTimerDisplay());
                } else {
                    // Time's up
                    Platform.runLater(() -> {
                        MFXNotifications.showInfo("Time's Up", "Your time is up. Submitting answers...");
                        submitAnswers();
                    });
                    cancel();
                }
            }
        }, 0, 1000);
    }

    /**
     * Update the timer display
     */
    private void updateTimerDisplay() {
        int minutes = remainingTimeInSeconds / 60;
        int seconds = remainingTimeInSeconds % 60;
        timeRemainingLabel.setText(String.format("%02d:%02d", minutes, seconds));

        // Change color to red when less than 5 minutes remaining
        if (remainingTimeInSeconds < 300) {
            timeRemainingLabel.setStyle("-fx-text-fill: red;");
        } else {
            timeRemainingLabel.setStyle("-fx-text-fill: -primary-color;");
        }
    }

    /**
     * Handle submit answers button click
     */
    @FXML
    public void submitAnswers() {
        // If already showing results, go back to exam portal using NavigationUtils
        if (showingResults) {
            NavigationUtil.navigateToExamPortal(new ActionEvent(submitButton, ActionEvent.NULL_SOURCE_TARGET));
            return;
        }

        // Stop the timer
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }

        // Calculate score
        int correctAnswers = 0;
        int totalAnswered = userAnswers.size();

        for (Question question : questions) {
            int questionId = question.getId();
            if (userAnswers.containsKey(questionId)) {
                QuestionOption correctOption = question.getCorrectOption();
                if (correctOption != null && userAnswers.get(questionId).equals(correctOption.getOptionText())) {
                    correctAnswers++;
                }
            }
        }

        // Calculate total marks
        double scorePercentage = questions.size() > 0 ? (correctAnswers * 100.0 / questions.size()) : 0;
        int earnedMarks = (int) Math.round(totalMarks * scorePercentage / 100.0);

        // Show results with total marks
        String message = String.format(
            "You answered %d out of %d questions.\nCorrect answers: %d\nScore: %.1f%%\nTotal Marks: %d/%d",
            totalAnswered, questions.size(), correctAnswers,
            scorePercentage, earnedMarks, totalMarks
        );

        MFXNotifications.showInfo("Exam Completed", message);

        // Set flag to show results
        showingResults = true;

        // Update button text
        submitButton.setText("Return to Exam Portal");

        // Redisplay questions with correct answers highlighted
        displayQuestionsWithWebView();
    }

    /**
     * Reset the mock test UI to initial state
     */
    private void resetMockTestUI() {
        // Clear user answers
        userAnswers.clear();

        // Reset showing results flag
        showingResults = false;

        // Reset timer display
        timeRemainingLabel.setText("75:00");
        timeRemainingLabel.setStyle("-fx-text-fill: -primary-color;");

        // Reset submit button text
        submitButton.setText("Submit Answers");
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Mock Test screen
     */
    void cleanup() {
        LOGGER.info("Cleaning up MockTestController before navigation");

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
            LOGGER.info("Auth state change detected in MockTestController");

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
        LOGGER.info("Subscribed to auth state changes in MockTestController");

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
        LOGGER.info("Mock Test scene became active, refreshing auth UI");

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
        LOGGER.info("Refreshing MockTestController UI");

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
                LOGGER.info("Updated profile button in Mock Test page");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating UI components in MockTestController: {0}", e.getMessage());
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
                LOGGER.log(Level.SEVERE, "Error updating containers in MockTestController: {0}", e.getMessage());
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
