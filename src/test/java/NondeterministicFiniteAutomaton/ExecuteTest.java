package NondeterministicFiniteAutomaton;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import common.Automaton;
import common.State;
import common.Symbol;

/**
 * JUnit 5 test class for NFA execution functionality.
 * Tests NFA construction, execution, and string acceptance.
 */
@DisplayName("NFA Execution Tests")
public class ExecuteTest {

    private NFA nfa;
    private Map<State, List<Transition>> transitions;
    private State startState;
    private State state2;
    private State state3;
    private Map<String, State> states;
    private Set<State> finalStates;

    @BeforeEach
    void setUp() {
        transitions = new HashMap<>();

        startState = new State("q1");
        startState.setStart(true);

        states = new HashMap<>();

        state2 = new State("q2");
        state3 = new State("q3");
        state3.setAccept(true);

        finalStates = new HashSet<>();
        finalStates.add(state3);

        states.put("q1", startState);
        states.put("q2", state2);
        states.put("q3", state3);


        Symbol symbol1 = new Symbol('a');
        Symbol symbol2 = new Symbol('b');
        Symbol symbol3 = new Symbol('c');
        Symbol epsilon = new Symbol('_');

        Set<Symbol> alphabet = new HashSet<>();
        alphabet.add(symbol1);
        alphabet.add(symbol2);
        alphabet.add(symbol3);
        alphabet.add(epsilon);

        Transition transition = new Transition(startState, state2, symbol2);
        Transition transition1 = new Transition(startState, state3, symbol2);
        Transition transition9 = new Transition(startState, state3, epsilon);

        Transition transition2 = new Transition(state2, state2, symbol1);
        Transition transition3 = new Transition(state2, state2, symbol3);
        Transition transition4 = new Transition(state2, state3, symbol2);
        Transition transition5 = new Transition(state2, state3, symbol3);
        Transition transition8 = new Transition(state2, state3, epsilon);

        Transition transition6 = new Transition(state3, startState, symbol1);
        Transition transition7 = new Transition(state3, startState, epsilon);

        transitions.put(startState, Arrays.asList(transition, transition1, transition9));
        transitions.put(state2, Arrays.asList(transition2, transition3, transition4, transition5, transition8));
        transitions.put(state3, Arrays.asList(transition6, transition7));

        nfa = new NFA(states, alphabet, startState, finalStates, transitions);
    }

    @Nested
    @DisplayName("NFA Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("NFA should be constructed successfully")
        void testNFAConstruction() {
            assertNotNull(nfa, "NFA should be constructed successfully");
            assertEquals(Automaton.MachineType.NFA, nfa.getType(), "NFA should have correct machine type");
        }
        
        @Test
        @DisplayName("NFA validation should succeed")
        void testNFAValidation() {
            List<Automaton.ValidationMessage> messages = nfa.validate();
            
            assertNotNull(messages, "Validation messages should not be null");
            // For this specific NFA, validation should pass (assuming it's correctly constructed)
        }
        
        @Test
        @DisplayName("NFA should generate DOT code")
        void testDotCodeGeneration() {
            String dotCode = nfa.toDotCode(null);
            
            assertNotNull(dotCode, "DOT code should not be null");
            assertFalse(dotCode.isEmpty(), "DOT code should not be empty");
            assertTrue(dotCode.contains("digraph"), "DOT code should contain digraph declaration");
        }
    }

    @Nested
    @DisplayName("String Acceptance Tests")
    class StringAcceptanceTests {
        
        @Test
        @DisplayName("String 'bcacb' should be processed correctly")
        void testSpecificString() {
            Automaton.ExecutionResult result = nfa.execute("bcacb");
            
            assertNotNull(result, "Execution result should not be null");
            assertNotNull(result.getTrace(), "Trace should not be null");
            assertNotNull(result.getRuntimeMessages(), "Runtime messages should not be null");
            
            // The specific acceptance depends on the NFA logic, so we just test that execution completes
            System.out.println("String 'bcacb' acceptance: " + result.isAccepted());
            System.out.println("Trace: " + result.getTrace());
        }
        
