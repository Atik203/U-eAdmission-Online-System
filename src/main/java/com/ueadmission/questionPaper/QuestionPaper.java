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
    private Integer totalQuestions;
    private String subjects;
    private String questionsPerSubject;
    private Integer timeLimitMinutes;
    private Integer totalMarks;
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

    /**
     * Constructor with all fields
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param totalQuestions The total number of questions
     * @param subjects The subjects included in the exam
     * @param questionsPerSubject The number of questions per subject
     * @param timeLimitMinutes The time limit in minutes
     * @param totalMarks The total marks for the exam
     * @param createdBy The ID of the user who created the question paper
     */
    public QuestionPaper(String title, String description, String school, boolean isMockExam, 
                         Integer totalQuestions, String subjects, String questionsPerSubject, 
                         Integer timeLimitMinutes, Integer totalMarks, int createdBy) {
        this.title = title;
        this.description = description;
        this.school = school;
        this.isMockExam = isMockExam;
        this.totalQuestions = totalQuestions;
        this.subjects = subjects;
        this.questionsPerSubject = questionsPerSubject;
        this.timeLimitMinutes = timeLimitMinutes;
        this.totalMarks = totalMarks;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getQuestionsPerSubject() {
        return questionsPerSubject;
    }

    public void setQuestionsPerSubject(String questionsPerSubject) {
        this.questionsPerSubject = questionsPerSubject;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
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
