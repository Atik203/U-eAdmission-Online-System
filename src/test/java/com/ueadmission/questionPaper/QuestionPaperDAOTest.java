package com.ueadmission.questionPaper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for QuestionPaperDAO
 */
public class QuestionPaperDAOTest {

    /**
     * Test that the initializeQuestionPaperSchema method works without errors
     */
    @Test
    public void testInitializeQuestionPaperSchema() {
        System.out.println("[DEBUG_LOG] Testing initializeQuestionPaperSchema");
        boolean result = QuestionPaperDAO.initializeQuestionPaperSchema();
        System.out.println("[DEBUG_LOG] Result: " + result);
        assertTrue(result, "Schema initialization should succeed");
    }

    /**
     * Test that the getMostRecentQuestionPaper method works without errors
     */
    @Test
    public void testGetMostRecentQuestionPaper() {
        System.out.println("[DEBUG_LOG] Testing getMostRecentQuestionPaper");

        // Test with mock exam = true
        QuestionPaper mockPaper = QuestionPaperDAO.getMostRecentQuestionPaper(true, "School of Engineering & Technology");
        System.out.println("[DEBUG_LOG] Mock paper: " + (mockPaper != null ? "Found" : "Not found"));

        // Test with mock exam = false
        QuestionPaper actualPaper = QuestionPaperDAO.getMostRecentQuestionPaper(false, "School of Engineering & Technology");
        System.out.println("[DEBUG_LOG] Actual paper: " + (actualPaper != null ? "Found" : "Not found"));

        // Test with no filters
        QuestionPaper anyPaper = QuestionPaperDAO.getMostRecentQuestionPaper(null, null);
        System.out.println("[DEBUG_LOG] Any paper: " + (anyPaper != null ? "Found" : "Not found"));
    }

    /**
     * Test that the getQuestionsForPaper method works without errors
     */
    @Test
    public void testGetQuestionsForPaper() {
        System.out.println("[DEBUG_LOG] Testing getQuestionsForPaper");

        // Get the most recent paper
        QuestionPaper paper = QuestionPaperDAO.getMostRecentQuestionPaper(null, null);
        if (paper != null) {
            System.out.println("[DEBUG_LOG] Found paper with ID: " + paper.getId());

            // Get questions for the paper
            java.util.List<Question> questions = QuestionPaperDAO.getQuestionsForPaper(paper.getId());
            System.out.println("[DEBUG_LOG] Found " + questions.size() + " questions");

            // Check if questions have subjects
            for (Question question : questions) {
                System.out.println("[DEBUG_LOG] Question ID: " + question.getId() + ", Subject: " + question.getSubject());
            }
        } else {
            System.out.println("[DEBUG_LOG] No papers found to test with");
        }
    }
}
