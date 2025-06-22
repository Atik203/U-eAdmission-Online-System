-- Update Question Papers Table to add result publication fields

# -- Use the database
# USE uiu_admission_db;
#
# -- Add is_result_published and pass_mark fields to question_papers table
# ALTER TABLE question_papers
# ADD COLUMN is_result_published BOOLEAN NOT NULL DEFAULT FALSE,
# ADD COLUMN pass_mark DECIMAL(5,2) DEFAULT 40.00;
#
# -- Update existing records to set default values
# UPDATE question_papers SET is_result_published = FALSE, pass_mark = 40.00;
#
# -- Create index for faster queries
# CREATE INDEX idx_question_papers_is_published ON question_papers(is_result_published);