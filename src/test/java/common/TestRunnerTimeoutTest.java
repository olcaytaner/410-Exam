package common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test class for TestRunner timeout functionality.
 */
public class TestRunnerTimeoutTest {
    
    /**
     * Mock automaton that simulates infinite loop for testing timeout
     */
    static class InfiniteLoopAutomaton extends Automaton {
        public InfiniteLoopAutomaton() {
            super(MachineType.DFA);
        }
        
        @Override
        public ParseResult parse(String inputText) {
            return new ParseResult(true, new ArrayList<>(), this);
        }
        
        @Override
        public ExecutionResult execute(String inputText) {
            // Simulate an infinite loop
            while (true) {
                try {
                    Thread.sleep(100); // Sleep to avoid consuming 100% CPU
                } catch (InterruptedException e) {
                    // Thread was interrupted due to timeout
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return new ExecutionResult(false, new ArrayList<>(), "Should not reach here");
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
    
    /**
     * Mock automaton that executes quickly for testing normal behavior
     */
    static class FastAutomaton extends Automaton {
        public FastAutomaton() {
            super(MachineType.DFA);
        }
        
        @Override
        public ParseResult parse(String inputText) {
            return new ParseResult(true, new ArrayList<>(), this);
        }
        
        @Override
        public ExecutionResult execute(String inputText) {
            // Quick execution
            boolean accept = inputText.equals("accept");
            return new ExecutionResult(accept, new ArrayList<>(), "Fast execution");
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
        // Create a temporary test file
        tempTestFile = File.createTempFile("timeout_test", ".test");
        tempTestFile.deleteOnExit();
        
        // Write sample test cases
        try (FileWriter writer = new FileWriter(tempTestFile)) {
            writer.write("test,0\n");        // "test" should reject
            writer.write("accept,1\n");      // "accept" should accept  
            writer.write("reject,0\n");      // "reject" should reject
        }
    }
    
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSingleTestWithTimeout() {
        InfiniteLoopAutomaton automaton = new InfiniteLoopAutomaton();
        
        // Run with a short timeout (500ms) 
        TestRunner.TestCaseResult result = TestRunner.runSingleTest(
            automaton, "test", true, 500
        );
        
        assertNotNull(result);
        assertTrue(result.isTimedOut(), "Test should have timed out");
        assertFalse(result.isPassed(), "Timed out test should not pass");
        assertTrue(result.getTrace().startsWith("TIMEOUT:"), 
            "Trace should indicate timeout");
    }
    
    @Test
    void testSingleTestWithoutTimeout() {
        FastAutomaton automaton = new FastAutomaton();
        
        // Run with a reasonable timeout
        TestRunner.TestCaseResult result = TestRunner.runSingleTest(
            automaton, "accept", true, 1000
        );
        
        assertNotNull(result);
        assertFalse(result.isTimedOut(), "Fast test should not timeout");
        assertTrue(result.isPassed(), "Fast test should pass");
        assertEquals("Fast execution", result.getTrace());
    }
    
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunTestsWithTimeout() {
        InfiniteLoopAutomaton automaton = new InfiniteLoopAutomaton();
        
        // Run tests with a short timeout for entire suite (500ms)
        TestRunner.TestResult result = TestRunner.runTests(
            automaton, tempTestFile.getAbsolutePath(), 500
        );
        
        assertNotNull(result);
        assertEquals(3, result.getTotalTests());
        assertEquals(0, result.getPassedTests());
        assertEquals(1, result.getTimeoutCount(), 
            "Test suite should timeout as a whole with infinite loop automaton");
    }
    
    @Test
    void testRunTestsWithMixedResults() {
        FastAutomaton automaton = new FastAutomaton();
        
        // Run tests with normal timeout
        TestRunner.TestResult result = TestRunner.runTests(
            automaton, tempTestFile.getAbsolutePath(), 1000
        );
        
        assertNotNull(result);
        assertEquals(3, result.getTotalTests());
        assertEquals(0, result.getTimeoutCount(), 
            "No tests should timeout with fast automaton");
        // Check that normal classification still works
        assertEquals(2, result.getTrueNegatives()); // "test" and "reject" correctly rejected
        assertEquals(1, result.getTruePositives());  // "accept" correctly accepted
    }
    
    @Test
    void testTimeoutReporting() {
        InfiniteLoopAutomaton automaton = new InfiniteLoopAutomaton();
        
        TestRunner.TestCaseResult result = TestRunner.runSingleTest(
            automaton, "test", false, 500
        );
        
        String resultString = result.toString();
        assertTrue(resultString.contains("TIMEOUT"), 
            "Timeout should be shown in toString()");
        assertTrue(resultString.contains("Expected: REJECT"),
            "Expected result should be shown");
    }
    
    @Test
    void testDefaultTimeout() {
        // Verify default timeout constant is defined
        assertEquals(5000, TestRunner.DEFAULT_TIMEOUT_MS,
            "Default timeout should be 5 seconds for entire test suite");
        
        // Test that default timeout is used when not specified
        FastAutomaton automaton = new FastAutomaton();
        TestRunner.TestCaseResult result = TestRunner.runSingleTest(
            automaton, "test", false
        );
        
        assertNotNull(result);
        assertFalse(result.isTimedOut());
    }
}