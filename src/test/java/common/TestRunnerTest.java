package common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import NondeterministicFiniteAutomaton.NFA;

/**
 * Test class for TestRunner functionality.
 */
public class TestRunnerTest {
    
    private File tempTestFile;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary test file
        tempTestFile = File.createTempFile("test", ".test");
        tempTestFile.deleteOnExit();
        
        // Write sample test cases
        try (FileWriter writer = new FileWriter(tempTestFile)) {
            writer.write(",0\n");         // empty string should reject
            writer.write("0,1\n");        // "0" should accept  
            writer.write("1,0\n");        // "1" should reject
            writer.write("00,1\n");       // "00" should accept
        }
    }
    
    @Test
    void testParseTestFile() throws IOException {
        List<TestCase> testCases = TestFileParser.parseTestFile(tempTestFile.getAbsolutePath());
        
        assertEquals(4, testCases.size());
        
        assertEquals("", testCases.get(0).getInput());
        assertFalse(testCases.get(0).shouldAccept());
        
        assertEquals("0", testCases.get(1).getInput());
        assertTrue(testCases.get(1).shouldAccept());
        
        assertEquals("1", testCases.get(2).getInput());
        assertFalse(testCases.get(2).shouldAccept());
        
        assertEquals("00", testCases.get(3).getInput());
        assertTrue(testCases.get(3).shouldAccept());
    }
    
    @Test
    void testSingleTestCase() {
        // Create a simple test case
        TestCase testCase = new TestCase("test", true);
        
        assertEquals("test", testCase.getInput());
        assertTrue(testCase.shouldAccept());
        
        String expected = "TestCase{input='test', shouldAccept=true}";
        assertEquals(expected, testCase.toString());
    }
    
    @Test 
    void testRunSingleTest() {
        // Create a simple NFA for testing
        NFA nfa = new NFA();
        
        // Test with a simple input - just verify the method doesn't crash
        TestRunner.TestCaseResult result = TestRunner.runSingleTest(nfa, "test", true);
        
        assertNotNull(result);
        assertEquals("test", result.getInput());
        assertTrue(result.getExpectedAccept());
        
        // The actual acceptance will depend on the NFA implementation
        // but the method should not crash
        assertNotNull(result.toString());
    }
}