package com.ueadmission.questionPaper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Question Paper
 * Represents a collection of questions for an exam or mock test
 */
public class QuestionPaper {
    private int id;
    private String title;
    private String description;
    private String school;
    private boolean isMockExam;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Question> questions;
    
    /**
     * Default constructor
     */
    public QuestionPaper() {
        this.questions = new ArrayList<>();
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param createdBy The ID of the user who created the question paper
     */
    public QuestionPaper(String title, String description, String school, boolean isMockExam, int createdBy) {
        this.title = title;
        this.description = description;
        this.school = school;
        this.isMockExam = isMockExam;
        this.createdBy = createdBy;
        this.questions = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSchool() {
        return school;
    }
    
    public void setSchool(String school) {
        this.school = school;
    }
    
    public boolean isMockExam() {
        return isMockExam;
    }
    
    public void setMockExam(boolean isMockExam) {
        this.isMockExam = isMockExam;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    /**
     * Add a question to this question paper
     * 
     * @param question The question to add
     */
    public void addQuestion(Question question) {
        this.questions.add(question);
    }
    
    @Override
    public String toString() {
        return "QuestionPaper{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", school='" + school + '\'' +
                ", isMockExam=" + isMockExam +
                ", questionsCount=" + (questions != null ? questions.size() : 0) +
                '}';
    }
}