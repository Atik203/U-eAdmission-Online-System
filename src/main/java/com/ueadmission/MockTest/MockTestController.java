package com.ueadmission.MockTest;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.*;

public class MockTestController {

    @FXML private Label timeLabel, questionText, questionNumberLabel;
    @FXML private ProgressBar timeProgress;
    @FXML private VBox testContent, resultsContainer;
    @FXML private ToggleGroup optionsGroup;
    @FXML private RadioButton option1, option2, option3, option4;
    @FXML private Label scoreLabel, correctLabel, wrongLabel, unansweredLabel;
    @FXML private Label mathScoreLabel, englishScoreLabel, analyticalScoreLabel;
    @FXML private ProgressBar mathProgress, englishProgress, analyticalProgress;
    @FXML private TextArea recommendationsText;
    @FXML private MFXButton startTestBtn, prevQuestionBtn, nextQuestionBtn, submitTestBtn;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int[] userAnswers; // store selected option index
    private Timeline timer;
    private int timeRemaining = 60 * 60; // 60 minutes in seconds

    @FXML
    public void initialize() {
        // Ensure ToggleGroup is initialized (in case not set in FXML)
        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);

        loadQuestions();
        userAnswers = new int[questions.size()];
        Arrays.fill(userAnswers, -1);

        startTestBtn.setOnAction(e -> startTest());
        prevQuestionBtn.setOnAction(e -> showPreviousQuestion());
        nextQuestionBtn.setOnAction(e -> showNextQuestion());
        submitTestBtn.setOnAction(e -> submitTest());

        optionsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                int selectedIndex = optionsGroup.getToggles().indexOf(newToggle);
                userAnswers[currentQuestionIndex] = selectedIndex;
            }
        });

        // Initially hide test and results; only show start button
        testContent.setVisible(false);
        resultsContainer.setVisible(false);
    }

    private void loadQuestions() {
        questions = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            questions.add(new Question(
                    "Question " + i + " text",
                    new String[]{"Option A", "Option B", "Option C", "Option D"},
                    new Random().nextInt(4) // random correct option
            ));
        }
    }

    private void startTest() {
        testContent.setVisible(true);
        resultsContainer.setVisible(false);
        startTimer();
        showQuestion(0);
    }

    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            int minutes = timeRemaining / 60;
            int seconds = timeRemaining % 60;
            timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
            timeProgress.setProgress((double) timeRemaining / 3600);

            if (timeRemaining <= 0) {
                timer.stop();
                submitTest();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void showQuestion(int index) {
        currentQuestionIndex = index;
        Question q = questions.get(index);
        questionText.setText(q.getText());
        option1.setText(q.getOptions()[0]);
        option2.setText(q.getOptions()[1]);
        option3.setText(q.getOptions()[2]);
        option4.setText(q.getOptions()[3]);
        questionNumberLabel.setText("Question " + (index + 1) + " of 60");

        // Clear selection or restore previous answer
        optionsGroup.selectToggle(null);
        if (userAnswers[index] != -1) {
            optionsGroup.selectToggle(optionsGroup.getToggles().get(userAnswers[index]));
        }
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            showQuestion(currentQuestionIndex + 1);
        }
    }

    private void showPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            showQuestion(currentQuestionIndex - 1);
        }
    }

    private void submitTest() {
        if (timer != null) timer.stop();

        int correct = 0, wrong = 0, unanswered = 0;
        int math = 0, english = 0, analytical = 0;

        for (int i = 0; i < questions.size(); i++) {
            int answer = userAnswers[i];
            int correctAnswer = questions.get(i).getCorrectIndex();

            if (answer == -1) {
                unanswered++;
            } else if (answer == correctAnswer) {
                correct++;
            } else {
                wrong++;
            }

            if (i < 20) math += (answer == correctAnswer) ? 1 : 0;
            else if (i < 40) english += (answer == correctAnswer) ? 1 : 0;
            else analytical += (answer == correctAnswer) ? 1 : 0;
        }

        scoreLabel.setText(String.valueOf(correct)); // 1 point per correct
        correctLabel.setText(String.valueOf(correct));
        wrongLabel.setText(String.valueOf(wrong));
        unansweredLabel.setText(String.valueOf(unanswered));

        mathScoreLabel.setText(math + "/20");
        englishScoreLabel.setText(english + "/20");
        analyticalScoreLabel.setText(analytical + "/20");

        mathProgress.setProgress(math / 20.0);
        englishProgress.setProgress(english / 20.0);
        analyticalProgress.setProgress(analytical / 20.0);

        recommendationsText.setText(
                "✅ Focus more on English.\n" +
                        "✅ Review analytical problems.\n" +
                        "✅ Keep practicing math for perfection."
        );

        testContent.setVisible(false);
        resultsContainer.setVisible(true);
    }

    static class Question {
        private final String text;
        private final String[] options;
        private final int correctIndex;

        public Question(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }

        public String getText() { return text; }
        public String[] getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
    }
}
