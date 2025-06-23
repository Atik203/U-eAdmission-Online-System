-- Update exam_sessions table to add warning field for anti-cheating feature

-- Use the database
USE uiu_admission_db;

-- Add warning field to exam_sessions table only if it doesn't exist
SET @columnExists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'uiu_admission_db'
    AND table_name = 'exam_sessions'
    AND column_name = 'warning_count'
);

SET @sqlStatement = IF(
    @columnExists = 0,
    'ALTER TABLE exam_sessions ADD COLUMN warning_count INT NOT NULL DEFAULT 0',
    'SELECT "Column warning_count already exists in exam_sessions table"'
);

PREPARE stmt FROM @sqlStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create index for faster queries on warning_count if it doesn't exist
SET @indexExists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = 'uiu_admission_db'
    AND table_name = 'exam_sessions'
    AND index_name = 'idx_exam_sessions_warning_count'
);

SET @sqlStatement = IF(
    @indexExists = 0,
    'CREATE INDEX idx_exam_sessions_warning_count ON exam_sessions(warning_count)',
    'SELECT "Index idx_exam_sessions_warning_count already exists on exam_sessions table"'
);

PREPARE stmt FROM @sqlStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
