package common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for TestRunner progress functionality.
 */
public class TestRunnerProgressTest {
    
    /**
     * Mock automaton for testing progress callbacks
     */
    static class MockAutomaton extends Automaton {
        public MockAutomaton() {
            super(MachineType.DFA);
        }
        
        @Override
        public ParseResult parse(String inputText) {
            return new ParseResult(true, new ArrayList<>(), this);
        }
        
        @Override
        public ExecutionResult execute(String inputText) {
            // Simulate some processing time
            try {
                Thread.sleep(50); // Small delay to see progress
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Simple logic: accept strings ending with 'a'
            boolean accept = inputText.endsWith("a");
            return new ExecutionResult(accept, new ArrayList<>(), "Mock execution");
        }
        
        @Override
        public List<ValidationMessage> validate() {
            return new ArrayList<>();
        }
        
        @Override
        public String toDotCode(String inputText) {
            return "";
        }
    }
    
    private File tempTestFile;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary test file with multiple test cases
        tempTestFile = File.createTempFile("progress_test", ".test");
        tempTestFile.deleteOnExit();
        
        try (FileWriter writer = new FileWriter(tempTestFile)) {
            writer.write("a,1\n");          // should accept
            writer.write("b,0\n");          // should reject
            writer.write("ab,0\n");         // should reject
            writer.write("ba,1\n");         // should accept
            writer.write("aaa,1\n");        // should accept
        }
    }
    
    @Test
    void testProgressCallbackIsInvoked() {
        MockAutomaton automaton = new MockAutomaton();
        
        // Track progress updates
        List<String> startedTests = new ArrayList<>();
        List<String> completedTests = new ArrayList<>();
        List<Boolean> testResults = new ArrayList<>();
        
        TestRunner.TestProgressCallback progressCallback = new TestRunner.TestProgressCallback() {
            @Override
            public void onTestStarted(int currentTest, int totalTests, String input) {
                startedTests.add(input);
                assertEquals(5, totalTests, "Total tests should be 5");
            }
            
            @Override
            public void onTestCompleted(int currentTest, int totalTests, String input, boolean passed) {
                completedTests.add(input);
                testResults.add(passed);
                assertEquals(5, totalTests, "Total tests should be 5");
            }
        };
        
        // Run tests with progress callback
        TestRunner.TestResult result = TestRunner.runTests(
            automaton, tempTestFile.getAbsolutePath(), progressCallback
        );
        
        // Verify results
        assertNotNull(result);
        assertEquals(5, result.getTotalTests());
        
        // Verify progress callbacks were called
        assertEquals(5, startedTests.size(), "All tests should have started");
        assertEquals(5, completedTests.size(), "All tests should have completed");
        assertEquals(5, testResults.size(), "All test results should be recorded");
        
        // Verify test inputs are correct
        assertEquals(Arrays.asList("a", "b", "ab", "ba", "aaa"), startedTests);
        assertEquals(Arrays.asList("a", "b", "ab", "ba", "aaa"), completedTests);
        
        // Verify test results match expected outcomes
        assertEquals(Arrays.asList(true, true, true, true, true), testResults); // all should pass based on our mock logic
    }
    
    @Test
    void testProgressDataClass() {
        // Test started progress
        TestRunner.TestProgress started = TestRunner.TestProgress.started(3, 10, "test");
        assertEquals(3, started.getCurrentTest());
        assertEquals(10, started.getTotalTests());
        assertEquals("test", started.getCurrentInput());
        assertFalse(started.isCompleted());
        assertFalse(started.isPassed());
        assertEquals(30, started.getProgressPercentage()); // 3/10 * 100 = 30
        
        // Test completed progress
        TestRunner.TestProgress completed = TestRunner.TestProgress.completed(5, 10, "test", true);
        assertEquals(5, completed.getCurrentTest());
        assertEquals(10, completed.getTotalTests());
        assertEquals("test", completed.getCurrentInput());
        assertTrue(completed.isCompleted());
        assertTrue(completed.isPassed());
        assertEquals(50, completed.getProgressPercentage()); // 5/10 * 100 = 50
    }
    
    @Test
    void testProgressCallbackWithTimeout() {
        MockAutomaton automaton = new MockAutomaton();
        
        List<String> startedTests = new ArrayList<>();
        TestRunner.TestProgressCallback progressCallback = new TestRunner.TestProgressCallback() {
            @Override
            public void onTestStarted(int currentTest, int totalTests, String input) {
                startedTests.add(input);
            }
            
            @Override
            public void onTestCompleted(int currentTest, int totalTests, String input, boolean passed) {
                // Record completed test
            }
        };
        
        // Run with very long timeout to ensure tests complete normally
        TestRunner.TestResult result = TestRunner.runTests(
            automaton, tempTestFile.getAbsolutePath(), 10000, progressCallback
        );
        
        // Verify tests ran normally
        assertNotNull(result);
        assertEquals(5, result.getTotalTests());
        assertEquals(0, result.getTimeoutCount());
        assertEquals(5, startedTests.size());
    }
}