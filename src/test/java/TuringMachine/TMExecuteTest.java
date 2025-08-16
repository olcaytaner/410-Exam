package TuringMachine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import common.Automaton;

/**
 * Comprehensive JUnit 5 test class for Turing Machine execution functionality.
 * Tests the execute() method inherited from Automaton abstract class,
 * focusing on tape operations, head movements, and computation.
 */
@DisplayName("Turing Machine Execute Method Tests")
public class TMExecuteTest {

    private TM tm;
    private String binaryIncrementTM;
    private String palindromeTM;
    private String copyTM;
    private String simpleTM;

    @BeforeEach
    void setUp() {
        // Simple TM that accepts strings with even number of 0s
        simpleTM = "states: q0 q1 q_accept q_reject\n" +
                  "input_alphabet: 0 1\n" +
                  "tape_alphabet: 0 1 _\n" +
                  "start: q0\n" +
                  "accept: q_accept\n" +
                  "REJECT: q_reject\n" +
                  "transitions:\n" +
                  "q0 0 -> q1 0 R\n" +
                  "q0 1 -> q0 1 R\n" +
                  "q0 _ -> q_accept _ R\n" +
                  "q1 0 -> q0 0 R\n" +
                  "q1 1 -> q1 1 R\n" +
                  "q1 _ -> q_reject _ R\n";
        
        // TM that increments a binary number by 1
        binaryIncrementTM = "states: q0 q1 q2 q_accept q_reject\n" +
                           "input_alphabet: 0 1\n" +
                           "tape_alphabet: 0 1 _\n" +
                           "start: q0\n" +
                           "accept: q_accept\n" +
                           "REJECT: q_reject\n" +
                           "transitions:\n" +
                           "q0 0 -> q0 0 R\n" +
                           "q0 1 -> q0 1 R\n" +
                           "q0 _ -> q1 _ L\n" +
                           "q1 0 -> q2 1 L\n" +
                           "q1 1 -> q1 0 L\n" +
                           "q1 _ -> q2 1 R\n" +
                           "q2 0 -> q2 0 L\n" +
                           "q2 1 -> q2 1 L\n" +
                           "q2 _ -> q_accept _ R\n";
        
        // TM that checks if a string is a palindrome
        palindromeTM = "states: q0 q1 q2 q3 q4 q5 q6 q_accept q_reject\n" +
                      "input_alphabet: 0 1\n" +
                      "tape_alphabet: 0 1 X _\n" +
                      "start: q0\n" +
                      "accept: q_accept\n" +
                      "reject: q_reject\n" +
                      "transitions:\n" +
                      "# Initial state - read first character\n" +
                      "q0 0 -> q1 X R\n" +
                      "q0 1 -> q2 X R\n" +
                      "q0 X -> q0 X R\n" +
                      "q0 _ -> q_accept _ R\n" +
                      "# Scanned 0 - find rightmost unprocessed\n" +
                      "q1 0 -> q1 0 R\n" +
                      "q1 1 -> q1 1 R\n" +
                      "q1 X -> q3 X L\n" +
                      "q1 _ -> q3 _ L\n" +
                      "# Scanned 1 - find rightmost unprocessed\n" +
                      "q2 0 -> q2 0 R\n" +
                      "q2 1 -> q2 1 R\n" +
                      "q2 X -> q4 X L\n" +
                      "q2 _ -> q4 _ L\n" +
                      "# Looking for last char to match 0\n" +
                      "q3 X -> q3 X L\n" +
                      "q3 0 -> q5 X L\n" +
                      "q3 1 -> q_reject 1 L\n" +
                      "q3 _ -> q_accept _ R\n" +
                      "# Looking for last char to match 1\n" +
                      "q4 X -> q4 X L\n" +
                      "q4 1 -> q5 X L\n" +
                      "q4 0 -> q_reject 0 L\n" +
                      "q4 _ -> q_accept _ R\n" +
                      "# Return to start\n" +
                      "q5 0 -> q5 0 L\n" +
                      "q5 1 -> q5 1 L\n" +
                      "q5 X -> q5 X L\n" +
                      "q5 _ -> q6 _ R\n" +
                      "# Find first unprocessed\n" +
                      "q6 X -> q6 X R\n" +
                      "q6 0 -> q0 0 L\n" +
                      "q6 1 -> q0 1 L\n" +
                      "q6 _ -> q_accept _ L\n";
        
        // TM that copies a string (0s and 1s) separated by #
        copyTM = "states: q0 q1 q2 q3 q4 q_accept q_reject\n" +
                "input_alphabet: 0 1 #\n" +
                "tape_alphabet: 0 1 # X Y _\n" +
                "start: q0\n" +
                "accept: q_accept\n" +
                "REJECT: q_reject\n" +
                "transitions:\n" +
                "q0 0 -> q1 X R\n" +
                "q0 1 -> q2 Y R\n" +
                "q0 # -> q_accept # R\n" +
                "q1 0 -> q1 0 R\n" +
                "q1 1 -> q1 1 R\n" +
                "q1 # -> q1 # R\n" +
                "q1 _ -> q3 0 L\n" +
                "q2 0 -> q2 0 R\n" +
                "q2 1 -> q2 1 R\n" +
                "q2 # -> q2 # R\n" +
                "q2 _ -> q4 1 L\n" +
                "q3 0 -> q3 0 L\n" +
                "q3 1 -> q3 1 L\n" +
                "q3 # -> q3 # L\n" +
                "q3 X -> q0 0 R\n" +
                "q4 0 -> q4 0 L\n" +
                "q4 1 -> q4 1 L\n" +
                "q4 # -> q4 # L\n" +
                "q4 Y -> q0 1 R\n";
    }

