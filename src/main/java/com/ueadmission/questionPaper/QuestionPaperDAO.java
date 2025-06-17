package com.ueadmission.questionPaper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.db.DatabaseConnection;

/**
 * Data Access Object for Question Paper operations
 * Handles database operations for question papers, questions, and options
 */
public class QuestionPaperDAO {
    private static final Logger LOGGER = Logger.getLogger(QuestionPaperDAO.class.getName());
    
    /**
     * Create a new question paper
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param createdBy The ID of the user who created the question paper
     * @return The ID of the newly created question paper, or -1 if creation failed
     */
    public static int createQuestionPaper(String title, String description, String school, boolean isMockExam, int createdBy) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int questionPaperId = -1;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO question_papers (title, description, school, is_mock_exam, created_by) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, school);
            ps.setBoolean(4, isMockExam);
            ps.setInt(5, createdBy);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    questionPaperId = rs.getInt(1);
                    LOGGER.info("Created question paper with ID: " + questionPaperId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating question paper", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
        
        return questionPaperId;
    }
    
    /**
     * Add a question to a question paper
     * 
     * @param questionPaperId The ID of the question paper
     * @param questionText The text of the question
     * @param hasImage Whether the question has an image
     * @param imagePath The path to the image (if hasImage is true)
     * @param hasLatex Whether the question has LaTeX content
     * @param options List of option texts
     * @param correctOptionIndex The index of the correct option (0-based)
     * @return The ID of the newly created question, or -1 if creation failed
     */
    public static int addQuestion(int questionPaperId, String questionText, boolean hasImage, 
                                 String imagePath, boolean hasLatex, List<String> options, int correctOptionIndex) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int questionId = -1;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert question
            String sql = "INSERT INTO questions (question_paper_id, question_text, has_image, image_path, has_latex) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, questionPaperId);
            ps.setString(2, questionText);
            ps.setBoolean(3, hasImage);
            ps.setString(4, imagePath);
            ps.setBoolean(5, hasLatex);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    questionId = rs.getInt(1);
                    LOGGER.info("Created question with ID: " + questionId);
                    
                    // Insert options
                    if (options != null && !options.isEmpty()) {
                        DatabaseConnection.closeResources(ps, rs);
                        ps = null;
                        rs = null;
                        
                        sql = "INSERT INTO question_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?)";
                        ps = conn.prepareStatement(sql);
                        
                        for (int i = 0; i < options.size(); i++) {
                            ps.setInt(1, questionId);
                            ps.setString(2, options.get(i));
                            ps.setBoolean(3, i == correctOptionIndex);
                            ps.setInt(4, i + 1);
                            ps.addBatch();
                        }
                        
                        int[] optionResults = ps.executeBatch();
                        LOGGER.info("Added " + optionResults.length + " options to question ID: " + questionId);
                    }
                    
                    // Commit transaction
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding question", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
            }
            questionId = -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error resetting auto-commit", e);
            }
            DatabaseConnection.closeResources(ps, rs);
        }
        
        return questionId;
    }
    
    /**
     * Get all question papers
     * 
     * @return List of question papers
     */
    public static List<QuestionPaper> getAllQuestionPapers() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<QuestionPaper> questionPapers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM question_papers ORDER BY created_at DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                QuestionPaper paper = new QuestionPaper();
                paper.setId(rs.getInt("id"));
                paper.setTitle(rs.getString("title"));
                paper.setDescription(rs.getString("description"));
                paper.setSchool(rs.getString("school"));
                paper.setMockExam(rs.getBoolean("is_mock_exam"));
                paper.setCreatedBy(rs.getInt("created_by"));
                paper.setCreatedAt(rs.getTimestamp("created_at"));
                paper.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                questionPapers.add(paper);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting question papers", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
        
        return questionPapers;
    }
    
    /**
     * Get questions for a question paper
     * 
     * @param questionPaperId The ID of the question paper
     * @return List of questions
     */
    public static List<Question> getQuestionsForPaper(int questionPaperId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Question> questions = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM questions WHERE question_paper_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, questionPaperId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setQuestionPaperId(rs.getInt("question_paper_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setHasImage(rs.getBoolean("has_image"));
                question.setImagePath(rs.getString("image_path"));
                question.setHasLatex(rs.getBoolean("has_latex"));
                question.setCreatedAt(rs.getTimestamp("created_at"));
                question.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                // Get options for this question
                question.setOptions(getOptionsForQuestion(question.getId()));
                
                questions.add(question);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting questions for paper", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
        
        return questions;
    }
    
    /**
     * Get options for a question
     * 
     * @param questionId The ID of the question
     * @return List of options
     */
    private static List<QuestionOption> getOptionsForQuestion(int questionId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<QuestionOption> options = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM question_options WHERE question_id = ? ORDER BY option_order";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, questionId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                QuestionOption option = new QuestionOption();
                option.setId(rs.getInt("id"));
                option.setQuestionId(rs.getInt("question_id"));
                option.setOptionText(rs.getString("option_text"));
                option.setCorrect(rs.getBoolean("is_correct"));
                option.setOptionOrder(rs.getInt("option_order"));
                
                options.add(option);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting options for question", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
        
        return options;
    }
    
    /**
     * Initialize the database schema for question papers
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeQuestionPaperSchema() {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Load SQL script from resources
            java.io.InputStream inputStream = QuestionPaperDAO.class.getResourceAsStream("/database/question_paper.sql");
            
            if (inputStream == null) {
                LOGGER.severe("Could not find question_paper.sql script in resources");
                return false;
            }
            
            // Read the SQL script
            String sql = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))
                    .lines().collect(java.util.stream.Collectors.joining("\n"));
            
            // Split the SQL script on semicolons
            String[] statements = sql.split(";");
            
            // Execute each statement
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        ps = conn.prepareStatement(statement);
                        ps.execute();
                        DatabaseConnection.closeResources(ps, null);
                        ps = null;
                    } catch (SQLException e) {
                        // Log but continue with remaining statements
                        LOGGER.log(Level.WARNING, "Error executing SQL statement: " + e.getMessage(), e);
                    }
                }
            }
            
            LOGGER.info("Question paper schema initialized successfully");
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing question paper schema", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }
}