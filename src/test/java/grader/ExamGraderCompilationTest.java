package grader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the grading system properly handles compilation errors
 * and assigns 0 points when a DFA is incomplete or has errors.
 */
@DisplayName("ExamGrader Compilation Validation Tests")
public class ExamGraderCompilationTest {

    @TempDir
    Path tempDir;

    /**
     * Test that a DFA with missing transitions gets 0 points
     */
    @Test
    @DisplayName("DFA with missing transitions should get 0 points")
    public void testIncompleteDFA() throws IOException {
        // Create a student folder
        Path studentFolder = tempDir.resolve("student1");
        Files.createDirectories(studentFolder);

        // Create an incomplete DFA file (missing transitions)
        String incompleteDFA = "Start: q0\n" +
                              "Finals: q1\n" +
                              "Alphabet: a b\n" +
                              "States: q0 q1\n" +
                              "\n" +
                              "Transitions:\n" +
                              "q0 -> q1 (a)\n";
        
        Path dfaFile = studentFolder.resolve("Q1a.dfa");
        Files.write(dfaFile, incompleteDFA.getBytes());

        // Create test cases folder with a simple test file
        Path testCasesFolder = tempDir.resolve("test_cases");
        Files.createDirectories(testCasesFolder);
        
        String testCases = "a,1\n" +
                          "b,0\n" +
                          "ab,0\n";
        Path testFile = testCasesFolder.resolve("Q1a.test");
        Files.write(testFile, testCases.getBytes());

        // Grade the question
        ExamGrader.GradingResult result = ExamGrader.gradeQuestion(
            studentFolder.toString(),
            "Q1a",
            testCasesFolder.toString()
        );

        // Verify that the student gets 0 points due to compilation error
        assertNotNull(result, "Result should not be null");
        assertFalse(result.success, "Result should not be successful");
        assertNotNull(result.errorMessage, "Error message should be present");
        assertTrue(result.errorMessage.contains("Compilation Error") || 
                   result.errorMessage.contains("Missing transition"),
                   "Error message should mention compilation error or missing transition");
        assertEquals(0.0, result.score, "Score should be 0.0 for incomplete DFA");
        assertEquals(10, result.maxPoints, "Max points should be set");
    }

    /**
     * Test that a complete and correct DFA gets proper points
     */
    @Test
    @DisplayName("Complete and correct DFA should get proper points")
    public void testCompleteDFA() throws IOException {
        // Create a student folder
        Path studentFolder = tempDir.resolve("student2");
        Files.createDirectories(studentFolder);

        // Create a complete DFA file (accepts strings ending with 'a')
        String completeDFA = "Start: q0\n" +
                            "Finals: q1\n" +
                            "Alphabet: a b\n" +
                            "States: q0 q1\n" +
                            "\n" +
                            "Transitions:\n" +
                            "q0 -> q1 (a)\n" +
                            "q0 -> q0 (b)\n" +
                            "q1 -> q1 (a)\n" +
                            "q1 -> q0 (b)\n";
        
        Path dfaFile = studentFolder.resolve("Q1a.dfa");
        Files.write(dfaFile, completeDFA.getBytes());

        // Create test cases folder with test file
        Path testCasesFolder = tempDir.resolve("test_cases2");
        Files.createDirectories(testCasesFolder);
        
        String testCases = "a,1\n" +
                          "b,0\n" +
                          "aa,1\n" +
                          "ab,0\n" +
                          "ba,1\n" +
                          "bb,0\n";
        Path testFile = testCasesFolder.resolve("Q1a.test");
        Files.write(testFile, testCases.getBytes());

        // Grade the question
        ExamGrader.GradingResult result = ExamGrader.gradeQuestion(
            studentFolder.toString(),
            "Q1a",
            testCasesFolder.toString()
        );

        // Verify that the student gets proper points
        assertNotNull(result, "Result should not be null");
        assertTrue(result.success, "Result should be successful");
        assertNull(result.errorMessage, "Error message should be null for valid DFA");
        assertTrue(result.score > 0, "Score should be greater than 0 for complete DFA");
        assertEquals(6, result.totalTests, "Should have 6 total tests");
        assertEquals(6, result.passedTests, "Should pass all 6 tests");
    }

