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
     * Create a new question paper (simplified version)
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param createdBy The ID of the user who created the question paper
     * @return The ID of the newly created question paper, or -1 if creation failed
     */
    public static int createQuestionPaper(String title, String description, String school, boolean isMockExam, int createdBy) {
        // Set default values based on school and exam type
        Integer totalQuestions = isMockExam ? 75 : 100;
        Integer timeLimitMinutes = isMockExam ? 75 : 120;
        Integer totalMarks = isMockExam ? 75 : 100;
        String subjects = "";
        String questionsPerSubject = "";

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

        // Call the full version of the method with the default values
        return createQuestionPaper(title, description, school, isMockExam, 
                                  totalQuestions, subjects, questionsPerSubject, 
                                  timeLimitMinutes, totalMarks, createdBy);
    }

    /**
     * Create a new question paper
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
     * @return The ID of the newly created question paper, or -1 if creation failed
     */
    public static int createQuestionPaper(String title, String description, String school, boolean isMockExam, 
                                         Integer totalQuestions, String subjects, String questionsPerSubject, 
                                         Integer timeLimitMinutes, Integer totalMarks, int createdBy) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int questionPaperId = -1;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "INSERT INTO question_papers (title, description, school, is_mock_exam, total_questions, subjects, " +
                         "questions_per_subject, time_limit_minutes, total_marks, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, school);
            ps.setBoolean(4, isMockExam);

            // Set the new fields, handling null values
            if (totalQuestions != null) {
                ps.setInt(5, totalQuestions);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.setString(6, subjects);
            ps.setString(7, questionsPerSubject);

            if (timeLimitMinutes != null) {
                ps.setInt(8, timeLimitMinutes);
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            if (totalMarks != null) {
                ps.setInt(9, totalMarks);
            } else {
                ps.setNull(9, java.sql.Types.INTEGER);
            }

            ps.setInt(10, createdBy);

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
     * @param subject The subject this question belongs to
     * @return The ID of the newly created question, or -1 if creation failed
     */
    public static int addQuestion(int questionPaperId, String questionText, boolean hasImage, 
                                 String imagePath, boolean hasLatex, List<String> options, int correctOptionIndex, String subject) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int questionId = -1;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert question
            String sql = "INSERT INTO questions (question_paper_id, question_text, has_image, image_path, has_latex, subject) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, questionPaperId);
            ps.setString(2, questionText);
            ps.setBoolean(3, hasImage);
            ps.setString(4, imagePath);
            ps.setBoolean(5, hasLatex);
            ps.setString(6, subject);

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

                // Get the new fields, handling null values
                try {
                    paper.setTotalQuestions(rs.getObject("total_questions") != null ? rs.getInt("total_questions") : null);
                    paper.setSubjects(rs.getString("subjects"));
                    paper.setQuestionsPerSubject(rs.getString("questions_per_subject"));
                    paper.setTimeLimitMinutes(rs.getObject("time_limit_minutes") != null ? rs.getInt("time_limit_minutes") : null);
                    paper.setTotalMarks(rs.getObject("total_marks") != null ? rs.getInt("total_marks") : null);
                } catch (SQLException e) {
                    // Log but continue - this might happen if the columns don't exist yet
                    LOGGER.log(Level.WARNING, "Error getting new fields from question_papers table: " + e.getMessage());
                }

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
     * Get the most recent question paper
     * 
     * @return The most recent question paper, or null if none exists
     */
    public static QuestionPaper getMostRecentQuestionPaper() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        QuestionPaper paper = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM question_papers ORDER BY created_at DESC LIMIT 1";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                paper = new QuestionPaper();
                paper.setId(rs.getInt("id"));
                paper.setTitle(rs.getString("title"));
                paper.setDescription(rs.getString("description"));
                paper.setSchool(rs.getString("school"));
                paper.setMockExam(rs.getBoolean("is_mock_exam"));

                // Get the new fields, handling null values
                try {
                    paper.setTotalQuestions(rs.getObject("total_questions") != null ? rs.getInt("total_questions") : null);
                    paper.setSubjects(rs.getString("subjects"));
                    paper.setQuestionsPerSubject(rs.getString("questions_per_subject"));
                    paper.setTimeLimitMinutes(rs.getObject("time_limit_minutes") != null ? rs.getInt("time_limit_minutes") : null);
                    paper.setTotalMarks(rs.getObject("total_marks") != null ? rs.getInt("total_marks") : null);
                } catch (SQLException e) {
                    // Log but continue - this might happen if the columns don't exist yet
                    LOGGER.log(Level.WARNING, "Error getting new fields from question_papers table: " + e.getMessage());
                }

                paper.setCreatedBy(rs.getInt("created_by"));
                paper.setCreatedAt(rs.getTimestamp("created_at"));
                paper.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Load questions for this paper
                List<Question> questions = getQuestionsForPaper(paper.getId());
                for (Question question : questions) {
                    paper.addQuestion(question);
                }

                LOGGER.info("Loaded most recent question paper with ID: " + paper.getId() + " and " + questions.size() + " questions");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting most recent question paper", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return paper;
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

                // Get subject if it exists in the database
                try {
                    question.setSubject(rs.getString("subject"));
                } catch (SQLException e) {
                    // Column might not exist in older database versions
                    LOGGER.log(Level.WARNING, "Subject column not found in questions table: " + e.getMessage());
                }

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
     * Reset all question paper related tables
     * 
     * @return true if reset was successful, false otherwise
     */
    public static boolean resetQuestionPaperTables() {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Disable foreign key checks to allow dropping tables with dependencies
            ps = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
            ps.execute();
            DatabaseConnection.closeResources(ps, null);
            ps = null;

            // Drop tables in reverse order of dependencies
            String[] tables = {
                "exam_sessions",
                "student_responses",
                "question_options",
                "questions",
                "question_papers"
            };

            for (String table : tables) {
                ps = conn.prepareStatement("DROP TABLE IF EXISTS " + table);
                ps.execute();
                DatabaseConnection.closeResources(ps, null);
                ps = null;
                LOGGER.info("Dropped table: " + table);
            }

            // Re-enable foreign key checks
            ps = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1");
            ps.execute();
            DatabaseConnection.closeResources(ps, null);
            ps = null;

            // Initialize the schema again
            boolean result = initializeQuestionPaperSchema();

            LOGGER.info("Question paper tables reset successfully");
            return result;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting question paper tables", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Initialize the database schema for question papers
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeQuestionPaperSchema() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

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

            // Check if the question_papers table has the required columns
            // If not, add them
            try {
                // Check if total_questions column exists
                String checkColumnSql = "SELECT * FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'question_papers' AND column_name = 'total_questions'";
                ps = conn.prepareStatement(checkColumnSql);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    // Column doesn't exist, add it
                    LOGGER.info("Adding missing total_questions column to question_papers table");
                    DatabaseConnection.closeResources(ps, rs);
                    ps = null;
                    rs = null;

                    String alterTableSql = "ALTER TABLE question_papers ADD COLUMN total_questions INT";
                    ps = conn.prepareStatement(alterTableSql);
                    ps.execute();
                    DatabaseConnection.closeResources(ps, null);
                    ps = null;
                }

                // Check if subjects column exists
                checkColumnSql = "SELECT * FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'question_papers' AND column_name = 'subjects'";
                ps = conn.prepareStatement(checkColumnSql);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    // Column doesn't exist, add it
                    LOGGER.info("Adding missing subjects column to question_papers table");
                    DatabaseConnection.closeResources(ps, rs);
                    ps = null;
                    rs = null;

                    String alterTableSql = "ALTER TABLE question_papers ADD COLUMN subjects TEXT";
                    ps = conn.prepareStatement(alterTableSql);
                    ps.execute();
                    DatabaseConnection.closeResources(ps, null);
                    ps = null;
                }

                // Check if questions_per_subject column exists
                checkColumnSql = "SELECT * FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'question_papers' AND column_name = 'questions_per_subject'";
                ps = conn.prepareStatement(checkColumnSql);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    // Column doesn't exist, add it
                    LOGGER.info("Adding missing questions_per_subject column to question_papers table");
                    DatabaseConnection.closeResources(ps, rs);
                    ps = null;
                    rs = null;

                    String alterTableSql = "ALTER TABLE question_papers ADD COLUMN questions_per_subject TEXT";
                    ps = conn.prepareStatement(alterTableSql);
                    ps.execute();
                    DatabaseConnection.closeResources(ps, null);
                    ps = null;
                }

                // Check if time_limit_minutes column exists
                checkColumnSql = "SELECT * FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'question_papers' AND column_name = 'time_limit_minutes'";
                ps = conn.prepareStatement(checkColumnSql);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    // Column doesn't exist, add it
                    LOGGER.info("Adding missing time_limit_minutes column to question_papers table");
                    DatabaseConnection.closeResources(ps, rs);
                    ps = null;
                    rs = null;

                    String alterTableSql = "ALTER TABLE question_papers ADD COLUMN time_limit_minutes INT";
                    ps = conn.prepareStatement(alterTableSql);
                    ps.execute();
                    DatabaseConnection.closeResources(ps, null);
                    ps = null;
                }

                // Check if total_marks column exists
                checkColumnSql = "SELECT * FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'question_papers' AND column_name = 'total_marks'";
                ps = conn.prepareStatement(checkColumnSql);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    // Column doesn't exist, add it
                    LOGGER.info("Adding missing total_marks column to question_papers table");
                    DatabaseConnection.closeResources(ps, rs);
                    ps = null;
                    rs = null;

                    String alterTableSql = "ALTER TABLE question_papers ADD COLUMN total_marks INT";
                    ps = conn.prepareStatement(alterTableSql);
                    ps.execute();
                    DatabaseConnection.closeResources(ps, null);
                    ps = null;
                }

                // Check if subject column exists in questions table
                checkColumnSql = "SELECT * FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'questions' AND column_name = 'subject'";
                ps = conn.prepareStatement(checkColumnSql);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    // Column doesn't exist, add it
                    LOGGER.info("Adding missing subject column to questions table");
                    DatabaseConnection.closeResources(ps, rs);
                    ps = null;
                    rs = null;

                    String alterTableSql = "ALTER TABLE questions ADD COLUMN subject VARCHAR(255)";
                    ps = conn.prepareStatement(alterTableSql);
                    ps.execute();
                    DatabaseConnection.closeResources(ps, null);
                    ps = null;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error checking or adding columns to tables: " + e.getMessage(), e);
            }

            LOGGER.info("Question paper schema initialized successfully");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing question paper schema", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
}
