-- Use the database
USE uiu_admission_db;

-- Sample data for question_papers table
-- This script inserts question papers for all schools and both exam types
-- If a record already exists (based on school_id and exam_type_id), it will update the existing record
-- This is achieved using the ON DUPLICATE KEY UPDATE clause, which relies on the unique_school_exam_type constraint
-- The constraint ensures that there can only be one question paper for each combination of school and exam type
-- This prevents duplicate entries if the script is run multiple times

-- Insert question papers for School of Engineering & Technology
INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Engineering Mock Exam', 'Mock exam for School of Engineering & Technology', 
       s.id, et.id, 75, 'English, General Mathematics, Higher Math & Physics', '30, 15, 30', 75, 75, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Engineering & Technology' AND et.is_mock_exam = TRUE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Engineering Actual Exam', 'Actual exam for School of Engineering & Technology', 
       s.id, et.id, 100, 'English, General Mathematics, Higher Math & Physics', '40, 20, 40', 120, 100, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Engineering & Technology' AND et.is_mock_exam = FALSE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

-- Insert question papers for School of Business & Economics
INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Business Mock Exam', 'Mock exam for School of Business & Economics', 
       s.id, et.id, 75, 'English, General Mathematics, Business & Economics', '30, 15, 30', 75, 75, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Business & Economics' AND et.is_mock_exam = TRUE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Business Actual Exam', 'Actual exam for School of Business & Economics', 
       s.id, et.id, 100, 'English, General Mathematics, Business & Economics', '40, 20, 40', 120, 100, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Business & Economics' AND et.is_mock_exam = FALSE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

-- Insert question papers for School of Humanities & Social Sciences
INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Humanities Mock Exam', 'Mock exam for School of Humanities & Social Sciences', 
       s.id, et.id, 75, 'English, General Mathematics, Current Affairs, Higher English & Logical Reasoning', '30, 15, 15, 15', 75, 75, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Humanities & Social Sciences' AND et.is_mock_exam = TRUE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Humanities Actual Exam', 'Actual exam for School of Humanities & Social Sciences', 
       s.id, et.id, 100, 'English, General Mathematics, Current Affairs, Higher English & Logical Reasoning', '40, 20, 20, 20', 120, 100, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Humanities & Social Sciences' AND et.is_mock_exam = FALSE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

-- Insert question papers for School of Life Sciences
INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Life Sciences Mock Exam', 'Mock exam for School of Life Sciences', 
       s.id, et.id, 75, 'English, General Mathematics, Biology & Chemistry', '30, 15, 30', 75, 75, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Life Sciences' AND et.is_mock_exam = TRUE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO question_papers (title, description, school_id, exam_type_id, total_questions, subjects, questions_per_subject, time_limit_minutes, total_marks, created_by)
SELECT 'Life Sciences Actual Exam', 'Actual exam for School of Life Sciences', 
       s.id, et.id, 100, 'English, General Mathematics, Biology & Chemistry', '40, 20, 40', 120, 100, 1
FROM schools s, exam_types et
WHERE s.name = 'School of Life Sciences' AND et.is_mock_exam = FALSE
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    description = VALUES(description),
    total_questions = VALUES(total_questions),
    subjects = VALUES(subjects),
    questions_per_subject = VALUES(questions_per_subject),
    time_limit_minutes = VALUES(time_limit_minutes),
    total_marks = VALUES(total_marks),
    updated_at = CURRENT_TIMESTAMP;