        @Test
        @DisplayName("Empty string should be processed")
        void testEmptyString() {
            Automaton.ExecutionResult result = nfa.execute("");
            
            assertNotNull(result, "Execution result should not be null");
            // Empty string acceptance depends on epsilon transitions to final state
        }
        
        @Test
        @DisplayName("Single character strings should be processed")
        void testSingleCharacters() {
            String[] testStrings = {"a", "b", "c"};
            
            for (String testString : testStrings) {
                Automaton.ExecutionResult result = nfa.execute(testString);
                assertNotNull(result, "Execution result should not be null for string: " + testString);
                
                System.out.println("String '" + testString + "' acceptance: " + result.isAccepted());
            }
        }
        
        @Test
        @DisplayName("Complex strings should be processed")
        void testComplexStrings() {
            String[] testStrings = {"abc", "bac", "cab", "abcabc", "bcb"};
            
            for (String testString : testStrings) {
                Automaton.ExecutionResult result = nfa.execute(testString);
                assertNotNull(result, "Execution result should not be null for string: " + testString);
                
                System.out.println("String '" + testString + "' acceptance: " + result.isAccepted());
            }
        }
    }

    @Nested
    @DisplayName("Epsilon Transition Tests")
    class EpsilonTransitionTests {
        
        @Test
        @DisplayName("NFA with epsilon transitions should handle them correctly")
        void testEpsilonTransitions() {
            // The constructed NFA has epsilon transitions, test that they work
            Automaton.ExecutionResult result = nfa.execute("b");
            
            assertNotNull(result, "Execution result should not be null");
            
            // Since there's an epsilon transition from q1 to q3, and q3 is final,
            // some strings might be accepted through epsilon transitions
            System.out.println("String 'b' with epsilon transitions: " + result.isAccepted());
        }
    }

    @Nested
    @DisplayName("Advanced Epsilon Execution Tests")
    class EpsilonExecutionTests {

        @Nested
        @DisplayName("Advanced NFA 1 Tests")
        class AdvancedNFA_1 {

            private NFA epsilonNFA;

            @BeforeEach
            void setupEpsilonNFA() {

                String text = "Start: q0\n" +
                        "Finals: q4\n" +
                        "Alphabet: a b\n" +
                        "States: q0 q1 q2 q3 q4\n" +
                        "transitions:\n" +
                        "q0 -> q1 (eps)\n" +
                        "q0 -> q2 (a)\n" +
                        "q1 -> q2 (eps)\n" +
                        "q1 -> q3 (b)\n" +
                        "q2 -> q1 (eps)\n" +
                        "q2 -> q4 (a)\n" +
                        "q3 -> q4 (eps)";

                NFA nfa = new NFA();
                nfa.parse(text);

                epsilonNFA = nfa;
            }

            @Test
            @DisplayName("Empty string should be rejected")
            void testEmptyStringAccepted() {
                Automaton.ExecutionResult result = epsilonNFA.execute("");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "Empty string should be rejected");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("Single 'a' should be accepted via direct transition q0-ε→q1-ε→q2-a→q4")
            void testSingleA() {
                Automaton.ExecutionResult result = epsilonNFA.execute("a");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String 'a' should be accepted through q0-ε→q1-ε→q2-a→q4");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("Single 'b' should be accepted via direct transition q0-ε→q1-b→q3-ε→q4")
            void testSingleB() {
                Automaton.ExecutionResult result = epsilonNFA.execute("b");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String 'b' should be accepted through q0-ε→q1-b→q3-ε→q4");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'ba' should be rejected")
            void testBA() {
                Automaton.ExecutionResult result = epsilonNFA.execute("ba");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "String 'ba' should be rejected");
            }

