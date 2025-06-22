-- Update exam_sessions table to add warning field for anti-cheating feature

-- Use the database
USE uiu_admission_db;

-- Add warning field to exam_sessions table
ALTER TABLE exam_sessions
ADD COLUMN warning_count INT NOT NULL DEFAULT 0;

-- Create index for faster queries on warning_count
CREATE INDEX idx_exam_sessions_warning_count ON exam_sessions(warning_count);