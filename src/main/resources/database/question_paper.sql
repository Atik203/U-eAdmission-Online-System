-- New Question Paper Database Schema

-- Use the database
USE uiu_admission_db;

# -- Drop tables in reverse order of dependencies
# DROP TABLE IF EXISTS student_responses;
# DROP TABLE IF EXISTS exam_sessions;
# DROP TABLE IF EXISTS question_options;
# DROP TABLE IF EXISTS questions;
# DROP TABLE IF EXISTS question_papers;


-- Table for storing schools
CREATE TABLE IF NOT EXISTS schools (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table for storing exam types
CREATE TABLE IF NOT EXISTS exam_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_mock_exam BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    time_limit_minutes INT,
    total_marks INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table for storing subjects
CREATE TABLE IF NOT EXISTS subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table for storing the relationship between schools, exam types, and subjects
CREATE TABLE IF NOT EXISTS exam_subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    exam_type_id INT NOT NULL,
    subject_id INT NOT NULL,
    max_questions INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_type_id) REFERENCES exam_types(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY unique_exam_subject (school_id, exam_type_id, subject_id)
);

-- Table for storing question papers (collections of questions)
CREATE TABLE IF NOT EXISTS question_papers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    school_id INT NOT NULL,
    exam_type_id INT NOT NULL,
    total_questions INT NOT NULL,
    subjects TEXT NOT NULL,
    questions_per_subject TEXT NOT NULL,
    time_limit_minutes INT NOT NULL,
    total_marks INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_type_id) REFERENCES exam_types(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_school_exam_type (school_id, exam_type_id)
);

-- Table for storing individual questions
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_paper_id INT NOT NULL,
    subject_id INT NOT NULL,
    question_text TEXT NOT NULL,
    has_image BOOLEAN NOT NULL DEFAULT FALSE,
    image_path VARCHAR(255),
    has_latex BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_paper_id) REFERENCES question_papers(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- Table for storing options for multiple choice questions
CREATE TABLE IF NOT EXISTS question_options (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    option_order INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Table for storing student responses to questions
CREATE TABLE IF NOT EXISTS student_responses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option_id INT,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    response_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES question_options(id) ON DELETE SET NULL
);

-- Table for storing exam sessions
CREATE TABLE IF NOT EXISTS exam_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_paper_id INT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    score DECIMAL(5,2),
    max_score DECIMAL(5,2),
    status ENUM('in_progress', 'completed', 'abandoned') DEFAULT 'in_progress',
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_paper_id) REFERENCES question_papers(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_questions_paper_id ON questions(question_paper_id);
CREATE INDEX idx_questions_subject_id ON questions(subject_id);
CREATE INDEX idx_options_question_id ON question_options(question_id);
CREATE INDEX idx_responses_student_id ON student_responses(student_id);
CREATE INDEX idx_responses_question_id ON student_responses(question_id);
CREATE INDEX idx_exam_sessions_student_id ON exam_sessions(student_id);
CREATE INDEX idx_exam_sessions_paper_id ON exam_sessions(question_paper_id);
CREATE INDEX idx_question_papers_school_id ON question_papers(school_id);
CREATE INDEX idx_question_papers_exam_type_id ON question_papers(exam_type_id);
CREATE INDEX idx_exam_subjects_school_id ON exam_subjects(school_id);
CREATE INDEX idx_exam_subjects_exam_type_id ON exam_subjects(exam_type_id);
CREATE INDEX idx_exam_subjects_subject_id ON exam_subjects(subject_id);

-- Insert default data for schools
INSERT IGNORE INTO schools (name, description) VALUES
('School of Engineering & Technology', 'School of Engineering & Technology at UIU'),
('School of Business & Economics', 'School of Business & Economics at UIU'),
('School of Humanities & Social Sciences', 'School of Humanities & Social Sciences at UIU'),
('School of Life Sciences', 'School of Life Sciences at UIU');

-- Insert default data for exam types
INSERT IGNORE INTO exam_types (name, is_mock_exam, time_limit_minutes, total_marks) VALUES
('Mock Exam', TRUE, 75, 75),
('Actual Exam', FALSE, 120, 100);

-- Insert default data for subjects
INSERT IGNORE INTO subjects (name) VALUES
('English'),
('General Mathematics'),
('Higher Math & Physics'),
('Business & Economics'),
('Current Affairs'),
('Higher English & Logical Reasoning'),
('Biology & Chemistry');

-- Insert default data for exam_subjects (relationship between schools, exam types, and subjects)
-- School of Engineering & Technology - Mock Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Engineering & Technology'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Engineering & Technology'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Engineering & Technology'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'Higher Math & Physics'),
    30;

-- School of Engineering & Technology - Actual Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Engineering & Technology'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Engineering & Technology'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Engineering & Technology'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'Higher Math & Physics'),
    30;

-- School of Business & Economics - Mock Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Business & Economics'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Business & Economics'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Business & Economics'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'Business & Economics'),
    30;

-- School of Business & Economics - Actual Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Business & Economics'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Business & Economics'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Business & Economics'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'Business & Economics'),
    30;

-- School of Humanities & Social Sciences - Mock Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'Current Affairs'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'Higher English & Logical Reasoning'),
    15;

-- School of Humanities & Social Sciences - Actual Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'Current Affairs'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Humanities & Social Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'Higher English & Logical Reasoning'),
    15;

-- School of Life Sciences - Mock Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Life Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Life Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Life Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Mock Exam'),
    (SELECT id FROM subjects WHERE name = 'Biology & Chemistry'),
    30;

-- School of Life Sciences - Actual Exam
INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Life Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'English'),
    30;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Life Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'General Mathematics'),
    15;

INSERT IGNORE INTO exam_subjects (school_id, exam_type_id, subject_id, max_questions)
SELECT 
    (SELECT id FROM schools WHERE name = 'School of Life Sciences'),
    (SELECT id FROM exam_types WHERE name = 'Actual Exam'),
    (SELECT id FROM subjects WHERE name = 'Biology & Chemistry'),
    30;