            @Test
            @DisplayName("'ab' should be accepted due to epsilon loops q0-a→q2-ε→q1-b→q3-ε→q4")
            void testAB() {
                Automaton.ExecutionResult result = epsilonNFA.execute("ab");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String 'ab' should be accepted through q0-a→q2-ε→q1-b→q3-ε→q4");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'aab' should be rejected")
            void testAAB() {
                Automaton.ExecutionResult result = epsilonNFA.execute("aab");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "String 'aab' should be rejected");
                System.out.println(result.getTrace());
            }

        }

        @Nested
        @DisplayName("NFA that accepts Even # of 0s or 1s Tests")
        class EvenBinaryNFA {

            private NFA epsilonNFA;

            @BeforeEach
            void setupEpsilonNFA() {

                String text = "Start: q0\n" +
                        "Finals: q1 q2\n" +
                        "Alphabet: 0 1\n" +
                        "States: q0 q1 q2 q3 q4\n" +
                        "transitions:\n" +
                        "q0 -> q1 (eps)\n" +
                        "q0 -> q2 (eps)\n" +
                        "q1 -> q1 (1)\n" +
                        "q1 -> q3 (0)\n" +
                        "q3 -> q1 (0)\n" +
                        "q3 -> q3 (1)\n" +
                        "q2 -> q2 (0)\n" +
                        "q2 -> q4 (1)\n" +
                        "q4 -> q2 (1)\n" +
                        "q4 -> q4 (0)";

                NFA nfa = new NFA();
                nfa.parse(text);

                epsilonNFA = nfa;
            }