    /**
     * Test that a DFA with parse errors gets 0 points
     */
    @Test
    @DisplayName("DFA with parse errors should get 0 points")
    public void testDFAWithParseErrors() throws IOException {
        // Create a student folder
        Path studentFolder = tempDir.resolve("student3");
        Files.createDirectories(studentFolder);

        // Create a DFA file with syntax errors
        String invalidDFA = "Start: q0\n" +
                           "Finals: q1\n" +
                           "Alphabet: a b\n" +
                           // Missing States line
                           "\n" +
                           "Transitions:\n" +
                           "q0 -> q1 (a)\n" +
                           "q0 -> q0 (b)\n";
        
        Path dfaFile = studentFolder.resolve("Q1a.dfa");
        Files.write(dfaFile, invalidDFA.getBytes());

        // Create test cases folder
        Path testCasesFolder = tempDir.resolve("test_cases3");
        Files.createDirectories(testCasesFolder);
        
        String testCases = "a,1\n";
        Path testFile = testCasesFolder.resolve("Q1a.test");
        Files.write(testFile, testCases.getBytes());

        // Grade the question
        ExamGrader.GradingResult result = ExamGrader.gradeQuestion(
            studentFolder.toString(),
            "Q1a",
            testCasesFolder.toString()
        );

        // Verify that the student gets 0 points due to compilation error
        assertNotNull(result, "Result should not be null");
        assertFalse(result.success, "Result should not be successful");
        assertNotNull(result.errorMessage, "Error message should be present");
        assertEquals(0.0, result.score, "Score should be 0.0 for DFA with parse errors");
    }

    /**
     * Test that an empty DFA file gets 0 points
     */
    @Test
    @DisplayName("Empty DFA file should get 0 points")
    public void testEmptyDFAFile() throws IOException {
        // Create a student folder
        Path studentFolder = tempDir.resolve("student4");
        Files.createDirectories(studentFolder);

        // Create an empty DFA file
        Path dfaFile = studentFolder.resolve("Q1a.dfa");
        Files.write(dfaFile, "".getBytes());

        // Create test cases folder
        Path testCasesFolder = tempDir.resolve("test_cases4");
        Files.createDirectories(testCasesFolder);
        
        String testCases = "a,1\n";
        Path testFile = testCasesFolder.resolve("Q1a.test");
        Files.write(testFile, testCases.getBytes());

        // Grade the question
        ExamGrader.GradingResult result = ExamGrader.gradeQuestion(
            studentFolder.toString(),
            "Q1a",
            testCasesFolder.toString()
        );

        // Verify that the student gets 0 points
        assertNotNull(result, "Result should not be null");
        assertFalse(result.success, "Result should not be successful");
        assertNotNull(result.errorMessage, "Error message should be present");
        assertTrue(result.errorMessage.contains("Empty file"), 
                   "Error message should mention empty file");
        assertEquals(0.0, result.score, "Score should be 0.0 for empty file");
    }

    /**
     * Test that a DFA with partially missing transitions gets 0 points
     */
    @Test
    @DisplayName("DFA with some missing transitions should get 0 points")
    public void testPartiallyIncompleteDFA() throws IOException {
        // Create a student folder
        Path studentFolder = tempDir.resolve("student5");
        Files.createDirectories(studentFolder);

        // Create a DFA with some but not all transitions
        String partialDFA = "Start: q0\n" +
                           "Finals: q2\n" +
                           "Alphabet: a b c\n" +
                           "States: q0 q1 q2\n" +
                           "\n" +
                           "Transitions:\n" +
                           "q0 -> q1 (a)\n" +
                           "q0 -> q0 (b)\n" +
                           "q0 -> q0 (c)\n" +
                           "q1 -> q2 (a)\n" +
                           "q1 -> q1 (b)\n" +
                           // Missing: q1 -> ... (c)
                           "q2 -> q2 (a b)\n";
                           // Missing: q2 -> ... (c)
        
        Path dfaFile = studentFolder.resolve("Q1a.dfa");
        Files.write(dfaFile, partialDFA.getBytes());

        // Create test cases folder
        Path testCasesFolder = tempDir.resolve("test_cases5");
        Files.createDirectories(testCasesFolder);
        
        String testCases = "aa,1\n" +
                          "a,0\n";
        Path testFile = testCasesFolder.resolve("Q1a.test");
        Files.write(testFile, testCases.getBytes());

        // Grade the question
        ExamGrader.GradingResult result = ExamGrader.gradeQuestion(
            studentFolder.toString(),
            "Q1a",
            testCasesFolder.toString()
        );

        // Verify that the student gets 0 points
        assertNotNull(result, "Result should not be null");
        assertFalse(result.success, "Result should not be successful");
        assertNotNull(result.errorMessage, "Error message should be present");
        assertTrue(result.errorMessage.contains("Missing transition"), 
                   "Error message should mention missing transition");
        assertEquals(0.0, result.score, "Score should be 0.0 for partially incomplete DFA");
        assertTrue(result.errorMessage.contains("q1") || result.errorMessage.contains("q2"),
                   "Error message should mention the state with missing transition");
    }
}