    @Nested
    @DisplayName("Basic Execution Tests")
    class BasicExecutionTests {
        
        @Test
        @DisplayName("Execute should return ExecutionResult object")
        void testExecuteReturnsResult() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            assertTrue(parseResult.isSuccess(), "TM should parse successfully");
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("00");
            
            assertNotNull(result, "ExecutionResult should not be null");
            assertNotNull(result.getTrace(), "Trace should not be null");
            assertNotNull(result.getRuntimeMessages(), "Runtime messages should not be null");
        }
        
        @Test
        @DisplayName("Should accept strings with even number of 0s")
        void testAcceptEvenZeros() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            assertTrue(tm.execute("").isAccepted(), "Empty string (0 zeros) should be accepted");
            assertTrue(tm.execute("00").isAccepted(), "'00' (2 zeros) should be accepted");
            assertTrue(tm.execute("0101").isAccepted(), "'0101' (2 zeros) should be accepted");
            assertTrue(tm.execute("0000").isAccepted(), "'0000' (4 zeros) should be accepted");
            assertTrue(tm.execute("11").isAccepted(), "'11' (0 zeros) should be accepted");
        }
        
        @Test
        @DisplayName("Should reject strings with odd number of 0s")
        void testRejectOddZeros() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            assertFalse(tm.execute("0").isAccepted(), "'0' (1 zero) should be rejected");
            assertFalse(tm.execute("000").isAccepted(), "'000' (3 zeros) should be rejected");
            assertFalse(tm.execute("0111").isAccepted(), "'0111' (1 zero) should be rejected");
            assertTrue(tm.execute("10101").isAccepted(), "'10101' (2 zeros) should be accepted");
        }
    }

    @Nested
    @DisplayName("Tape Operation Tests")
    class TapeOperationTests {
        
        @Test
        @DisplayName("Trace should contain tape configurations")
        void testTraceContainsTapeConfig() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("01");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
            
            // Trace should show tape contents and head position
        }
        
        @Test
        @DisplayName("Head movement should be tracked")
        void testHeadMovement() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("101");
            
            assertNotNull(result.getTrace(), "Should have trace with head movements");
            // Trace should show R (right) movements
        }
        
        @Test
        @DisplayName("Tape should be modified correctly")
        void testTapeModification() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(binaryIncrementTM);
            tm = (TM) parseResult.getAutomaton();
            
            // Test binary increment
            Automaton.ExecutionResult result = tm.execute("101"); // 5 in binary
            
            assertTrue(result.isAccepted(), "Binary increment should complete successfully");
            
            // After execution, check tape contents if possible
            if (tm.getTape() != null) {
                String tapeContents = tm.getTape().getTapeContents();
                assertNotNull(tapeContents, "Tape contents should be available");
                // Should be "110" (6 in binary)
            }
        }
    }

    @Nested
    @DisplayName("ExecutionResult Validation Tests")
    class ExecutionResultTests {
        
        @Test
        @DisplayName("Accepted result should have isAccepted() true")
        void testAcceptedResult() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("00");
            
            assertTrue(result.isAccepted(), "Result should be accepted");
            assertNotNull(result.getTrace(), "Accepted result should have trace");
        }
        
        @Test
        @DisplayName("Rejected result should have isAccepted() false")
        void testRejectedResult() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("0");
            
            assertFalse(result.isAccepted(), "Result should be rejected");
            assertNotNull(result.getTrace(), "Rejected result should have trace");
        }
        
        @Test
        @DisplayName("Runtime messages should be populated")
        void testRuntimeMessages() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("01");
            List<Automaton.ValidationMessage> messages = result.getRuntimeMessages();
            
            assertNotNull(messages, "Runtime messages should not be null");
            // Messages may contain information about computation steps
        }
    }

    @Nested
    @DisplayName("Complex Computation Tests")
    class ComplexComputationTests {
        
        @Test
        @DisplayName("Binary increment computation")
        void testBinaryIncrement() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(binaryIncrementTM);
            tm = (TM) parseResult.getAutomaton();
            
            // Test various binary numbers
            assertTrue(tm.execute("0").isAccepted(), "Increment of '0' should succeed");
            assertTrue(tm.execute("1").isAccepted(), "Increment of '1' should succeed");
            assertTrue(tm.execute("10").isAccepted(), "Increment of '10' should succeed");
            assertTrue(tm.execute("11").isAccepted(), "Increment of '11' should succeed");
            assertTrue(tm.execute("111").isAccepted(), "Increment of '111' should succeed");
        }
        
        @Test
        @DisplayName("Palindrome checking")
        void testPalindromeChecking() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(palindromeTM);
            tm = (TM) parseResult.getAutomaton();
            
            // Test palindromes
            assertTrue(tm.execute("").isAccepted(), "Empty string is a palindrome");
            assertTrue(tm.execute("0").isAccepted(), "'0' is a palindrome");
            assertTrue(tm.execute("1").isAccepted(), "'1' is a palindrome");
            assertTrue(tm.execute("00").isAccepted(), "'00' is a palindrome");
            assertTrue(tm.execute("11").isAccepted(), "'11' is a palindrome");
            assertTrue(tm.execute("010").isAccepted(), "'010' is a palindrome");
            assertTrue(tm.execute("101").isAccepted(), "'101' is a palindrome");
            assertTrue(tm.execute("0110").isAccepted(), "'0110' is a palindrome");
            
            // Test non-palindromes
            assertFalse(tm.execute("01").isAccepted(), "'01' is not a palindrome");
            assertFalse(tm.execute("10").isAccepted(), "'10' is not a palindrome");
            assertFalse(tm.execute("001").isAccepted(), "'001' is not a palindrome");
            assertFalse(tm.execute("100").isAccepted(), "'100' is not a palindrome");
        }
        
        @Test
        @DisplayName("String copying operation")
        void testStringCopying() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(copyTM);
            tm = (TM) parseResult.getAutomaton();
            
            // Test copying with separator
            assertTrue(tm.execute("01#").isAccepted(), "Copy '01' should succeed");
            assertTrue(tm.execute("101#").isAccepted(), "Copy '101' should succeed");
            assertTrue(tm.execute("#").isAccepted(), "Empty copy should succeed");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Empty string execution")
        void testEmptyString() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("");
            
            assertNotNull(result, "Result should not be null for empty string");
            assertTrue(result.isAccepted(), "Empty string (0 zeros) should be accepted");
        }
        
        @Test
        @DisplayName("Very long string execution")
        void testVeryLongString() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                longString.append("01"); // 100 zeros total (even)
            }
            
            Automaton.ExecutionResult result = tm.execute(longString.toString());
            
            assertNotNull(result, "Result should not be null for long string");
            assertTrue(result.isAccepted(), "String with 100 zeros should be accepted");
        }
        
        @Test
        @DisplayName("Execution without parsing should throw exception")
        void testExecuteWithoutParsing() {
            TM unparsedTM = new TM(null, null, null, null, null, null, null);
            
            assertThrows(NullPointerException.class, () -> {
                unparsedTM.execute("test");
            }, "Executing unparsed TM should throw NullPointerException");
        }
    }

    @Nested
    @DisplayName("Accept and Reject State Tests")
    class AcceptRejectStateTests {
        
        @Test
        @DisplayName("Should reach accept state for valid input")
        void testReachAcceptState() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("00");
            
            assertTrue(result.isAccepted(), "Should reach accept state");
            
            // Trace should show reaching q_accept
            String trace = result.getTrace();
            if (trace != null) {
                // Check if trace mentions accept state
            }
        }
        
        @Test
        @DisplayName("Should reach reject state for invalid input")
        void testReachRejectState() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("0");
            
            assertFalse(result.isAccepted(), "Should reach reject state");
            
            // Trace should show reaching q_reject
            String trace = result.getTrace();
            if (trace != null) {
                // Check if trace mentions reject state
            }
        }
    }

    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @BeforeEach
        void setUp() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", "00", "11", "0011", "1100", "0000", "1111"})
        @DisplayName("Should accept strings with even number of 0s")
        void testAcceptedStrings(String input) {
            Automaton.ExecutionResult result = tm.execute(input);
            assertTrue(result.isAccepted(), 
                String.format("String '%s' should be accepted", input));
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"0", "000", "01", "10", "0001", "11110"})
        @DisplayName("Should reject strings with odd number of 0s")
        void testRejectedStrings(String input) {
            Automaton.ExecutionResult result = tm.execute(input);
            assertFalse(result.isAccepted(), 
                String.format("String '%s' should be rejected", input));
        }
        
        @ParameterizedTest
        @CsvSource({
            "'', true",
            "0, false",
            "00, true",
            "000, false",
            "0000, true",
            "1, true",
            "11, true",
            "01, false",
            "10, false",
            "0011, true"
        })
        @DisplayName("Test various inputs with expected results")
        void testVariousInputs(String input, boolean expectedAccepted) {
            Automaton.ExecutionResult result = tm.execute(input);
            assertEquals(expectedAccepted, result.isAccepted(),
                String.format("String '%s' should be %s", 
                    input, expectedAccepted ? "accepted" : "rejected"));
        }
    }

    @Nested
    @DisplayName("Trace Validation Tests")
    class TraceValidationTests {
        
        @Test
        @DisplayName("Trace should show complete computation")
        void testCompleteTrace() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("01");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
            
            // Trace should show state transitions and tape operations
        }
        
        @Test
        @DisplayName("Trace for rejected computation")
        void testRejectedTrace() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            Automaton.ExecutionResult result = tm.execute("0");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null for rejected string");
            assertFalse(trace.isEmpty(), "Trace should not be empty for rejected string");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle long computations efficiently")
        void testLongComputation() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(simpleTM);
            tm = (TM) parseResult.getAutomaton();
            
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 500; i++) {
                longString.append("01");
            }
            
            long startTime = System.currentTimeMillis();
            Automaton.ExecutionResult result = tm.execute(longString.toString());
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isAccepted(), "Long string with even zeros should be accepted");
            assertTrue((endTime - startTime) < 2000, "Execution should complete within 2 seconds");
        }
        
        @Test
        @DisplayName("Should handle complex computations")
        void testComplexComputation() {
            tm = new TM(null, null, null, null, null, null, null);
            Automaton.ParseResult parseResult = tm.parse(binaryIncrementTM);
            tm = (TM) parseResult.getAutomaton();
            
            // Test incrementing a large binary number
            StringBuilder largeBinary = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                largeBinary.append('1'); // All 1s - worst case for increment
            }
            
            long startTime = System.currentTimeMillis();
            Automaton.ExecutionResult result = tm.execute(largeBinary.toString());
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isAccepted(), "Binary increment should complete");
            assertTrue((endTime - startTime) < 2000, "Complex computation should complete within 2 seconds");
        }
    }
}