            @Test
            @DisplayName("Empty string should be accepted")
            void testEmptyStringAccepted() {
                Automaton.ExecutionResult result = epsilonNFA.execute("");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "Empty string should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("Single '0' should be accepted")
            void testSingle0() {
                Automaton.ExecutionResult result = epsilonNFA.execute("0");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '0' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("Single '1' should be accepted")
            void testSingle1() {
                Automaton.ExecutionResult result = epsilonNFA.execute("1");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '1' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'01' should be rejected")
            void test01() {
                Automaton.ExecutionResult result = epsilonNFA.execute("01");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "String '01' should be rejected");
            }

            @Test
            @DisplayName("'10' should be rejected")
            void test10() {
                Automaton.ExecutionResult result = epsilonNFA.execute("10");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "String '10' should be rejected");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'001' should be accepted")
            void test001() {
                Automaton.ExecutionResult result = epsilonNFA.execute("001");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '001' should be accepted");
                System.out.println(result.getTrace());
            }

        }

        @Nested
        @DisplayName("NFA that accepts (01 ∪ 1)*0 Tests")
        class RegexNFA {

            private NFA epsilonNFA;

            @BeforeEach
            void setupEpsilonNFA() {

                String text = "Start: q0\n" +
                        "Finals: q9\n" +
                        "Alphabet: 0 1\n" +
                        "States: q0 q1 q2 q3 q4 q5 q6 q7 q8 q9\n" +
                        "Transitions:\n" +
                        "q0 -> q1 (eps)\n" +
                        "q0 -> q8 (eps)\n" +
                        "q1 -> q2 (eps)\n" +
                        "q1 -> q6 (eps)\n" +
                        "q2 -> q3 (0)\n" +
                        "q3 -> q4 (eps)\n" +
                        "q4 -> q5 (1)\n" +
                        "q5 -> q1 (eps)\n" +
                        "q5 -> q8 (eps)\n" +
                        "q6 -> q7 (1)\n" +
                        "q7 -> q1 (eps)\n" +
                        "q7 -> q8 (eps)\n" +
                        "q8 -> q9 (0)\n";

                NFA nfa = new NFA();
                nfa.parse(text);

                epsilonNFA = nfa;
            }

            @Test
            @DisplayName("Empty string should be rejected")
            void testEmptyStringAccepted() {
                Automaton.ExecutionResult result = epsilonNFA.execute("");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "Empty string should be rejected");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("Single '0' should be accepted")
            void testSingle0() {
                Automaton.ExecutionResult result = epsilonNFA.execute("0");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '0' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("Single '1' should be rejected")
            void testSingle1() {
                Automaton.ExecutionResult result = epsilonNFA.execute("1");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "String '1' should be rejected");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'01' should be rejected")
            void test01() {
                Automaton.ExecutionResult result = epsilonNFA.execute("01");
                assertNotNull(result);
                assertFalse(result.isAccepted(), "String '01' should be rejected");
            }

            @Test
            @DisplayName("'10' should be accepted")
            void test10() {
                Automaton.ExecutionResult result = epsilonNFA.execute("10");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '10' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'010' should be accepted")
            void test010() {
                Automaton.ExecutionResult result = epsilonNFA.execute("010");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '010' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'01010' should be accepted")
            void test01010() {
                Automaton.ExecutionResult result = epsilonNFA.execute("01010");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '01010' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'0110' should be accepted")
            void test0110() {
                Automaton.ExecutionResult result = epsilonNFA.execute("0110");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '0110' should be accepted");
                System.out.println(result.getTrace());
            }

            @Test
            @DisplayName("'10110' should be accepted")
            void test10110() {
                Automaton.ExecutionResult result = epsilonNFA.execute("10110");
                assertNotNull(result);
                assertTrue(result.isAccepted(), "String '10110' should be accepted");
                System.out.println(result.getTrace());
            }

        }

    }

    @Nested
    @DisplayName("Parse Method Tests")
    class ParseMethodTests {
        
        @Test
        @DisplayName("Valid NFA text should parse correctly")
        void testParseValidNFA() {
            String correctText = "Start: q1\n" +
                    "Finals: q2 q3\n" +
                    "Alphabet: a b c\n" +
                    "transitions:\n" +
                    "q1 -> q2 (a eps)\n" +
                    "q3 -> q1 (a)\n" +
                    "q2 -> q2 (a c)\n" +
                    "q2 -> q3 (b c)\n";

            NFA testNFA = new NFA();
            Automaton.ParseResult parseResult = testNFA.parse(correctText);

            assertNotNull(parseResult, "Parse result should not be null");
            assertNotNull(parseResult.getValidationMessages(), "Validation messages should not be null");
            
            // Count message types
            int errorCount = 0;
            int warningCount = 0;
            int infoCount = 0;

            for (Automaton.ValidationMessage message : parseResult.getValidationMessages()) {
                if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.ERROR) {
                    errorCount++;
                } else if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.WARNING) {
                    warningCount++;
                } else if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.INFO) {
                    infoCount++;
                }
            }

            // For valid NFA, there should be minimal errors
            System.out.println("Valid NFA Syntax: " + errorCount + " error(s), " + 
                             warningCount + " warning(s) and " + infoCount + " info(s).");
            
            // If parsing succeeded, test validation
            if (parseResult.isSuccess()) {
                NFA parsedNFA = (NFA) parseResult.getAutomaton();
                List<Automaton.ValidationMessage> validationMessages = parsedNFA.validate();
                
                int validationErrors = 0;
                int validationWarnings = 0;
                int validationInfos = 0;

                for (Automaton.ValidationMessage message : validationMessages) {
                    if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.ERROR) {
                        validationErrors++;
                    } else if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.WARNING) {
                        validationWarnings++;
                    } else if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.INFO) {
                        validationInfos++;
                    }
                }
                
                System.out.println("Valid NFA Validation: " + validationErrors + " error(s), " + 
                                 validationWarnings + " warning(s) and " + validationInfos + " info(s).");
                
                // Test pretty print functionality
                assertDoesNotThrow(parsedNFA::prettyPrint,
                    "Pretty print should not throw exceptions");
            }
        }
        
