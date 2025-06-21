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
 * Updated to work with the new database schema
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

        // Call the full version of the method with the default values
        return createQuestionPaper(title, description, school, isMockExam, 
                                  totalQuestions, timeLimitMinutes, totalMarks, createdBy);
    }

    /**
     * Create a new question paper
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param totalQuestions The total number of questions
     * @param timeLimitMinutes The time limit in minutes
     * @param totalMarks The total marks for the exam
     * @param createdBy The ID of the user who created the question paper
     * @return The ID of the newly created question paper, or -1 if creation failed
     */
    public static int createQuestionPaper(String title, String description, String school, boolean isMockExam, 
                                         Integer totalQuestions, Integer timeLimitMinutes, Integer totalMarks, int createdBy) {
        return createQuestionPaper(title, description, school, isMockExam, 
                                  totalQuestions, null, null, timeLimitMinutes, totalMarks, createdBy);
    }

    /**
     * Create a new question paper with subjects and questionsPerSubject
     * 
     * @param title The title of the question paper
     * @param description The description of the question paper
     * @param school The school the question paper belongs to
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param totalQuestions The total number of questions
     * @param subjects The subjects included in the exam (comma-separated)
     * @param questionsPerSubject The number of questions per subject (comma-separated)
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

            // First, get the school_id and exam_type_id
            int schoolId = getSchoolId(conn, school);
            int examTypeId = getExamTypeId(conn, isMockExam);

            if (schoolId == -1 || examTypeId == -1) {
                LOGGER.warning("Failed to get school_id or exam_type_id");
                return -1;
            }

            String sql = "INSERT INTO question_papers (title, description, school_id, exam_type_id, " +
                         "total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, schoolId);
            ps.setInt(4, examTypeId);

            // Set the new fields, handling null values
            if (totalQuestions != null) {
                ps.setInt(5, totalQuestions);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            // Set subjects and questionsPerSubject
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
     * Get the school ID from the school name
     * 
     * @param conn The database connection
     * @param schoolName The name of the school
     * @return The ID of the school, or -1 if not found
     */
    private static int getSchoolId(Connection conn, String schoolName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int schoolId = -1;

        try {
            String sql = "SELECT id FROM schools WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, schoolName);
            rs = ps.executeQuery();

            if (rs.next()) {
                schoolId = rs.getInt("id");
            }
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return schoolId;
    }

    /**
     * Get the exam type ID from the is_mock_exam flag
     * 
     * @param conn The database connection
     * @param isMockExam Whether this is a mock exam or actual exam
     * @return The ID of the exam type, or -1 if not found
     */
    private static int getExamTypeId(Connection conn, boolean isMockExam) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int examTypeId = -1;

        try {
            String sql = "SELECT id FROM exam_types WHERE is_mock_exam = ?";
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, isMockExam);
            rs = ps.executeQuery();

            if (rs.next()) {
                examTypeId = rs.getInt("id");
            }
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return examTypeId;
    }

    /**
     * Get the subject ID from the subject name
     * 
     * @param conn The database connection
     * @param subjectName The name of the subject
     * @return The ID of the subject, or -1 if not found
     */
    private static int getSubjectId(Connection conn, String subjectName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int subjectId = -1;

        try {
            String sql = "SELECT id FROM subjects WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, subjectName);
            rs = ps.executeQuery();

            if (rs.next()) {
                subjectId = rs.getInt("id");
            }
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return subjectId;
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

            // Get the subject ID
            int subjectId = getSubjectId(conn, subject);
            if (subjectId == -1) {
                LOGGER.warning("Subject not found: " + subject);
                return -1;
            }

            // Insert question
            String sql = "INSERT INTO questions (question_paper_id, subject_id, question_text, has_image, image_path, has_latex) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, questionPaperId);
            ps.setInt(2, subjectId);
            ps.setString(3, questionText);
            ps.setBoolean(4, hasImage);
            ps.setString(5, imagePath);
            ps.setBoolean(6, hasLatex);

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

            String sql = "SELECT qp.*, s.name as school_name, et.is_mock_exam " +
                         "FROM question_papers qp " +
                         "JOIN schools s ON qp.school_id = s.id " +
                         "JOIN exam_types et ON qp.exam_type_id = et.id " +
                         "ORDER BY qp.created_at DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                QuestionPaper paper = new QuestionPaper();
                paper.setId(rs.getInt("id"));
                paper.setTitle(rs.getString("title"));
                paper.setDescription(rs.getString("description"));
                paper.setSchool(rs.getString("school_name"));
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
        return getMostRecentQuestionPaper(null);
    }

    /**
     * Get the most recent question paper with the specified mock exam status
     * 
     * @param isMockExam Boolean flag indicating whether to get a mock exam paper (true) or a real exam paper (false).
     *                   If null, returns the most recent paper regardless of type.
     * @return The most recent question paper matching the criteria, or null if none exists
     */
    public static QuestionPaper getMostRecentQuestionPaper(Boolean isMockExam) {
        return getMostRecentQuestionPaper(isMockExam, null);
    }

    /**
     * Get the most recent question paper with the specified mock exam status and school
     * 
     * @param isMockExam Boolean flag indicating whether to get a mock exam paper (true) or a real exam paper (false).
     *                   If null, returns the most recent paper regardless of type.
     * @param school The school to filter by. If null, returns papers from any school.
     * @return The most recent question paper matching the criteria, or null if none exists
     */
    public static QuestionPaper getMostRecentQuestionPaper(Boolean isMockExam, String school) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        QuestionPaper paper = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT qp.*, s.name as school_name, et.is_mock_exam " +
                         "FROM question_papers qp " +
                         "JOIN schools s ON qp.school_id = s.id " +
                         "JOIN exam_types et ON qp.exam_type_id = et.id";
            boolean hasWhere = false;

            if (isMockExam != null) {
                sql += " WHERE et.is_mock_exam = ?";
                hasWhere = true;
            }

            if (school != null && !school.isEmpty()) {
                if (hasWhere) {
                    sql += " AND s.name = ?";
                } else {
                    sql += " WHERE s.name = ?";
                    hasWhere = true;
                }
            }

            sql += " ORDER BY qp.created_at DESC LIMIT 1";

            ps = conn.prepareStatement(sql);

            int paramIndex = 1;
            if (isMockExam != null) {
                ps.setBoolean(paramIndex++, isMockExam);
                LOGGER.info("Filtering question papers by isMockExam = " + isMockExam);
            }

            if (school != null && !school.isEmpty()) {
                ps.setString(paramIndex++, school);
                LOGGER.info("Filtering question papers by school = " + school);
            }

            LOGGER.info("Executing SQL: " + sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                paper = new QuestionPaper();
                paper.setId(rs.getInt("id"));
                paper.setTitle(rs.getString("title"));
                paper.setDescription(rs.getString("description"));
                paper.setSchool(rs.getString("school_name"));
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

                LOGGER.info("Loaded most recent question paper with ID: " + paper.getId() + 
                           ", school: " + paper.getSchool() + 
                           ", isMockExam: " + paper.isMockExam() + 
                           ", subjects: " + paper.getSubjects() + 
                           " and " + questions.size() + " questions");
            } else {
                LOGGER.warning("No question paper found for school: " + school + ", isMockExam: " + isMockExam);
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

            String sql = "SELECT q.*, s.name as subject_name FROM questions q " +
                         "JOIN subjects s ON q.subject_id = s.id " +
                         "WHERE q.question_paper_id = ?";
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

                // Get subject from the joined subjects table
                try {
                    question.setSubject(rs.getString("subject_name"));
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error getting subject name: " + e.getMessage());
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
     * Get subjects for a question paper
     * 
     * @param conn The database connection
     * @param questionPaperId The ID of the question paper
     * @return Comma-separated list of subjects
     */
    private static String getSubjectsForPaper(Connection conn, int questionPaperId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder subjects = new StringBuilder();

        try {
            String sql = "SELECT DISTINCT s.name " +
                         "FROM subjects s " +
                         "JOIN questions q ON s.id = q.subject_id " +
                         "WHERE q.question_paper_id = ? " +
                         "ORDER BY s.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, questionPaperId);
            rs = ps.executeQuery();

            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    subjects.append(", ");
                }
                subjects.append(rs.getString("name"));
                first = false;
            }
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return subjects.toString();
    }

    /**
     * Get questions per subject for a question paper
     * 
     * @param conn The database connection
     * @param questionPaperId The ID of the question paper
     * @return Comma-separated list of question counts per subject
     */
    private static String getQuestionsPerSubjectForPaper(Connection conn, int questionPaperId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder questionsPerSubject = new StringBuilder();

        try {
            String sql = "SELECT s.name, COUNT(q.id) as question_count " +
                         "FROM subjects s " +
                         "JOIN questions q ON s.id = q.subject_id " +
                         "WHERE q.question_paper_id = ? " +
                         "GROUP BY s.name " +
                         "ORDER BY s.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, questionPaperId);
            rs = ps.executeQuery();

            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    questionsPerSubject.append(", ");
                }
                questionsPerSubject.append(rs.getInt("question_count"));
                first = false;
            }
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return questionsPerSubject.toString();
    }

    /**
     * Get the count of questions for a specific school, exam type, and subject
     * 
     * @param school The school name
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param subject The subject name
     * @return The count of questions
     */
    public static int getQuestionCount(String school, boolean isMockExam, String subject) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM questions q " +
                         "JOIN question_papers qp ON q.question_paper_id = qp.id " +
                         "JOIN schools s ON qp.school_id = s.id " +
                         "JOIN exam_types et ON qp.exam_type_id = et.id " +
                         "JOIN subjects subj ON q.subject_id = subj.id " +
                         "WHERE s.name = ? AND et.is_mock_exam = ? AND subj.name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, school);
            ps.setBoolean(2, isMockExam);
            ps.setString(3, subject);
            rs = ps.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting question count", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return count;
    }

    /**
     * Get the total count of questions for a specific school and exam type
     * 
     * @param school The school name
     * @param isMockExam Whether this is a mock exam or actual exam
     * @return The total count of questions
     */
    public static int getTotalQuestionCount(String school, boolean isMockExam) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM questions q " +
                         "JOIN question_papers qp ON q.question_paper_id = qp.id " +
                         "JOIN schools s ON qp.school_id = s.id " +
                         "JOIN exam_types et ON qp.exam_type_id = et.id " +
                         "WHERE s.name = ? AND et.is_mock_exam = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, school);
            ps.setBoolean(2, isMockExam);
            rs = ps.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total question count", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return count;
    }

    /**
     * Get the maximum number of questions for a specific school, exam type, and subject
     * 
     * @param school The school name
     * @param isMockExam Whether this is a mock exam or actual exam
     * @param subject The subject name
     * @return The maximum number of questions
     */
    public static int getMaxQuestions(String school, boolean isMockExam, String subject) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int maxQuestions = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT es.max_questions FROM exam_subjects es " +
                         "JOIN schools s ON es.school_id = s.id " +
                         "JOIN exam_types et ON es.exam_type_id = et.id " +
                         "JOIN subjects subj ON es.subject_id = subj.id " +
                         "WHERE s.name = ? AND et.is_mock_exam = ? AND subj.name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, school);
            ps.setBoolean(2, isMockExam);
            ps.setString(3, subject);
            rs = ps.executeQuery();

            if (rs.next()) {
                maxQuestions = rs.getInt("max_questions");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting max questions", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return maxQuestions;
    }

    /**
     * Get the total maximum number of questions for a specific school and exam type
     * 
     * @param school The school name
     * @param isMockExam Whether this is a mock exam or actual exam
     * @return The total maximum number of questions
     */
    public static int getTotalMaxQuestions(String school, boolean isMockExam) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int totalMaxQuestions = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT SUM(es.max_questions) as total_max FROM exam_subjects es " +
                         "JOIN schools s ON es.school_id = s.id " +
                         "JOIN exam_types et ON es.exam_type_id = et.id " +
                         "WHERE s.name = ? AND et.is_mock_exam = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, school);
            ps.setBoolean(2, isMockExam);
            rs = ps.executeQuery();

            if (rs.next()) {
                totalMaxQuestions = rs.getInt("total_max");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total max questions", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return totalMaxQuestions;
    }

    /**
     * Get all subjects for a specific school and exam type
     * 
     * @param school The school name
     * @param isMockExam Whether this is a mock exam or actual exam
     * @return List of subjects
     */
    public static List<String> getSubjectsForSchool(String school, boolean isMockExam) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> subjects = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT subj.name FROM exam_subjects es " +
                         "JOIN schools s ON es.school_id = s.id " +
                         "JOIN exam_types et ON es.exam_type_id = et.id " +
                         "JOIN subjects subj ON es.subject_id = subj.id " +
                         "WHERE s.name = ? AND et.is_mock_exam = ? " +
                         "ORDER BY subj.name";
            ps = conn.prepareStatement(sql);
            ps.setString(1, school);
            ps.setBoolean(2, isMockExam);
            rs = ps.executeQuery();

            while (rs.next()) {
                subjects.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting subjects for school", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return subjects;
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

            // Load SQL script from resources to recreate the tables
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

            // Execute each statement to recreate the tables
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        ps = conn.prepareStatement(statement);
                        ps.execute();
                        DatabaseConnection.closeResources(ps, null);
                        ps = null;
                    } catch (SQLException e) {
                        // Check if it's a duplicate key error (which is expected when rerunning the script)
                        if (e.getMessage().contains("Duplicate key name")) {
                            // This is expected when indexes already exist, just log as info
                            LOGGER.log(Level.INFO, "Index already exists: " + e.getMessage());
                        } else {
                            // Log other errors as warnings but continue with remaining statements
                            LOGGER.log(Level.WARNING, "Error executing SQL statement: " + e.getMessage(), e);
                        }
                    }
                }
            }

            LOGGER.info("Question paper schema initialized successfully");

            // Load question paper data from question-paper-data.sql
            // This only contains the 8 question paper records (4 schools x 2 exam types)
            try {
                // Load SQL script from resources
                java.io.InputStream sampleDataStream = QuestionPaperDAO.class.getResourceAsStream("/database/question-paper-data.sql");

                if (sampleDataStream == null) {
                    LOGGER.warning("Could not find question-paper-data.sql script in resources");
                } else {
                    // Read the SQL script
                    String sampleDataSql = new java.io.BufferedReader(new java.io.InputStreamReader(sampleDataStream))
                            .lines().collect(java.util.stream.Collectors.joining("\n"));

                    // Split the SQL script on semicolons
                    String[] sampleDataStatements = sampleDataSql.split(";");

                    // Execute each statement to insert the 8 question paper records
                    for (String statement : sampleDataStatements) {
                        if (!statement.trim().isEmpty()) {
                            try {
                                ps = conn.prepareStatement(statement);
                                ps.execute();
                                DatabaseConnection.closeResources(ps, null);
                                ps = null;
                            } catch (SQLException e) {
                                // Log errors but continue with remaining statements
                                LOGGER.log(Level.WARNING, "Error executing question paper data SQL statement: " + e.getMessage(), e);
                            }
                        }
                    }

                    LOGGER.info("Question paper data initialized successfully (8 question papers for 4 schools x 2 exam types)");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error initializing question paper data", e);
            }

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing question paper schema", e);
            return false;
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }
    }
}
