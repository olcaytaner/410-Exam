package DeterministicFiniteAutomaton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class DFATest {

    private DFA dfa;
    private Set<State> states;
    private Set<Symbol> alphabet;
    private Set<State> finalStates;
    private Set<Transition> transitions;
    private State q0, q1, q2, q3, q4, q5, q6, q7;

    @BeforeEach
    void setUp() {
        setupComplexDFA();
    }

    private void setupComplexDFA() {
        // Create states
        q0 = new State("q0");
        q1 = new State("q1");
        q2 = new State("q2");
        q3 = new State("q3");
        q4 = new State("q4");
        q5 = new State("q5");
        q6 = new State("q6");
        q7 = new State("q7");

        states = new HashSet<>();
        states.add(q0);
        states.add(q1);
        states.add(q2);
        states.add(q3);
        states.add(q4);
        states.add(q5);
        states.add(q6);
        states.add(q7);

        // Create alphabet
        alphabet = new HashSet<>();
        alphabet.add(new Symbol('a'));
        alphabet.add(new Symbol('b'));
        alphabet.add(new Symbol('c'));

        // Set start state
        State startState = q0;

        // Set final states
        finalStates = new HashSet<>();
        finalStates.add(q5);
        finalStates.add(q7);

        // Create transitions based on dfa.txt
        transitions = new HashSet<>();
        transitions.add(new Transition(q0, new Symbol('c'), q0));
        transitions.add(new Transition(q0, new Symbol('a'), q1));
        transitions.add(new Transition(q0, new Symbol('b'), q1));
        transitions.add(new Transition(q1, new Symbol('a'), q2));
        transitions.add(new Transition(q1, new Symbol('b'), q3));
        transitions.add(new Transition(q1, new Symbol('c'), q1));
        transitions.add(new Transition(q2, new Symbol('a'), q2));
        transitions.add(new Transition(q2, new Symbol('c'), q2));
        transitions.add(new Transition(q2, new Symbol('b'), q4));
        transitions.add(new Transition(q3, new Symbol('a'), q4));
        transitions.add(new Transition(q3, new Symbol('b'), q3));
        transitions.add(new Transition(q3, new Symbol('c'), q3));
        transitions.add(new Transition(q4, new Symbol('a'), q5));
        transitions.add(new Transition(q4, new Symbol('b'), q6));
        transitions.add(new Transition(q4, new Symbol('c'), q6));
        transitions.add(new Transition(q5, new Symbol('a'), q5));
        transitions.add(new Transition(q5, new Symbol('b'), q5));
        transitions.add(new Transition(q5, new Symbol('c'), q5));
        transitions.add(new Transition(q6, new Symbol('a'), q7));
        transitions.add(new Transition(q6, new Symbol('b'), q0));
        transitions.add(new Transition(q6, new Symbol('c'), q0));
        transitions.add(new Transition(q7, new Symbol('a'), q7));
        transitions.add(new Transition(q7, new Symbol('b'), q7));
        transitions.add(new Transition(q7, new Symbol('c'), q7));

        // Create DFA
        dfa = new DFA(states, alphabet, startState, finalStates, transitions);
    }

    @Nested
    @DisplayName("Basic Validation Tests")
    class BasicValidationTests {
        @Test
        @DisplayName("Valid strings should be accepted")
        void testValidStrings() {
            assertTrue(dfa.validate("aabacb"));
            assertTrue(dfa.validate("abaaa"));
        }

        @Test
        @DisplayName("Invalid strings should be rejected")
        void testInvalidStrings() {
            assertFalse(dfa.validate("abacba"));
            assertFalse(dfa.validate("abacbab"));
            assertFalse(dfa.validate("aabcb"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        @Test
        @DisplayName("Empty string should be rejected (start state not final)")
        void testEmptyString() {
            assertFalse(dfa.validate(""));
        }

        @Test
        @DisplayName("Single character strings")
        void testSingleCharacters() {
            assertFalse(dfa.validate("a"));
            assertFalse(dfa.validate("b"));
            assertFalse(dfa.validate("c"));
        }

        @Test
        @DisplayName("Invalid symbols should cause rejection")
        void testInvalidSymbols() {
            assertFalse(dfa.validate("ax")); // 'x' not in alphabet
            assertFalse(dfa.validate("a1b")); // '1' not in alphabet
            assertFalse(dfa.validate("ab!")); // '!' not in alphabet
        }

        @Test
        @DisplayName("Very long valid string")
        void testLongValidString() {
            assertTrue(dfa.validate("aabacbaaaaaaaaaa"));
        }

        @Test
        @DisplayName("Very long invalid string")
        void testLongInvalidString() {
            assertFalse(dfa.validate("aabcbcccccccccc"));
        }
    }

    @Nested
    @DisplayName("Special DFA Configurations")
    class SpecialDFATests {
        @Test
        @DisplayName("DFA with start state as final state")
        void testStartStateAsFinal() {
            Set<State> singleState = new HashSet<>();
            State s0 = new State("s0");
            singleState.add(s0);

            Set<Symbol> simpleAlphabet = new HashSet<>();
            simpleAlphabet.add(new Symbol('a'));

            Set<State> finalStates = new HashSet<>();
            finalStates.add(s0);

            Set<Transition> selfLoop = new HashSet<>();
            selfLoop.add(new Transition(s0, new Symbol('a'), s0));

            DFA simpleDFA = new DFA(singleState, simpleAlphabet, s0, finalStates, selfLoop);

            assertTrue(simpleDFA.validate(""));
            assertTrue(simpleDFA.validate("a"));
            assertTrue(simpleDFA.validate("aa"));
            assertTrue(simpleDFA.validate("aaa"));
        }

        @Test
        @DisplayName("DFA with no final states")
        void testNoFinalStates() {
            Set<State> twoStates = new HashSet<>();
            State s0 = new State("s0");
            State s1 = new State("s1");
            twoStates.add(s0);
            twoStates.add(s1);

            Set<Symbol> simpleAlphabet = new HashSet<>();
            simpleAlphabet.add(new Symbol('a'));

            Set<State> emptyFinalStates = new HashSet<>();

            Set<Transition> transitions = new HashSet<>();
            transitions.add(new Transition(s0, new Symbol('a'), s1));
            transitions.add(new Transition(s1, new Symbol('a'), s0));

            DFA rejectAllDFA = new DFA(twoStates, simpleAlphabet, s0, emptyFinalStates, transitions);

            assertFalse(rejectAllDFA.validate(""));
            assertFalse(rejectAllDFA.validate("a"));
            assertFalse(rejectAllDFA.validate("aa"));
        }

        @Test
        @DisplayName("DFA with incomplete transitions")
        void testIncompleteTransitions() {
            Set<State> twoStates = new HashSet<>();
            State s0 = new State("s0");
            State s1 = new State("s1");
            twoStates.add(s0);
            twoStates.add(s1);

            Set<Symbol> alphabet = new HashSet<>();
            alphabet.add(new Symbol('a'));
            alphabet.add(new Symbol('b'));

            Set<State> finalStates = new HashSet<>();
            finalStates.add(s1);

            // Only transition for 'a', missing 'b' transition from s0
            Set<Transition> incompleteTransitions = new HashSet<>();
            incompleteTransitions.add(new Transition(s0, new Symbol('a'), s1));

            DFA incompleteDFA = new DFA(twoStates, alphabet, s0, finalStates, incompleteTransitions);

            assertTrue(incompleteDFA.validate("a"));
            assertFalse(incompleteDFA.validate("b")); // No transition for 'b'
            assertFalse(incompleteDFA.validate("ab"));
        }
    }

    @Nested
    @DisplayName("Graphviz Generation Tests")
    class GraphvizTests {
        @Test
        @DisplayName("Graphviz output should contain required elements")
        void testGraphvizGeneration() {
            String dot = dfa.toGraphviz();
            
            assertNotNull(dot);
            assertTrue(dot.contains("digraph DFA {"));
            assertTrue(dot.contains("rankdir=LR;"));
            assertTrue(dot.contains("node [shape = doublecircle];"));
            assertTrue(dot.contains("node [shape = circle];"));
            assertTrue(dot.contains("__start [shape=point];"));
            assertTrue(dot.contains("__start -> q0;"));
            assertTrue(dot.contains("}"));
        }

        @Test
        @DisplayName("Graphviz should include all final states")
        void testGraphvizFinalStates() {
            String dot = dfa.toGraphviz();
            
            assertTrue(dot.contains("q5"));
            assertTrue(dot.contains("q7"));
        }

        @Test
        @DisplayName("Graphviz should include sample transitions")
        void testGraphvizTransitions() {
            String dot = dfa.toGraphviz();
            
            assertTrue(dot.contains("q0 -> q1 [label=\"a\"];"));
            assertTrue(dot.contains("q0 -> q0 [label=\"c\"];"));
            assertTrue(dot.contains("q4 -> q5 [label=\"a\"];"));
        }

        @Test
        @DisplayName("Simple DFA Graphviz generation")
        void testSimpleGraphviz() {
            // Create a simple 2-state DFA
            Set<State> simpleStates = new HashSet<>();
            State start = new State("start");
            State end = new State("end");
            simpleStates.add(start);
            simpleStates.add(end);

            Set<Symbol> simpleAlphabet = new HashSet<>();
            simpleAlphabet.add(new Symbol('x'));

            Set<State> simpleFinals = new HashSet<>();
            simpleFinals.add(end);

            Set<Transition> simpleTransitions = new HashSet<>();
            simpleTransitions.add(new Transition(start, new Symbol('x'), end));

            DFA simpleDFA = new DFA(simpleStates, simpleAlphabet, start, simpleFinals, simpleTransitions);
            String dot = simpleDFA.toGraphviz();

            assertTrue(dot.contains("start -> end [label=\"x\"];"));
            assertTrue(dot.contains("end"));
        }
    }

    @Nested
    @DisplayName("DFA Validator Tests")
    class ValidatorTests {
        @Test
        @DisplayName("Complete DFA should pass validation")
        void testCompleteValidation() {
            assertDoesNotThrow(() -> DFAValidator.validateCompleteness(dfa));
        }

        @Test
        @DisplayName("Incomplete DFA should throw exception")
        void testIncompleteValidation() {
            // Create incomplete DFA (missing some transitions)
            Set<State> incompleteStates = new HashSet<>();
            State s0 = new State("s0");
            State s1 = new State("s1");
            incompleteStates.add(s0);
            incompleteStates.add(s1);

            Set<Symbol> incompleteAlphabet = new HashSet<>();
            incompleteAlphabet.add(new Symbol('a'));
            incompleteAlphabet.add(new Symbol('b'));

            Set<State> incompleteFinals = new HashSet<>();
            incompleteFinals.add(s1);

            Set<Transition> incompleteTransitions = new HashSet<>();
            // Only add transition for 'a' from s0, missing 'b' and all transitions from s1
            incompleteTransitions.add(new Transition(s0, new Symbol('a'), s1));

            DFA incompleteDFA = new DFA(incompleteStates, incompleteAlphabet, s0, incompleteFinals, incompleteTransitions);

            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> DFAValidator.validateCompleteness(incompleteDFA));
            
            assertTrue(exception.getMessage().contains("Incomplete DFA:"));
            assertTrue(exception.getMessage().contains("missing transition"));
        }

        @Test
        @DisplayName("DFA with self-loops should pass validation")
        void testSelfLoopValidation() {
            Set<State> loopStates = new HashSet<>();
            State loop = new State("loop");
            loopStates.add(loop);

            Set<Symbol> loopAlphabet = new HashSet<>();
            loopAlphabet.add(new Symbol('a'));
            loopAlphabet.add(new Symbol('b'));

            Set<State> loopFinals = new HashSet<>();
            loopFinals.add(loop);

            Set<Transition> loopTransitions = new HashSet<>();
            loopTransitions.add(new Transition(loop, new Symbol('a'), loop));
            loopTransitions.add(new Transition(loop, new Symbol('b'), loop));

            DFA loopDFA = new DFA(loopStates, loopAlphabet, loop, loopFinals, loopTransitions);

            assertDoesNotThrow(() -> DFAValidator.validateCompleteness(loopDFA));
        }
    }

    @Nested
    @DisplayName("State and Symbol Tests")
    class ComponentTests {
        @Test
        @DisplayName("Symbol equality and hashcode")
        void testSymbolEquality() {
            Symbol a1 = new Symbol('a');
            Symbol a2 = new Symbol('a');
            Symbol b = new Symbol('b');

            assertEquals(a1, a2);
            assertEquals(a1.hashCode(), a2.hashCode());
            assertNotEquals(a1, b);
            assertEquals('a', a1.getValue());
            assertEquals("a", a1.toString());
        }

        @Test
        @DisplayName("State equality and properties")
        void testStateEquality() {
            State s1 = new State("test");
            State s2 = new State("test");
            State s3 = new State("other");

            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
            assertNotEquals(s1, s3);
            assertEquals("test", s1.getName());
            assertEquals("test", s1.toString());
            
            assertFalse(s1.isStart());
            assertFalse(s1.isFinal());
            
            s1.setStart(true);
            s1.setFinal(true);
            
            assertTrue(s1.isStart());
            assertTrue(s1.isFinal());
        }

        @Test
        @DisplayName("State constructor with flags")
        void testStateConstructorWithFlags() {
            State startFinal = new State("sf", true, true);
            State regular = new State("reg", false, false);

            assertTrue(startFinal.isStart());
            assertTrue(startFinal.isFinal());
            assertFalse(regular.isStart());
            assertFalse(regular.isFinal());
        }

        @Test
        @DisplayName("Transition equality and properties")
        void testTransitionEquality() {
            State from = new State("from");
            State to = new State("to");
            Symbol symbol = new Symbol('a');

            Transition t1 = new Transition(from, symbol, to);
            Transition t2 = new Transition(from, symbol, to);
            Transition t3 = new Transition(to, symbol, from);

            assertEquals(t1, t2);
            assertEquals(t1.hashCode(), t2.hashCode());
            assertNotEquals(t1, t3);

            assertEquals(from, t1.getFrom());
            assertEquals(to, t1.getTo());
            assertEquals(symbol, t1.getSymbol());
            
            assertTrue(t1.toString().contains("from"));
            assertTrue(t1.toString().contains("to"));
            assertTrue(t1.toString().contains("a"));
        }
    }

    @Nested
    @DisplayName("Complex Scenario Tests")
    class ComplexScenarioTests {
        @Test
        @DisplayName("Binary number DFA (accepts strings ending in '01')")
        void testBinaryNumberDFA() {
            // Create DFA that accepts binary strings ending in "01"
            Set<State> binStates = new HashSet<>();
            State q0 = new State("q0"); // start
            State q1 = new State("q1"); // seen 0
            State q2 = new State("q2"); // seen 01 (accept)
            binStates.add(q0);
            binStates.add(q1);
            binStates.add(q2);

            Set<Symbol> binAlphabet = new HashSet<>();
            binAlphabet.add(new Symbol('0'));
            binAlphabet.add(new Symbol('1'));

            Set<State> binFinals = new HashSet<>();
            binFinals.add(q2);

            Set<Transition> binTransitions = new HashSet<>();
            binTransitions.add(new Transition(q0, new Symbol('0'), q1));
            binTransitions.add(new Transition(q0, new Symbol('1'), q0));
            binTransitions.add(new Transition(q1, new Symbol('0'), q1));
            binTransitions.add(new Transition(q1, new Symbol('1'), q2));
            binTransitions.add(new Transition(q2, new Symbol('0'), q1));
            binTransitions.add(new Transition(q2, new Symbol('1'), q0));

            DFA binaryDFA = new DFA(binStates, binAlphabet, q0, binFinals, binTransitions);

            assertTrue(binaryDFA.validate("01"));
            assertTrue(binaryDFA.validate("001"));
            assertTrue(binaryDFA.validate("101"));
            assertTrue(binaryDFA.validate("1001"));
            assertTrue(binaryDFA.validate("110101"));
            
            assertFalse(binaryDFA.validate(""));
            assertFalse(binaryDFA.validate("0"));
            assertFalse(binaryDFA.validate("1"));
            assertFalse(binaryDFA.validate("10"));
            assertFalse(binaryDFA.validate("011"));
        }

        @Test
        @DisplayName("Even number of 'a's DFA")
        void testEvenAsCountDFA() {
            Set<State> evenStates = new HashSet<>();
            State even = new State("even");
            State odd = new State("odd");
            evenStates.add(even);
            evenStates.add(odd);

            Set<Symbol> evenAlphabet = new HashSet<>();
            evenAlphabet.add(new Symbol('a'));
            evenAlphabet.add(new Symbol('b'));

            Set<State> evenFinals = new HashSet<>();
            evenFinals.add(even);

            Set<Transition> evenTransitions = new HashSet<>();
            evenTransitions.add(new Transition(even, new Symbol('a'), odd));
            evenTransitions.add(new Transition(even, new Symbol('b'), even));
            evenTransitions.add(new Transition(odd, new Symbol('a'), even));
            evenTransitions.add(new Transition(odd, new Symbol('b'), odd));

            DFA evenDFA = new DFA(evenStates, evenAlphabet, even, evenFinals, evenTransitions);

            assertTrue(evenDFA.validate(""));
            assertTrue(evenDFA.validate("aa"));
            assertTrue(evenDFA.validate("bbbaaabbab"));
            assertTrue(evenDFA.validate("abab"));
            
            assertFalse(evenDFA.validate("a"));
            assertFalse(evenDFA.validate("aaa"));
            assertFalse(evenDFA.validate("bab"));
        }
    }
}
