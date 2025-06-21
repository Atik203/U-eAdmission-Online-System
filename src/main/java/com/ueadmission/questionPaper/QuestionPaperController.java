package com.ueadmission.questionPaper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Controller for the Question Paper page
 * Handles user interactions and navigation for the Question Paper screen
 */
public class QuestionPaperController {

    private static final Logger LOGGER = Logger.getLogger(QuestionPaperController.class.getName());
    private Consumer<AuthState> authStateListener;

    // Current question paper
    private QuestionPaper currentQuestionPaper;
    private List<Question> questions = new ArrayList<>();

    // Separate lists for mock exam and actual exam questions
    private List<Question> mockExamQuestions = new ArrayList<>();
    private List<Question> actualExamQuestions = new ArrayList<>();

    // Map to track questions per subject
    private java.util.Map<String, Integer> questionsPerSubjectMap = new java.util.HashMap<>();
    private java.util.Map<String, Integer> maxQuestionsPerSubjectMap = new java.util.HashMap<>();
    private String currentSelectedSubject;

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

    // Question Paper specific elements
    @FXML private MFXCheckbox mockExamCheckbox;
    @FXML private MFXCheckbox actualExamCheckbox;
    @FXML private MFXComboBox<String> schoolComboBox;
    @FXML private MFXComboBox<String> subjectComboBox;
    @FXML private Label remainingQuestionsLabel;
    @FXML private Label subjectErrorLabel;
    @FXML private TextArea questionTextArea;
    @FXML private MFXToggleButton includeImageToggle;
    @FXML private MFXButton uploadImageButton;
    @FXML private Label imageNameLabel;
    @FXML private MFXToggleButton latexSupportToggle;
    @FXML private MFXCheckbox correctOption1Checkbox;
    @FXML private MFXCheckbox correctOption2Checkbox;
    @FXML private MFXCheckbox correctOption3Checkbox;
    @FXML private MFXCheckbox correctOption4Checkbox;
    @FXML private MFXTextField option1TextField;
    @FXML private MFXTextField option2TextField;
    @FXML private MFXTextField option3TextField;
    @FXML private MFXTextField option4TextField;
    @FXML private MFXButton clearFormButton;
    @FXML private MFXButton addQuestionButton;

    // LaTeX Preview elements
    @FXML private VBox latexPreviewContainer;
    @FXML private WebView latexWebView;

    // Options LaTeX Preview elements
    @FXML private VBox optionsLatexPreviewContainer;
    @FXML private WebView optionsLatexWebView;

    // Image Preview elements
    @FXML private VBox imagePreviewContainer;
    @FXML private ImageView imagePreview;

    // Question List elements
    @FXML private VBox questionListContainer;
    @FXML private Label noQuestionsLabel;

    // Error message labels
    @FXML private Label questionTextErrorLabel;
    @FXML private Label schoolErrorLabel;
    @FXML private Label optionsErrorLabel;
    @FXML private Label correctOptionErrorLabel;

    // Store the uploaded image URL
    private String uploadedImageUrl;

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

        // Initialize question paper database schema
        initializeQuestionPaperSchema();

