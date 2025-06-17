package com.ueadmission.questionPaper;

import java.sql.Timestamp;

/**
 * Model class for Question Option
 * Represents a single option for a multiple choice question
 */
public class QuestionOption {
    private int id;
    private int questionId;
    private String optionText;
    private boolean isCorrect;
    private int optionOrder;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    /**
     * Default constructor
     */
    public QuestionOption() {
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param questionId The ID of the question this option belongs to
     * @param optionText The text of the option
     * @param isCorrect Whether this option is the correct answer
     * @param optionOrder The order of this option (1-based)
     */
    public QuestionOption(int questionId, String optionText, boolean isCorrect, int optionOrder) {
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.optionOrder = optionOrder;
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    
    public String getOptionText() {
        return optionText;
    }
    
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
    public int getOptionOrder() {
        return optionOrder;
    }
    
    public void setOptionOrder(int optionOrder) {
        this.optionOrder = optionOrder;
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
    
    @Override
    public String toString() {
        return "QuestionOption{" +
                "id=" + id +
                ", optionText='" + optionText + '\'' +
                ", isCorrect=" + isCorrect +
                ", optionOrder=" + optionOrder +
                '}';
    }
}