package com.ueadmission.questionPaper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Question
 * Represents a single question in a question paper
 */
public class Question {
    private int id;
    private int questionPaperId;
    private String questionText;
    private boolean hasImage;
    private String imagePath;
    private boolean hasLatex;
    private String subject; // Added subject field
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<QuestionOption> options;

    /**
     * Default constructor
     */
    public Question() {
        this.options = new ArrayList<>();
    }

    /**
     * Constructor with basic fields
     * 
     * @param questionPaperId The ID of the question paper this question belongs to
     * @param questionText The text of the question
     * @param hasImage Whether the question has an image
     * @param imagePath The path to the image (if hasImage is true)
     * @param hasLatex Whether the question has LaTeX content
     */
    public Question(int questionPaperId, String questionText, boolean hasImage, String imagePath, boolean hasLatex) {
        this.questionPaperId = questionPaperId;
        this.questionText = questionText;
        this.hasImage = hasImage;
        this.imagePath = imagePath;
        this.hasLatex = hasLatex;
        this.options = new ArrayList<>();
    }

    /**
     * Constructor with subject field
     * 
     * @param questionPaperId The ID of the question paper this question belongs to
     * @param questionText The text of the question
     * @param hasImage Whether the question has an image
     * @param imagePath The path to the image (if hasImage is true)
     * @param hasLatex Whether the question has LaTeX content
     * @param subject The subject this question belongs to
     */
    public Question(int questionPaperId, String questionText, boolean hasImage, String imagePath, boolean hasLatex, String subject) {
        this.questionPaperId = questionPaperId;
        this.questionText = questionText;
        this.hasImage = hasImage;
        this.imagePath = imagePath;
        this.hasLatex = hasLatex;
        this.subject = subject;
        this.options = new ArrayList<>();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionPaperId() {
        return questionPaperId;
    }

    public void setQuestionPaperId(int questionPaperId) {
        this.questionPaperId = questionPaperId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isHasLatex() {
        return hasLatex;
    }

    public void setHasLatex(boolean hasLatex) {
        this.hasLatex = hasLatex;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }

    /**
     * Add an option to this question
     * 
     * @param option The option to add
     */
    public void addOption(QuestionOption option) {
        this.options.add(option);
    }

    /**
     * Get the correct option for this question
     * 
     * @return The correct option, or null if no correct option is found
     */
    public QuestionOption getCorrectOption() {
        for (QuestionOption option : options) {
            if (option.isCorrect()) {
                return option;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", hasImage=" + hasImage +
                ", hasLatex=" + hasLatex +
                ", optionsCount=" + (options != null ? options.size() : 0) +
                '}';
    }
}