        // Initialize question paper specific elements
        initializeQuestionPaperElements();
    }

    /**
     * Initialize question paper specific elements
     */
    private void initializeQuestionPaperElements() {
        // Try to load the most recent question paper
        loadMostRecentQuestionPaper();

        // Initialize school combo box with the 4 schools
        List<String> schools = Arrays.asList(
            "School of Engineering & Technology",
            "School of Business & Economics",
            "School of Humanities & Social Sciences",
            "School of Life Sciences"
        );
        schoolComboBox.getItems().addAll(schools);

        // Set the first school as default
        schoolComboBox.setValue(schools.get(0));

        // Initialize subject combo box (will be populated based on selected school)
        subjectComboBox.setDisable(true);

        // Set up school combo box listener to update subject combo box and reload question paper
        schoolComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSubjectsForSchool(newVal);

                // Reset current question paper when school changes
                currentQuestionPaper = null;
                questions.clear();

                // Load the appropriate question paper for the new school and current exam type
                loadMostRecentQuestionPaper();
            } else {
                subjectComboBox.getItems().clear();
                subjectComboBox.setDisable(true);
                remainingQuestionsLabel.setText("Remaining: 0");
            }
        });

        // Set up subject combo box listener to update remaining questions count
        subjectComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateRemainingQuestionsCount(newVal);
            }
        });

        // Set up toggle for image upload button
        includeImageToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            uploadImageButton.setDisable(!newVal);
            imagePreviewContainer.setVisible(newVal);
            imagePreviewContainer.setManaged(newVal);
            if (!newVal) {
                imageNameLabel.setText("No file selected");
            }
        });

        // Set up upload image button
        uploadImageButton.setOnAction(event -> handleImageUpload());

        // Set up LaTeX support toggle
        latexSupportToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            latexPreviewContainer.setVisible(newVal);
            latexPreviewContainer.setManaged(newVal);
            updateLatexPreview();
        });

        // Set up question text area to update LaTeX preview
        questionTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        // Set up option text fields to update LaTeX preview
        option1TextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        option2TextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        option3TextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        option4TextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        // Update LaTeX preview when correct option checkboxes change
        correctOption1Checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        correctOption2Checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        correctOption3Checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        correctOption4Checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (latexSupportToggle.isSelected()) {
                updateLatexPreview();
            }
        });

        // Set up clear form button
        clearFormButton.setOnAction(event -> clearForm());

        // Set up add question button
        addQuestionButton.setOnAction(event -> addQuestion());

        // Initialize question list
        updateQuestionList();

        // Group checkboxes for correct answer
        javafx.beans.value.ChangeListener<Boolean> checkboxListener = (obs, oldVal, newVal) -> {
            if (newVal) {
                // Deselect other checkboxes
                if (obs == correctOption1Checkbox.selectedProperty()) {
                    correctOption2Checkbox.setSelected(false);
                    correctOption3Checkbox.setSelected(false);
                    correctOption4Checkbox.setSelected(false);
                } else if (obs == correctOption2Checkbox.selectedProperty()) {
                    correctOption1Checkbox.setSelected(false);
                    correctOption3Checkbox.setSelected(false);
                    correctOption4Checkbox.setSelected(false);
                } else if (obs == correctOption3Checkbox.selectedProperty()) {
                    correctOption1Checkbox.setSelected(false);
                    correctOption2Checkbox.setSelected(false);
                    correctOption4Checkbox.setSelected(false);
                } else if (obs == correctOption4Checkbox.selectedProperty()) {
                    correctOption1Checkbox.setSelected(false);
                    correctOption2Checkbox.setSelected(false);
                    correctOption3Checkbox.setSelected(false);
                }
            }
        };

        correctOption1Checkbox.selectedProperty().addListener(checkboxListener);
        correctOption2Checkbox.selectedProperty().addListener(checkboxListener);
        correctOption3Checkbox.selectedProperty().addListener(checkboxListener);
        correctOption4Checkbox.selectedProperty().addListener(checkboxListener);

        // Group checkboxes for exam type and switch between question lists when exam type changes
        mockExamCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                actualExamCheckbox.setSelected(false);

                // Reset current question paper when exam type changes
                currentQuestionPaper = null;

                // Switch to mock exam questions without clearing them
                questions.clear();
                questions.addAll(mockExamQuestions);

                // Update the UI with the mock exam questions
                updateQuestionCountsPerSubject();
                updateQuestionList();

                // Load the appropriate question paper for the current school and new exam type
                loadMostRecentQuestionPaper();
            }
        });

        actualExamCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                mockExamCheckbox.setSelected(false);

                // Reset current question paper when exam type changes
                currentQuestionPaper = null;

                // Switch to actual exam questions without clearing them
                questions.clear();
                questions.addAll(actualExamQuestions);

                // Update the UI with the actual exam questions
                updateQuestionCountsPerSubject();
                updateQuestionList();

                // Load the appropriate question paper for the current school and new exam type
                loadMostRecentQuestionPaper();
            }
        });
    }

    /**
     * Handle image upload button click
     */
    private void handleImageUpload() {
        // Open a file chooser dialog to select an image
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Get the current stage from any UI element
        javafx.stage.Window window = homeButton.getScene().getWindow();

        // Show the file chooser dialog
        java.io.File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null) {
            try {
                // Update the label with the file name
                imageNameLabel.setText(selectedFile.getName());

                // Load the selected image
                javafx.scene.image.Image originalImage = new javafx.scene.image.Image(
                    selectedFile.toURI().toString()
                );

                // Create a consistent size image (300x200) while maintaining aspect ratio
                double width = 300;
                double height = 200;

                // Create a WritableImage with the desired dimensions
                javafx.scene.image.WritableImage resizedImage = new javafx.scene.image.WritableImage(
                    (int) width, (int) height
                );

                // Create a Canvas to draw the resized image
                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(width, height);
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

                // Fill the background with white
                gc.setFill(javafx.scene.paint.Color.WHITE);
                gc.fillRect(0, 0, width, height);

                // Calculate the scaling to maintain aspect ratio
                double originalWidth = originalImage.getWidth();
                double originalHeight = originalImage.getHeight();
                double scale = Math.min(width / originalWidth, height / originalHeight);

                // Calculate the centered position
                double x = (width - originalWidth * scale) / 2;
                double y = (height - originalHeight * scale) / 2;

                // Draw the image centered and scaled
                gc.drawImage(originalImage, x, y, originalWidth * scale, originalHeight * scale);

                // Capture the canvas as an image
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(javafx.scene.paint.Color.TRANSPARENT);
                javafx.scene.image.Image processedImage = canvas.snapshot(params, null);

                // Set the processed image to the preview
                imagePreview.setImage(processedImage);

                // Show the image preview container
                imagePreviewContainer.setVisible(true);
                imagePreviewContainer.setManaged(true);

                LOGGER.info("Image uploaded: " + selectedFile.getName());

                // Upload the image to Cloudinary
                String cloudinaryUrl = uploadToCloudinary(selectedFile);
                LOGGER.info("Image uploaded to Cloudinary: " + cloudinaryUrl);

                // Store the URL for later use
                uploadedImageUrl = cloudinaryUrl;

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load selected image", e);
                MFXNotifications.showError("Error", "Failed to load the selected image");
            }
        }
    }

    /**
     * Upload an image to Cloudinary
     * 
     * @param imageFile The image file to upload
     * @return The URL of the uploaded image
     */
    private String uploadToCloudinary(java.io.File imageFile) {
        try {
            // Load environment variables from .env file
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .load();

            // Get Cloudinary credentials from .env file
            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
            String apiKey = dotenv.get("CLOUDINARY_API_KEY");
            String apiSecret = dotenv.get("CLOUDINARY_API_SECRET");

            // Check if credentials are set
            if (cloudName == null || cloudName.equals("your_cloud_name") || 
                apiKey == null || apiKey.equals("your_api_key") || 
                apiSecret == null || apiSecret.equals("your_api_secret")) {
                LOGGER.warning("Cloudinary credentials not properly configured in .env file");
                MFXNotifications.showWarning("Warning", "Cloudinary credentials not configured. Using local storage.");
                return imageFile.toURI().toString();
            }

            // Configure Cloudinary
            com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(com.cloudinary.utils.ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
            ));

            LOGGER.info("Uploading image to Cloudinary: " + imageFile.getName());

            // Upload the image
            java.util.Map uploadResult = cloudinary.uploader().upload(
                imageFile, 
                com.cloudinary.utils.ObjectUtils.emptyMap()
            );

            // Get the URL of the uploaded image
            String imageUrl = (String) uploadResult.get("url");

            LOGGER.info("Image uploaded successfully to Cloudinary: " + imageUrl);
            return imageUrl;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading image to Cloudinary", e);
            MFXNotifications.showError("Error", "Failed to upload image to Cloudinary: " + e.getMessage());

            // Return local file URL as fallback
            return imageFile.toURI().toString();
        }
    }

    /**
     * Update the LaTeX preview based on the current question text and options
     */
    private void updateLatexPreview() {
        if (latexSupportToggle.isSelected()) {
            // Get question text and options
            String questionText = questionTextArea.getText();
            String option1 = option1TextField.getText();
            String option2 = option2TextField.getText();
            String option3 = option3TextField.getText();
            String option4 = option4TextField.getText();

            // Create HTML content with MathJax for rendering LaTeX
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head>");
            htmlContent.append("<meta charset='UTF-8'>");
            htmlContent.append("<script type='text/javascript' id='MathJax-script' async ");
            htmlContent.append("src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js'></script>");
            htmlContent.append("<style>");
            htmlContent.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 10px; font-size: 16px; background-color: #fff; }");
            htmlContent.append(".question-container { margin-bottom: 15px; }");
            htmlContent.append(".question-number { font-weight: bold; font-size: 18px; color: #FA4506; }");
            htmlContent.append(".question-text { font-weight: bold; font-size: 16px; margin-bottom: 10px; }");
            htmlContent.append(".question-image { max-width: 300px; max-height: 200px; width: auto; height: auto; display: block; margin: 10px auto; }");
            htmlContent.append(".options-container { margin-top: 10px; }");
            htmlContent.append(".option { margin: 5px 0; }");
            htmlContent.append(".correct-option { color: #4CAF50; font-weight: bold; }");
            htmlContent.append(".mjx-chtml { display: inline-block !important; }");
            htmlContent.append("</style>");
            htmlContent.append("</head><body>");

            // Question container
            htmlContent.append("<div class='question-container'>");

            // Question number and text
            htmlContent.append("<span class='question-number'>1. </span>");

            // Process question text for LaTeX
            String processedText = questionText;
            // Replace single $ with \( and \) for inline math
            processedText = processedText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");
            // Replace double $$ with \[ and \] for display math
            processedText = processedText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");

            htmlContent.append("<span class='question-text'>").append(processedText).append("</span>");

            // Add image if present
            if (includeImageToggle.isSelected() && uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                htmlContent.append("<div><img src='").append(uploadedImageUrl).append("' class='question-image' alt='Question Image'></div>");
            }

            // Options container
            htmlContent.append("<div class='options-container'>");

            // Process and add each option
            String optionClass1 = correctOption1Checkbox.isSelected() ? "option correct-option" : "option";
            htmlContent.append("<div class='").append(optionClass1).append("'>1. ");
            String processedOption1 = option1.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)").replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
            htmlContent.append(processedOption1).append("</div>");

            String optionClass2 = correctOption2Checkbox.isSelected() ? "option correct-option" : "option";
            htmlContent.append("<div class='").append(optionClass2).append("'>2. ");
            String processedOption2 = option2.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)").replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
            htmlContent.append(processedOption2).append("</div>");

            String optionClass3 = correctOption3Checkbox.isSelected() ? "option correct-option" : "option";
            htmlContent.append("<div class='").append(optionClass3).append("'>3. ");
            String processedOption3 = option3.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)").replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
            htmlContent.append(processedOption3).append("</div>");

            String optionClass4 = correctOption4Checkbox.isSelected() ? "option correct-option" : "option";
            htmlContent.append("<div class='").append(optionClass4).append("'>4. ");
            String processedOption4 = option4.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)").replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
            htmlContent.append(processedOption4).append("</div>");

            htmlContent.append("</div>"); // Close options container
            htmlContent.append("</div>"); // Close question container

            htmlContent.append("</body></html>");

            // Load the HTML content into the WebView
            WebEngine engine = latexWebView.getEngine();
            engine.loadContent(htmlContent.toString());

            // Show the question preview container
            latexPreviewContainer.setVisible(true);
            latexPreviewContainer.setManaged(true);

            // Hide the options preview container as we're showing everything in the question preview
            optionsLatexPreviewContainer.setVisible(false);
            optionsLatexPreviewContainer.setManaged(false);

            LOGGER.info("Full question preview with options updated with MathJax rendering");
        } else {
            // Hide both preview containers
            latexPreviewContainer.setVisible(false);
            latexPreviewContainer.setManaged(false);
            optionsLatexPreviewContainer.setVisible(false);
            optionsLatexPreviewContainer.setManaged(false);
        }
    }

    /**
     * Process text to replace LaTeX delimiters with MathJax syntax
     */
    private String processLatexText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String processedText = text;

        // Replace single $ with \( and \) for inline math
        // Use a non-greedy match to ensure proper pairing
        processedText = processedText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");

        // Replace double $$ with \[ and \] for display math
        // Use a non-greedy match to ensure proper pairing
        processedText = processedText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");

        // Configure MathJax to ensure inline rendering
        processedText = "<script type='text/x-mathjax-config'>" +
                        "MathJax.Hub.Config({" +
                        "  tex2jax: { inlineMath: [['\\\\(','\\\\)']], displayMath: [['\\\\[','\\\\]']] }," +
                        "  'HTML-CSS': { linebreaks: { automatic: true } }," +
                        "  CommonHTML: { linebreaks: { automatic: true } }," +
                        "  SVG: { linebreaks: { automatic: true } }" +
                        "});" +
                        "</script>" + processedText;

        return processedText;
    }

    /**
     * Update the question list display
     */
    private void updateQuestionList() {
        // Clear existing items except the placeholder
        questionListContainer.getChildren().clear();

        // If there are no questions, show the placeholder
        if (questions.isEmpty()) {
            questionListContainer.getChildren().add(noQuestionsLabel);
            return;
        }

        // Hide the placeholder
        noQuestionsLabel.setVisible(false);
        noQuestionsLabel.setManaged(false);

        // Add each question to the list with sequential numbering
        int questionNumber = 1;
        for (Question question : questions) {
            // Create a VBox for the question
            VBox questionItem = new VBox(10);
            questionItem.getStyleClass().add("question-item");

            // Create a single WebView for the entire question (text, image, and options)
            WebView webView = new WebView();
            webView.setPrefWidth(800);
            WebEngine engine = webView.getEngine();

            // Build HTML content for the entire question
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
            htmlContent.append(".question-container { margin-bottom: 15px; }");
            htmlContent.append(".question-number { font-weight: bold; font-size: 18px; color: #FA4506; }");
            htmlContent.append(".question-text { font-weight: bold; font-size: 16px; margin-bottom: 10px; }");
            htmlContent.append(".question-image { max-width: 300px; max-height: 200px; width: auto; height: auto; display: block; margin: 10px auto; }");
            htmlContent.append(".options-container { margin-top: 10px; }");
            htmlContent.append(".option { margin: 5px 0; }");
            htmlContent.append(".correct-option { color: #4CAF50; font-weight: bold; }");
            htmlContent.append(".mjx-chtml { display: inline-block !important; }");
            htmlContent.append("</style>");
            htmlContent.append("</head><body>");

            // Question container
            htmlContent.append("<div class='question-container'>");

            // Question number and text
            htmlContent.append("<span class='question-number'>").append(questionNumber).append(". </span>");

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

            // Add options
            for (QuestionOption option : question.getOptions()) {
                String optionClass = option.isCorrect() ? "option correct-option" : "option";
                htmlContent.append("<div class='").append(optionClass).append("'>");

                // Option number
                htmlContent.append(option.getOptionOrder()).append(". ");

                // Process option text for LaTeX if needed
                String optionText = option.getOptionText();
                if (question.isHasLatex()) {
                    // Replace single $ with \( and \) for inline math
                    optionText = optionText.replaceAll("\\$([^$]+?)\\$", "\\\\($1\\\\)");
                    // Replace double $$ with \[ and \] for display math
                    optionText = optionText.replaceAll("\\$\\$([^$]+?)\\$\\$", "\\\\[$1\\\\]");
                }

                htmlContent.append(optionText);
                htmlContent.append("</div>");
            }

            htmlContent.append("</div>"); // Close options container
            htmlContent.append("</div>"); // Close question container

            htmlContent.append("</body></html>");

            // Load the HTML content into the WebView
            engine.loadContent(htmlContent.toString());

            // Set appropriate height based on content
            webView.setPrefHeight(300);

            // Add the WebView to the question item
            questionItem.getChildren().add(webView);

            // Add to container
            questionListContainer.getChildren().add(questionItem);

            // Increment question number
            questionNumber++;
        }
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        questionTextArea.clear();
        option1TextField.clear();
        option2TextField.clear();
        option3TextField.clear();
        option4TextField.clear();
        correctOption1Checkbox.setSelected(false);
        correctOption2Checkbox.setSelected(false);
        correctOption3Checkbox.setSelected(false);
        correctOption4Checkbox.setSelected(false);
        includeImageToggle.setSelected(false);
        latexSupportToggle.setSelected(false);
        imageNameLabel.setText("No file selected");

        // If a school is selected, reset the subject dropdown to the first subject
        if (schoolComboBox.getValue() != null && !subjectComboBox.getItems().isEmpty()) {
            subjectComboBox.setValue(subjectComboBox.getItems().get(0));
            updateRemainingQuestionsCount(subjectComboBox.getValue());
        }

        // Reset image preview
        imagePreviewContainer.setVisible(false);
        imagePreviewContainer.setManaged(false);
        imagePreview.setImage(null);

        // Reset LaTeX preview
        latexPreviewContainer.setVisible(false);
        latexPreviewContainer.setManaged(false);
        optionsLatexPreviewContainer.setVisible(false);
        optionsLatexPreviewContainer.setManaged(false);

        // Clear WebView content if it exists
        if (latexWebView != null && latexWebView.getEngine() != null) {
            latexWebView.getEngine().loadContent("");
        }
        if (optionsLatexWebView != null && optionsLatexWebView.getEngine() != null) {
            optionsLatexWebView.getEngine().loadContent("");
        }

        // Reset uploaded image URL
        uploadedImageUrl = null;

        // Clear error messages
        clearErrorMessages();

        LOGGER.info("Form cleared");
    }

    /**
     * Clear all error messages
     */
    private void clearErrorMessages() {
        // Hide all error labels
        questionTextErrorLabel.setVisible(false);
        questionTextErrorLabel.setManaged(false);
        schoolErrorLabel.setVisible(false);
        schoolErrorLabel.setManaged(false);
        subjectErrorLabel.setVisible(false);
        subjectErrorLabel.setManaged(false);
        optionsErrorLabel.setVisible(false);
        optionsErrorLabel.setManaged(false);
        correctOptionErrorLabel.setVisible(false);
        correctOptionErrorLabel.setManaged(false);
    }

    /**
     * Update the subjects dropdown based on the selected school
     * @param school The selected school
     */
    private void updateSubjectsForSchool(String school) {
        // Clear previous items
        subjectComboBox.getItems().clear();
        questionsPerSubjectMap.clear();
        maxQuestionsPerSubjectMap.clear();

        // Get subjects for the selected school from the database
        boolean isMockExam = mockExamCheckbox.isSelected();
        List<String> subjects = QuestionPaperDAO.getSubjectsForSchool(school, isMockExam);

        // Add subjects to combo box
        subjectComboBox.getItems().addAll(subjects);

        // Initialize question count maps
        for (String subject : subjects) {
            int maxQuestions = QuestionPaperDAO.getMaxQuestions(school, isMockExam, subject);
            questionsPerSubjectMap.put(subject, 0);
            maxQuestionsPerSubjectMap.put(subject, maxQuestions);
        }

        // Enable subject combo box
        subjectComboBox.setDisable(false);

        // Reload question counts for subjects in this school
        if (currentQuestionPaper != null) {
            updateQuestionCountsPerSubject();
        }

        // Select first subject by default
        if (!subjects.isEmpty()) {
            subjectComboBox.setValue(subjects.get(0));
            currentSelectedSubject = subjects.get(0);
            updateRemainingQuestionsCount(subjects.get(0));
        }
    }

    /**
     * Update the remaining questions count for the selected subject
     * @param subject The selected subject
     */
    private void updateRemainingQuestionsCount(String subject) {
        currentSelectedSubject = subject;

        // Get maximum questions allowed for the subject
        int maxQuestions = maxQuestionsPerSubjectMap.getOrDefault(subject, 0);

        // Query database for current question count for this subject and school
        int currentQuestions = 0;
        int totalCurrentQuestions = 0;
        int totalMaxQuestions = 0;

        try {
            // Check if we have a valid question paper
            if (currentQuestionPaper != null) {
                String school = currentQuestionPaper.getSchool();
                boolean isMockExam = currentQuestionPaper.isMockExam();

                // Use QuestionPaperDAO methods to get question counts
                currentQuestions = QuestionPaperDAO.getQuestionCount(school, isMockExam, subject);
                totalCurrentQuestions = QuestionPaperDAO.getTotalQuestionCount(school, isMockExam);
                totalMaxQuestions = QuestionPaperDAO.getTotalMaxQuestions(school, isMockExam);
            } else {
                // Fallback to instance map if no current paper
                currentQuestions = questionsPerSubjectMap.getOrDefault(subject, 0);

                // Calculate total current and max questions
                for (String subj : maxQuestionsPerSubjectMap.keySet()) {
                    totalCurrentQuestions += questionsPerSubjectMap.getOrDefault(subj, 0);
                    totalMaxQuestions += maxQuestionsPerSubjectMap.getOrDefault(subj, 0);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting question count from database", e);
            // Fallback to instance map if database query fails
            currentQuestions = questionsPerSubjectMap.getOrDefault(subject, 0);

            // Calculate total current and max questions
            for (String subj : maxQuestionsPerSubjectMap.keySet()) {
                totalCurrentQuestions += questionsPerSubjectMap.getOrDefault(subj, 0);
                totalMaxQuestions += maxQuestionsPerSubjectMap.getOrDefault(subj, 0);
            }
        }

        // Calculate remaining questions
        int remainingQuestions = maxQuestions - currentQuestions;
        int totalRemainingQuestions = totalMaxQuestions - totalCurrentQuestions;

        // Update the instance map with the current count from database
        questionsPerSubjectMap.put(subject, currentQuestions);

        // Update label with both subject-specific and total counts
        remainingQuestionsLabel.setText(String.format("Remaining: %d (Subject: %s) / Total: %d", 
                                       remainingQuestions, subject, totalRemainingQuestions));
    }

    /**
     * Initialize the question paper database schema
     */
    private void initializeQuestionPaperSchema() {
        try {
            // Only initialize the schema if needed, don't reset tables
            boolean success = QuestionPaperDAO.initializeQuestionPaperSchema();
            if (success) {
                LOGGER.info("Question paper schema initialized successfully");
            } else {
                LOGGER.warning("Failed to initialize question paper schema");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing question paper schema", e);
        }
    }

    /**
     * Load the most recent question paper from the database
     */
    private void loadMostRecentQuestionPaper() {
        try {
            // Get the most recent question paper based on the selected school and exam type
            boolean isMockExam = mockExamCheckbox.isSelected();
            String school = schoolComboBox.getValue();

            // Only proceed if a school is selected
            if (school == null || school.isEmpty()) {
                LOGGER.info("No school selected, cannot load question paper");
                return;
            }

            QuestionPaper paper = QuestionPaperDAO.getMostRecentQuestionPaper(isMockExam, school);

            if (paper != null) {
                // Set as current question paper
                currentQuestionPaper = paper;

                // Load existing questions from the database
                List<Question> existingQuestions = QuestionPaperDAO.getQuestionsForPaper(paper.getId());

                // Store questions in the appropriate list based on exam type
                if (isMockExam) {
                    // For mock exams, update the mockExamQuestions list
                    mockExamQuestions.clear();
                    mockExamQuestions.addAll(existingQuestions);

                    // Update the main questions list for display
                    questions.clear();
                    questions.addAll(mockExamQuestions);
                } else {
                    // For actual exams, update the actualExamQuestions list
                    actualExamQuestions.clear();
                    actualExamQuestions.addAll(existingQuestions);

                    // Update the main questions list for display
                    questions.clear();
                    questions.addAll(actualExamQuestions);
                }

                // Set school in UI if available
                if (paper.getSchool() != null && !paper.getSchool().isEmpty()) {
                    schoolComboBox.setValue(paper.getSchool());

                    // This will update maxQuestionsPerSubjectMap based on the school
                    updateSubjectsForSchool(paper.getSchool());
                }

                // Update question counts per subject
                // Only count questions in the current session
                updateQuestionCountsPerSubject();

                // Reset the question list - only show questions added in current session
                updateQuestionList();

                LOGGER.info("Loaded most recent question paper with ID: " + paper.getId() + 
                           " and " + paper.getQuestions().size() + " total questions" +
                           " for school: " + school + " and mock exam: " + isMockExam);

                // Set mock exam checkbox
                mockExamCheckbox.setSelected(paper.isMockExam());
                actualExamCheckbox.setSelected(!paper.isMockExam());
            } else {
                LOGGER.info("No existing question paper found for school: " + school + " and mock exam: " + isMockExam);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading most recent question paper", e);
        }
    }

    /**
     * Update question counts per subject based on loaded questions
     */
    private void updateQuestionCountsPerSubject() {
        // Reset counts to zero for all subjects in the current school
        for (String subject : maxQuestionsPerSubjectMap.keySet()) {
            questionsPerSubjectMap.put(subject, 0);
        }

        // Only count questions from the current session
        for (Question question : questions) {
            String subject = question.getSubject();
            if (subject != null && !subject.isEmpty() && questionsPerSubjectMap.containsKey(subject)) {
                int count = questionsPerSubjectMap.getOrDefault(subject, 0);
                questionsPerSubjectMap.put(subject, count + 1);
            }
        }

        // Update remaining questions count if a subject is selected
        if (subjectComboBox.getValue() != null) {
            updateRemainingQuestionsCount(subjectComboBox.getValue());
        }
    }

    /**
     * Create a new question paper
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @return The ID of the newly created question paper, or -1 if creation failed
     */
    private int createQuestionPaper(String title, String description, String school, boolean isMockExam) {
        try {
            // Get the current user ID from the auth state
            AuthState authState = AuthStateManager.getInstance().getState();
            int userId = authState != null && authState.getUser() != null ? authState.getUser().getId() : -1;

            if (userId == -1) {
                LOGGER.warning("Cannot create question paper: User not authenticated");
                return -1;
            }

            // Use static values from exam portal instead of UI fields
            Integer totalQuestions = null;
            String subjects = null;
            String questionsPerSubject = null;
            Integer timeLimitMinutes = null;
            Integer totalMarks = null;

            // Set default values based on school
            setDefaultValuesBasedOnSchool(school, isMockExam);

            // Use the values set by the default method
            if (school.equals("School of Engineering & Technology")) {
                subjects = "English, General Mathematics, Higher Math & Physics";
                questionsPerSubject = "30, 15, 30";
            } else if (school.equals("School of Business & Economics")) {
                subjects = "English, General Mathematics, Business & Economics";
                questionsPerSubject = "30, 15, 30";
            } else if (school.equals("School of Humanities & Social Sciences")) {
                subjects = "English, General Mathematics, Current Affairs, Higher English & Logical Reasoning";
                questionsPerSubject = "30, 15, 15, 15";
            } else if (school.equals("School of Life Sciences")) {
                subjects = "English, General Mathematics, Biology & Chemistry";
                questionsPerSubject = "30, 15, 30";
            }

            totalQuestions = isMockExam ? 75 : 100;
            timeLimitMinutes = isMockExam ? 75 : 120;
            totalMarks = isMockExam ? 75 : 100;

            // Create the question paper with all fields
            int questionPaperId = QuestionPaperDAO.createQuestionPaper(
                title, description, school, isMockExam, 
                totalQuestions, subjects, questionsPerSubject,
                timeLimitMinutes, totalMarks, userId);

            if (questionPaperId != -1) {
                // Create a new QuestionPaper object with all fields
                currentQuestionPaper = new QuestionPaper(
                    title, description, school, isMockExam, 
                    totalQuestions, subjects, questionsPerSubject, 
                    timeLimitMinutes, totalMarks, userId);
                currentQuestionPaper.setId(questionPaperId);

                LOGGER.info("Created question paper with ID: " + questionPaperId);
                return questionPaperId;
            } else {
                LOGGER.warning("Failed to create question paper");
                return -1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating question paper", e);
            return -1;
        }
    }

    /**
     * Set default values for exam fields based on the selected school and exam type
     * 
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     */
    private void setDefaultValuesBasedOnSchool(String school, boolean isMockExam) {
        // This method now only logs the action since we're using static values
        // and no longer setting UI fields
        LOGGER.info("Using default values for " + school + " - Mock Exam: " + isMockExam);
    }

    /**
     * Add a question to the question paper
     */
    private void addQuestion() {
        // Clear previous error messages
        clearErrorMessages();

        // Flag to track validation errors
        boolean hasErrors = false;

        // Validate form
        if (questionTextArea.getText().trim().isEmpty()) {
            // Show error message
            MFXNotifications.showError("Error", "Question text is empty");
            questionTextErrorLabel.setText("Question text cannot be empty");
            questionTextErrorLabel.setVisible(true);
            questionTextErrorLabel.setManaged(true);
            LOGGER.warning("Question text is empty");
            hasErrors = true;
        }

        if (option1TextField.getText().trim().isEmpty() ||
            option2TextField.getText().trim().isEmpty() ||
            option3TextField.getText().trim().isEmpty() ||
            option4TextField.getText().trim().isEmpty()) {
            // Show error message
            MFXNotifications.showError("Error", "One or more options are empty");
            optionsErrorLabel.setText("All options must be filled");
            optionsErrorLabel.setVisible(true);
            optionsErrorLabel.setManaged(true);
            LOGGER.warning("One or more options are empty");
            hasErrors = true;
        }

        if (!correctOption1Checkbox.isSelected() && 
            !correctOption2Checkbox.isSelected() && 
            !correctOption3Checkbox.isSelected() && 
            !correctOption4Checkbox.isSelected()) {
            // Show error message
            MFXNotifications.showError("Error", "No correct option selected");
            correctOptionErrorLabel.setText("Please select at least one correct option");
            correctOptionErrorLabel.setVisible(true);
            correctOptionErrorLabel.setManaged(true);
            LOGGER.warning("No correct option selected");
            hasErrors = true;
        }

        if (schoolComboBox.getValue() == null) {
            // Show error message
            MFXNotifications.showError("Error", "Please select a school");
            schoolErrorLabel.setText("Please select a school");
            schoolErrorLabel.setVisible(true);
            schoolErrorLabel.setManaged(true);
            LOGGER.warning("No school selected");
            hasErrors = true;
        }

        if (subjectComboBox.getValue() == null) {
            // Show error message
            MFXNotifications.showError("Error", "Please select a subject");
            subjectErrorLabel.setText("Please select a subject");
            subjectErrorLabel.setVisible(true);
            subjectErrorLabel.setManaged(true);
            LOGGER.warning("No subject selected");
            hasErrors = true;
        }

        // Check if we've reached the maximum number of questions for this subject
        if (subjectComboBox.getValue() != null) {
            String subject = subjectComboBox.getValue();
            int currentQuestions = questionsPerSubjectMap.getOrDefault(subject, 0);
            int maxQuestions = maxQuestionsPerSubjectMap.getOrDefault(subject, 0);

            if (currentQuestions >= maxQuestions) {
                // Show error message
                MFXNotifications.showError("Error", "Maximum number of questions reached for this subject");
                subjectErrorLabel.setText("Maximum number of questions reached for this subject");
                subjectErrorLabel.setVisible(true);
                subjectErrorLabel.setManaged(true);
                LOGGER.warning("Maximum number of questions reached for subject: " + subject);
                hasErrors = true;
            }
        }

        // If there are validation errors, don't proceed
        if (hasErrors) {
            return;
        }

        // Get form data
        String questionText = questionTextArea.getText().trim();
        String option1 = option1TextField.getText().trim();
        String option2 = option2TextField.getText().trim();
        String option3 = option3TextField.getText().trim();
        String option4 = option4TextField.getText().trim();
        int correctOptionIndex = correctOption1Checkbox.isSelected() ? 0 :
                           correctOption2Checkbox.isSelected() ? 1 :
                           correctOption3Checkbox.isSelected() ? 2 : 3;
        boolean includeImage = includeImageToggle.isSelected();
        boolean latexSupport = latexSupportToggle.isSelected();
        String imagePath = includeImage ? (uploadedImageUrl != null ? uploadedImageUrl : imageNameLabel.getText()) : null;
        String school = schoolComboBox.getValue();
        String subject = subjectComboBox.getValue();
        boolean isMockExam = mockExamCheckbox.isSelected();

        try {
            // Create or get the current question paper
            if (currentQuestionPaper == null) {
                // First try to load the most recent question paper
                loadMostRecentQuestionPaper();

                // If still null, create a new one
                if (currentQuestionPaper == null) {
                    String title = "Question Paper - " + school;
                    String description = "Question paper for " + (isMockExam ? "mock exam" : "actual exam");

                    int questionPaperId = createQuestionPaper(title, description, school, isMockExam);

                    if (questionPaperId == -1) {
                        MFXNotifications.showError("Error", "Failed to create question paper");
                        return;
                    }
                }
            }

            // Create a list of options
            List<String> options = new ArrayList<>();
            options.add(option1);
            options.add(option2);
            options.add(option3);
            options.add(option4);

            // Add the question to the database
            int questionId = QuestionPaperDAO.addQuestion(
                currentQuestionPaper.getId(),
                questionText,
                includeImage,
                imagePath,
                latexSupport,
                options,
                correctOptionIndex,
                subject
            );

            if (questionId != -1) {
                // Create a Question object and add it to the list
                Question question = new Question(
                    currentQuestionPaper.getId(),
                    questionText,
                    includeImage,
                    imagePath,
                    latexSupport,
                    subject
                );
                question.setId(questionId);

                // Update the questions count for this subject
                int currentCount = questionsPerSubjectMap.getOrDefault(subject, 0);
                questionsPerSubjectMap.put(subject, currentCount + 1);

                // Update the remaining questions count display
                updateRemainingQuestionsCount(subject);

                // Add options to the question
                for (int i = 0; i < options.size(); i++) {
                    QuestionOption option = new QuestionOption(
                        questionId,
                        options.get(i),
                        i == correctOptionIndex,
                        i + 1
                    );
                    question.addOption(option);
                }

                // Add the question to the current question paper
                currentQuestionPaper.addQuestion(question);

                // Add the question to the appropriate list based on exam type
                if (isMockExam) {
                    mockExamQuestions.add(question);
                } else {
                    actualExamQuestions.add(question);
                }

                // Add to the main questions list for display
                questions.add(question);

                // Show success message
                MFXNotifications.showSuccess("Success", "Question added successfully");
                LOGGER.info("Added question with ID: " + questionId);

                // Log the question data
                LOGGER.info("Question text: " + questionText);
                LOGGER.info("Options: " + option1 + ", " + option2 + ", " + option3 + ", " + option4);
                LOGGER.info("Correct option index: " + correctOptionIndex);
                LOGGER.info("Include image: " + includeImage + ", Image path: " + imagePath);
                LOGGER.info("LaTeX support: " + latexSupport);
                LOGGER.info("School: " + school);
                LOGGER.info("Is mock exam: " + isMockExam);

                // Update the question list
                updateQuestionList();

                // Clear the form after adding the question
                clearForm();
            } else {
                MFXNotifications.showError("Error", "Failed to add question");
                LOGGER.warning("Failed to add question");
            }
        } catch (Exception e) {
            MFXNotifications.showError("Error", "An error occurred: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error adding question", e);
        }
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Question Paper screen
     */
    void cleanup() {
        LOGGER.info("Cleaning up QuestionPaperController before navigation");

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
            LOGGER.info("Auth state change detected in QuestionPaperController");

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
        LOGGER.info("Subscribed to auth state changes in QuestionPaperController");

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
        LOGGER.info("Question Paper scene became active, refreshing auth UI");

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
        LOGGER.info("Refreshing QuestionPaperController UI");

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
                LOGGER.info("Updated profile button in Question Paper page");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating UI components in QuestionPaperController: {0}", e.getMessage());
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
                LOGGER.log(Level.SEVERE, "Error updating containers in QuestionPaperController: {0}", e.getMessage());
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