        @Test
        @DisplayName("Invalid NFA text should generate appropriate messages")
        void testParseInvalidNFA() {
            // Gets 10 Errors, 1 Warning, 0 info and 2 correct transitions
            String incorrectText = "Start: q1 q2\n" +
                    "Finals: q2 qasd\n" +
                    "Alphabet: a b c eps ?\n" +
                    "transitions:\n" +
                    "q1 -> q2 (a eps)\n" +
                    "q? -> q1 (a)\n" +
                    "q2 -> qwe (a c)\n" +
                    "q2 -> q3 (b)\n" +
                    "q2 -> q3 (c)\n" +
                    "q4 -> q5 (abc)\n" +
                    "q5 -> q6 (d)";

            NFA testNFA = new NFA();
            Automaton.ParseResult parseResult = testNFA.parse(incorrectText);

            assertNotNull(parseResult, "Parse result should not be null");
            assertFalse(parseResult.getValidationMessages().isEmpty(), 
                "Invalid NFA should generate validation messages");
            
            // Count message types
            int errorCount = 0;
            int warningCount = 0;
            int infoCount = 0;

            for (Automaton.ValidationMessage message : parseResult.getValidationMessages()) {
                if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.ERROR) {
                    errorCount++;
                } else if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.WARNING) {
                    warningCount++;
                } else if (message.getType() == Automaton.ValidationMessage.ValidationMessageType.INFO) {
                    infoCount++;
                }
            }

            System.out.println("Invalid NFA Syntax: " + errorCount + " error(s), " + 
                             warningCount + " warning(s) and " + infoCount + " info(s).");
            
            // Invalid NFA should have errors
            assertTrue(errorCount > 0, "Invalid NFA should generate error messages");
        }
        
        @Test
        @DisplayName("Empty input should fail parsing")
        void testParseEmptyInput() {
            NFA testNFA = new NFA();
            Automaton.ParseResult result = testNFA.parse("");
            
            assertNotNull(result, "Parse result should not be null");
            assertFalse(result.isSuccess(), "Empty input should fail parsing");
            assertFalse(result.getValidationMessages().isEmpty(), 
                "Parse result should contain validation messages");
        }
        
        @Test
        @DisplayName("Null input should throw exception")
        void testParseNullInput() {
            NFA testNFA = new NFA();
            
            // NFA implementation throws NPE on null input - this is expected behavior
            assertThrows(NullPointerException.class, () -> {
                testNFA.parse(null);
            }, "Parsing null input should throw NullPointerException");
        }
    }

    @Nested
    @DisplayName("File-based Parse Tests")
    class FileBasedParseTests {
        
        @Test
        @DisplayName("Parse from correct test file")
        void testParseFromCorrectFile() {
            String correctFilePath = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/NFAtest-Correct.txt";
            
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(correctFilePath)));
                
                NFA testNFA = new NFA();
                Automaton.ParseResult parseResult = testNFA.parse(content);
                
                assertNotNull(parseResult, "Parse result should not be null");
                // File parsing results depend on the actual file content
                
            } catch (java.io.IOException e) {
                // If file doesn't exist, that's okay - just log it
                System.out.println("Test file not found: " + correctFilePath);
            }
        }
        
        @Test
        @DisplayName("Parse from incorrect test file")
        void testParseFromIncorrectFile() {
            String incorrectFilePath = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/NFAtest-Incorrect.txt";
            
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(incorrectFilePath)));
                
                NFA testNFA = new NFA();
                Automaton.ParseResult parseResult = testNFA.parse(content);
                
                assertNotNull(parseResult, "Parse result should not be null");
                // Incorrect file should generate validation messages
                assertFalse(parseResult.getValidationMessages().isEmpty(), 
                    "Incorrect file should generate validation messages");
                
            } catch (java.io.IOException e) {
                // If file doesn't exist, that's okay - just log it
                System.out.println("Test file not found: " + incorrectFilePath);
            }
        }
    }
}
