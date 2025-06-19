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

        // Shuffle questions to randomize order
        shuffleQuestions();

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

        // Set total marks based on the number of questions (each question is worth 1 mark)
        // This will be updated after questions are retrieved
        totalMarks = 0;

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

            // Set total marks based on the number of questions (each question is worth 1 mark)
            totalMarks = questions.size();
            totalMarksLabel.setText(String.valueOf(totalMarks));
            LOGGER.info("Total marks set to: " + totalMarks);

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
     * Questions are grouped by subject
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

        // Initialize question counter for sequential numbering regardless of question IDs
        int questionCounter = 1;

        // If showing results, add the marks summary at the top
        if (showingResults) {
            // Get marks data that was calculated in submitAnswers
            int correctAnswers = 0;
            for (Question question : questions) {
                int questionId = question.getId();
                if (userAnswers.containsKey(questionId)) {
                    QuestionOption correctOption = question.getCorrectOption();
                    if (correctOption != null && userAnswers.get(questionId).equals(correctOption.getOptionText())) {
                        correctAnswers++;
                    }
                }
            }

            // Each correct answer is worth 1 mark
            int earnedMarks = correctAnswers;
            double scorePercentage = questions.size() > 0 ? (correctAnswers * 100.0 / questions.size()) : 0;

            // Create a marks summary container with improved styling
            VBox marksContainer = new VBox(15);
            marksContainer.setAlignment(javafx.geometry.Pos.CENTER);
            marksContainer.getStyleClass().add("marks-container");
            marksContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #f9f9f9, #f0f8ff); " +
                    "-fx-padding: 20px; " +
                    "-fx-border-color: #4CAF50; " +
                    "-fx-border-width: 2px; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-margin-bottom: 25px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");

            Label marksHeader = new Label("Exam Results");
            marksHeader.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-underline: true;");

            // Create a more visually appealing marks display
            HBox marksBox = new HBox(20);
            marksBox.setAlignment(javafx.geometry.Pos.CENTER);
            marksBox.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));

            Label marksObtained = new Label(String.format("%d", earnedMarks));
            marksObtained.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

            Label marksOutOf = new Label(String.format("out of %d", totalMarks));
            marksOutOf.setStyle("-fx-font-size: 18px; -fx-text-fill: #555555; -fx-padding: 8 0 0 0;");

            VBox marksVBox = new VBox(0);
            marksVBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            marksVBox.getChildren().addAll(marksObtained, marksOutOf);

            // Create a circular progress indicator for the score percentage
            double scoreDecimal = scorePercentage / 100.0;
            javafx.scene.layout.StackPane scoreCircle = new javafx.scene.layout.StackPane();
            scoreCircle.setMinSize(80, 80);
            scoreCircle.setMaxSize(80, 80);

            // Background circle
            javafx.scene.shape.Circle bgCircle = new javafx.scene.shape.Circle(40);
            bgCircle.setFill(javafx.scene.paint.Color.web("#f0f0f0"));
            bgCircle.setStroke(javafx.scene.paint.Color.web("#dddddd"));
            bgCircle.setStrokeWidth(2);

            // Score text
            Label scoreLabel = new Label(String.format("%.1f%%", scorePercentage));
            scoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

            scoreCircle.getChildren().addAll(bgCircle, scoreLabel);

            marksBox.getChildren().addAll(marksVBox, scoreCircle);

            // Add performance rating based on score
            String performanceText;
            String performanceColor;
            if (scorePercentage >= 80) {
                performanceText = "Excellent!";
                performanceColor = "#4CAF50";
            } else if (scorePercentage >= 60) {
                performanceText = "Good";
                performanceColor = "#8BC34A";
            } else if (scorePercentage >= 40) {
                performanceText = "Fair";
                performanceColor = "#FFC107";
            } else if (scorePercentage >= 20) {
                performanceText = "Needs Improvement";
                performanceColor = "#FF9800";
            } else {
                performanceText = "Poor";
                performanceColor = "#F44336";
            }

            Label performanceLabel = new Label("Performance: " + performanceText);
            performanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + performanceColor + ";");

            marksContainer.getChildren().addAll(marksHeader, marksBox, performanceLabel);
            questionListContainer.getChildren().add(marksContainer);
        }

        // Create a VBox to hold subject sections
        VBox subjectsContainer = new VBox(20);
        questionListContainer.getChildren().add(subjectsContainer);

        // Group questions by subject
        Map<String, List<Question>> questionsBySubject = new HashMap<>();

        for (Question question : questions) {
            String subject = question.getSubject();
            if (subject == null || subject.isEmpty()) {
                subject = "General";
            }

            if (!questionsBySubject.containsKey(subject)) {
                questionsBySubject.put(subject, new ArrayList<>());
            }
            questionsBySubject.get(subject).add(question);
        }

        // Display questions by subject
        for (Map.Entry<String, List<Question>> entry : questionsBySubject.entrySet()) {
            String subject = entry.getKey();
            List<Question> subjectQuestions = entry.getValue();

            // Create subject header
            Label subjectLabel = new Label(subject);
            subjectLabel.getStyleClass().add("subject-header");
            subjectLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px 0;");

            // Create a container for this subject
            VBox subjectContainer = new VBox(10);
            subjectContainer.getChildren().add(subjectLabel);

            // Create a two-column layout for this subject's questions
            javafx.scene.layout.GridPane gridPane = new javafx.scene.layout.GridPane();
            gridPane.setHgap(20);
            gridPane.setVgap(20);

            // Set column constraints for equal width columns
            javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
            col1.setPercentWidth(50);
            javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
            col2.setPercentWidth(50);
            gridPane.getColumnConstraints().addAll(col1, col2);

            // Calculate how many questions should go in each column
            int totalQuestions = subjectQuestions.size();
            int questionsPerColumn = (totalQuestions + 1) / 2; // Ceiling division to ensure all questions fit

            // Add questions to the grid
            for (int i = 0; i < totalQuestions; i++) {
                Question question = subjectQuestions.get(i);

                // Determine column and row
                int column = i < questionsPerColumn ? 0 : 1;
                int row = i < questionsPerColumn ? i : i - questionsPerColumn;

                // Create and add the question WebView with sequential numbering
                WebView webView = createQuestionWebView(question, showingResults, questionCounter++);
                setupWebViewClickHandler(webView, question);
                gridPane.add(webView, column, row, 1, 1);
            }

            subjectContainer.getChildren().add(gridPane);
            subjectsContainer.getChildren().add(subjectContainer);
        }
    }

    /**
     * Create a WebView for displaying a question with LaTeX support
     * @param question The question to display
     * @param showResults Whether to show correct answers
     * @return A WebView containing the question and options
     */
    private WebView createQuestionWebView(Question question, boolean showResults, int questionNumber) {
        // Create a WebView for the question
        WebView webView = new WebView();
        webView.setPrefWidth(500); // Increased width from 400 to 500
        webView.setPrefHeight(500); // Increased height to avoid vertical scrollbar
        WebEngine engine = webView.getEngine();

        // Add subject as a data attribute for reference
        String subject = question.getSubject();
        if (subject != null && !subject.isEmpty()) {
            webView.getProperties().put("subject", subject);
        }

        // Build HTML content for the question
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html><html><head>");
        htmlContent.append("<meta charset='UTF-8'>");

        // Add subject label if available
        if (subject != null && !subject.isEmpty()) {
            htmlContent.append("<div style='background-color: #f0f0f0; padding: 5px; margin-bottom: 10px; border-radius: 4px; font-size: 12px;'>Subject: " + subject + "</div>");
        }
        htmlContent.append("<script type='text/javascript' id='MathJax-script' async ");
        htmlContent.append("src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js'></script>");
        htmlContent.append("<script type='text/x-mathjax-config'>");
        htmlContent.append("MathJax.Hub.Config({tex2jax: {inlineMath: [['\\\\(','\\\\)']], displayMath: [['\\\\[','\\\\]']]},");
        htmlContent.append("'HTML-CSS': {linebreaks: {automatic: true}},");
        htmlContent.append("CommonHTML: {linebreaks: {automatic: true}},");
        htmlContent.append("SVG: {linebreaks: {automatic: true}}");
        htmlContent.append("});</script>");

        // Add JavaScript function to handle radio button clicks
        htmlContent.append("<script type='text/javascript'>");
        htmlContent.append("function handleRadioClick(radio, questionId, optionText) {");
        htmlContent.append("  console.log('Radio clicked: ' + questionId + ' - ' + optionText);");
        htmlContent.append("  // Find all option containers in this question");
        htmlContent.append("  var optionContainers = document.querySelectorAll('.option');");
        htmlContent.append("  // Remove selected class from all options");
        htmlContent.append("  for (var i = 0; i < optionContainers.length; i++) {");
        htmlContent.append("    optionContainers[i].classList.remove('selected-option');");
        htmlContent.append("  }");
        htmlContent.append("  // Add selected class to the parent of the selected radio");
        htmlContent.append("  radio.parentNode.classList.add('selected-option');");
        htmlContent.append("}");
        htmlContent.append("</script>");

        htmlContent.append("<style>");
        htmlContent.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 10px; font-size: 16px; background-color: #fff; }");
        htmlContent.append(".question-container { margin-bottom: 15px; border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; }");
        htmlContent.append(".question-number { font-weight: bold; font-size: 18px; color: #FA4506; }");
        htmlContent.append(".question-text { font-weight: bold; font-size: 16px; margin-bottom: 10px; }");
        htmlContent.append(".question-image { max-width: 300px; max-height: 200px; width: auto; height: auto; display: block; margin: 10px auto; }");
        htmlContent.append(".options-container { margin-top: 10px; }");
        htmlContent.append(".option { margin: 5px 0; padding: 5px; display: flex; align-items: center; padding: 8px; border-radius: 4px; transition: all 0.3s; }");
        htmlContent.append(".option-radio { margin-right: 10px; cursor: pointer; width: 18px; height: 18px; }");
        htmlContent.append(".option-radio:checked { accent-color: #fa4506; }"); // Primary color from common.css
        htmlContent.append(".option-radio:hover { cursor: pointer; }");
        htmlContent.append(".option:hover { background-color: #f5f5f5; cursor: pointer; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        htmlContent.append(".correct-option { color: #4CAF50; font-weight: bold; border-left: 4px solid #4CAF50; padding-left: 12px; }");
        htmlContent.append(".selected-option { background-color: #f0f0f0; border-left: 4px solid #2196F3; padding-left: 12px; }");
        htmlContent.append(".correct-and-selected { background-color: #e8f5e9; border-left: 4px solid #4CAF50; padding-left: 12px; box-shadow: 0 2px 8px rgba(76,175,80,0.3); }");
        htmlContent.append(".incorrect-selected { background-color: #ffebee; border-left: 4px solid #F44336; padding-left: 12px; box-shadow: 0 2px 8px rgba(244,67,54,0.3); }");
        htmlContent.append(".mjx-chtml { display: inline-block !important; }");
        htmlContent.append("</style>");
        htmlContent.append("</head><body>");

        // Question container
        htmlContent.append("<div class='question-container'>");

        // Question number and text (using sequential number instead of question ID)
        htmlContent.append("<span class='question-number'>Question ").append(questionNumber).append(". </span>");

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

        // Add marks information when showing results
        if (showResults) {
            boolean isCorrect = userAnswer != null && userAnswer.equals(correctOptionText);
            String marksText = isCorrect ? "✓ 1 Mark" : "✗ 0 Marks";
            String marksColor = isCorrect ? "#4CAF50" : "#F44336";
            String bgColor = isCorrect ? "rgba(76,175,80,0.1)" : "rgba(244,67,54,0.1)";
            String borderColor = isCorrect ? "#4CAF50" : "#F44336";
            htmlContent.append("<div style='float: right; font-weight: bold; color: " + marksColor + 
                "; margin-right: 10px; padding: 5px 10px; border-radius: 15px; background-color: " + 
                bgColor + "; border: 1px solid " + borderColor + "; box-shadow: 0 1px 3px rgba(0,0,0,0.1);'>" + 
                marksText + "</div>");
        }

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

            // Add radio button instead of checkbox
            String checkedAttr = isSelected ? "checked" : "";
            htmlContent.append("<input type='radio' name='question-")
                    .append(question.getId())
                    .append("' class='option-radio' onclick='handleRadioClick(this, \"")
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

        // JavaScript for handling radio button clicks is now added in setupWebViewClickHandler

        htmlContent.append("</div>"); // Close options container
        htmlContent.append("</div>"); // Close question container
        htmlContent.append("</body></html>");

        // Load the HTML content into the WebView
        engine.loadContent(htmlContent.toString());

        // Since we can't use JavaScript bridge due to module restrictions,
        // we'll make the radio buttons read-only in results view
        if (showResults) {
            // Disable radio buttons when showing results
            engine.executeScript("document.querySelectorAll('input[type=\"radio\"]').forEach(function(rb) { rb.disabled = true; });");
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

        // Update total marks obtained label
        Label totalMarksObtainedLabel = new Label();
        totalMarksObtainedLabel.setId("totalMarksObtainedLabel");
        totalMarksObtainedLabel.getStyleClass().add("total-marks-obtained");

        // Stop the timer
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }

        // Calculate overall score
        int correctAnswers = 0;
        int totalAnswered = userAnswers.size();

        // Calculate score by subject
        Map<String, Integer> correctBySubject = new HashMap<>();
        Map<String, Integer> totalBySubject = new HashMap<>();

        for (Question question : questions) {
            // Get subject, default to "General" if not set
            String subject = question.getSubject();
            if (subject == null || subject.isEmpty()) {
                subject = "General";
            }

            // Initialize counters for this subject if not already done
            if (!totalBySubject.containsKey(subject)) {
                totalBySubject.put(subject, 0);
                correctBySubject.put(subject, 0);
            }

            // Increment total questions for this subject
            totalBySubject.put(subject, totalBySubject.get(subject) + 1);

            // Check if answer is correct
            int questionId = question.getId();
            if (userAnswers.containsKey(questionId)) {
                QuestionOption correctOption = question.getCorrectOption();
                if (correctOption != null && userAnswers.get(questionId).equals(correctOption.getOptionText())) {
                    correctAnswers++;
                    correctBySubject.put(subject, correctBySubject.get(subject) + 1);
                }
            }
        }

            // Calculate total marks - each correct answer is worth 1 mark
        int earnedMarks = correctAnswers;
        // Calculate percentage based on the number of questions, not the totalMarks (which might be different)
        double scorePercentage = questions.size() > 0 ? (correctAnswers * 100.0 / questions.size()) : 0;
        LOGGER.info("Earned marks: " + earnedMarks + " out of " + totalMarks + " (" + scorePercentage + "%)");

        // Build detailed results message with subject breakdown
        StringBuilder messageBuilder = new StringBuilder();
        // Add a prominent display of total marks obtained
        messageBuilder.append(String.format(
            "✅ TOTAL MARKS OBTAINED: %d out of %d ✅\n\n",
            earnedMarks, totalMarks
        ));

        // Create a marks summary container with improved styling
        VBox marksContainer = new VBox(15);
        marksContainer.setAlignment(javafx.geometry.Pos.CENTER);
        marksContainer.getStyleClass().add("marks-container");
        marksContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #f9f9f9, #f0f8ff); " +
                "-fx-padding: 20px; " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-margin-bottom: 25px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");

        Label marksHeader = new Label("Exam Results");
        marksHeader.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-underline: true;");

        // Create a more visually appealing marks display
        HBox marksBox = new HBox(20);
        marksBox.setAlignment(javafx.geometry.Pos.CENTER);
        marksBox.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));

        Label marksObtained = new Label(String.format("%d", earnedMarks));
        marksObtained.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        Label marksOutOf = new Label(String.format("out of %d", totalMarks));
        marksOutOf.setStyle("-fx-font-size: 18px; -fx-text-fill: #555555; -fx-padding: 8 0 0 0;");

        VBox marksVBox = new VBox(0);
        marksVBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        marksVBox.getChildren().addAll(marksObtained, marksOutOf);

        // Create a circular progress indicator for the score percentage
        double scoreDecimal = scorePercentage / 100.0;
        javafx.scene.layout.StackPane scoreCircle = new javafx.scene.layout.StackPane();
        scoreCircle.setMinSize(80, 80);
        scoreCircle.setMaxSize(80, 80);

        // Background circle
        javafx.scene.shape.Circle bgCircle = new javafx.scene.shape.Circle(40);
        bgCircle.setFill(javafx.scene.paint.Color.web("#f0f0f0"));
        bgCircle.setStroke(javafx.scene.paint.Color.web("#dddddd"));
        bgCircle.setStrokeWidth(2);

        // Score text
        Label scoreLabel = new Label(String.format("%.1f%%", scorePercentage));
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        scoreCircle.getChildren().addAll(bgCircle, scoreLabel);

        marksBox.getChildren().addAll(marksVBox, scoreCircle);

        // Add performance rating based on score
        String performanceText;
        String performanceColor;
        if (scorePercentage >= 80) {
            performanceText = "Excellent!";
            performanceColor = "#4CAF50";
        } else if (scorePercentage >= 60) {
            performanceText = "Good";
            performanceColor = "#8BC34A";
        } else if (scorePercentage >= 40) {
            performanceText = "Fair";
            performanceColor = "#FFC107";
        } else if (scorePercentage >= 20) {
            performanceText = "Needs Improvement";
            performanceColor = "#FF9800";
        } else {
            performanceText = "Poor";
            performanceColor = "#F44336";
        }

        Label performanceLabel = new Label("Performance: " + performanceText);
        performanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + performanceColor + ";");

        marksContainer.getChildren().addAll(marksHeader, marksBox, performanceLabel);

        // Add the marks container at the top of the question list container
        questionListContainer.getChildren().clear();
        questionListContainer.getChildren().add(marksContainer);

        // Build a more visually appealing message
        messageBuilder.append(String.format(
            "📊 EXAM SUMMARY 📊\n\n" +
            "You answered %d out of %d questions.\n" +
            "Correct answers: %d\n" +
            "Score: %.1f%%\n\n",
            totalAnswered, questions.size(), correctAnswers,
            scorePercentage
        ));

        // Add subject breakdown with improved formatting
        messageBuilder.append("📚 SUBJECT BREAKDOWN 📚\n");
        for (String subject : totalBySubject.keySet()) {
            int subjectTotal = totalBySubject.get(subject);
            int subjectCorrect = correctBySubject.get(subject);
            double subjectPercentage = subjectTotal > 0 ? (subjectCorrect * 100.0 / subjectTotal) : 0;

            // Add star indicators and emoji based on performance
            String performance;
            String emoji;
            if (subjectPercentage >= 80) {
                performance = "★★★★★ Excellent!";
                emoji = "🏆";
            } else if (subjectPercentage >= 60) {
                performance = "★★★★☆ Good";
                emoji = "👍";
            } else if (subjectPercentage >= 40) {
                performance = "★★★☆☆ Fair";
                emoji = "🔍";
            } else if (subjectPercentage >= 20) {
                performance = "★★☆☆☆ Needs Improvement";
                emoji = "📝";
            } else {
                performance = "★☆☆☆☆ Poor";
                emoji = "⚠️";
            }

            messageBuilder.append(String.format(
                "\n%s %s: %d/%d (%.1f%%)\n   %s\n",
                emoji, subject, subjectCorrect, subjectTotal, subjectPercentage, performance
            ));
        }

        // Show results with total marks and subject breakdown
        String title = String.format("🎓 Exam Completed - Score: %.1f%%", scorePercentage);
        MFXNotifications.showInfo(title, messageBuilder.toString());

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

    /**
     * Shuffle the questions to randomize their order
     * This helps prevent cheating by ensuring students don't get questions in the same order
     */
    private void shuffleQuestions() {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        LOGGER.info("Shuffling " + questions.size() + " questions");

        // Use Collections.shuffle to randomize the order
        java.util.Collections.shuffle(questions);

        LOGGER.info("Questions shuffled successfully");
    }
}
