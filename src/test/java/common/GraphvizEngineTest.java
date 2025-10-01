package common;

import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Tests GraphViz rendering across different Java versions.
 * This test verifies that the GraphvizJdkEngine (GraalVM) works
 * correctly on Java 8, 11, 17, 21, and 24.
 */
@DisplayName("GraphViz Multi-Version Compatibility Tests")
class GraphvizEngineTest {

    @BeforeAll
    static void printJavaInfo() {
        String separator = generateSeparator(60);
        System.out.println("\n" + separator);
        System.out.println("JAVA ENVIRONMENT INFO");
        System.out.println(separator);
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        System.out.println("OS: " + System.getProperty("os.name") +
                         " (" + System.getProperty("os.arch") + ")");
        System.out.println(separator + "\n");
    }

    /**
     * Helper method to generate separator string (Java 8 compatible).
     * String.repeat() was added in Java 11, so we use StringBuilder for compatibility.
     */
    private static String generateSeparator(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append("=");
        }
        return sb.toString();
    }

    @Test
    @DisplayName("DFA GraphViz Rendering - Simple Automaton")
    void testDFASimpleRendering() {
        String dfaInput =
            "states: q0 q1 q2\n" +
            "alphabet: a b\n" +
            "start: q0\n" +
            "finals: q2\n" +
            "transitions:\n" +
            "q0 -> q1 (a)\n" +
            "q1 -> q2 (b)\n" +
            "q2 -> q0 (a)\n";

        DFA dfa = new DFA();

        // Test parsing
        Automaton.ParseResult parseResult = dfa.parse(dfaInput);
        assertTrue(parseResult.isSuccess(),
                  "DFA parsing should succeed");
        assertEquals(0, parseResult.getValidationMessages().stream()
                      .filter(m -> m.getType() == Automaton.ValidationMessage.ValidationMessageType.ERROR)
                      .count(),
                  "Should have no parse errors");

        // Test GraphViz rendering
        JLabel result = dfa.toGraphviz(dfaInput);
        assertNotNull(result, "GraphViz should return a JLabel");
        assertNotNull(result.getIcon(), "JLabel should contain an icon");
        assertTrue(result.getIcon() instanceof ImageIcon,
                  "Icon should be an ImageIcon");

        // Verify image data
        ImageIcon icon = (ImageIcon) result.getIcon();
        assertTrue(icon.getIconWidth() > 0, "Image should have width");
        assertTrue(icon.getIconHeight() > 0, "Image should have height");

        System.out.println("✅ DFA rendering successful - " +
                         icon.getIconWidth() + "x" + icon.getIconHeight() + " pixels");
    }

    @Test
    @DisplayName("NFA GraphViz Rendering - With Epsilon Transitions")
    void testNFAEpsilonRendering() {
        String nfaInput =
            "states: q0 q1 q2 q3\n" +
            "alphabet: a b\n" +
            "start: q0\n" +
            "finals: q3\n" +
            "transitions:\n" +
            "q0 -> q1 (a eps)\n" +
            "q1 -> q2 (b)\n" +
            "q2 -> q3 (eps)\n" +
            "q0 -> q3 (a b)\n";

        NFA nfa = new NFA();

        // Test parsing
        Automaton.ParseResult parseResult = nfa.parse(nfaInput);
        assertTrue(parseResult.isSuccess(),
                  "NFA parsing should succeed");

        // Test GraphViz rendering
        JLabel result = nfa.toGraphviz(nfaInput);
        assertNotNull(result, "GraphViz should return a JLabel");
        assertNotNull(result.getIcon(), "JLabel should contain an icon");

        ImageIcon icon = (ImageIcon) result.getIcon();
        assertTrue(icon.getIconWidth() > 0, "Image should have width");
        assertTrue(icon.getIconHeight() > 0, "Image should have height");

        System.out.println("✅ NFA rendering successful - " +
                         icon.getIconWidth() + "x" + icon.getIconHeight() + " pixels");
    }

    @Test
    @DisplayName("Complex DFA - Stress Test")
    void testComplexDFA() {
        StringBuilder dfaInput = new StringBuilder();
        dfaInput.append("states: ");
        for (int i = 0; i < 10; i++) {
            dfaInput.append("q").append(i).append(" ");
        }
        dfaInput.append("\nalphabet: a b c\n");
        dfaInput.append("start: q0\n");
        dfaInput.append("finals: q9\n");
        dfaInput.append("transitions:\n");
        for (int i = 0; i < 9; i++) {
            dfaInput.append("q").append(i).append(" -> q").append(i+1).append(" (a)\n");
            dfaInput.append("q").append(i).append(" -> q").append((i+2)%10).append(" (b)\n");
        }

        DFA dfa = new DFA();
        Automaton.ParseResult parseResult = dfa.parse(dfaInput.toString());
        assertTrue(parseResult.isSuccess(), "Complex DFA parsing should succeed");

        JLabel result = dfa.toGraphviz(dfaInput.toString());
        assertNotNull(result, "Complex DFA should render");
        assertNotNull(result.getIcon(), "Should have rendered graph");

        System.out.println("✅ Complex DFA (10 states) rendered successfully");
    }

    @Test
    @DisplayName("Error Case - Invalid Automaton")
    void testInvalidAutomatonRendering() {
        String invalidInput = "this is not valid";

        DFA dfa = new DFA();
        Automaton.ParseResult parseResult = dfa.parse(invalidInput);

        // Parsing should fail
        assertFalse(parseResult.isSuccess(),
                   "Invalid input should fail parsing");

        // GraphViz should still return a label (with error message)
        JLabel result = dfa.toGraphviz(invalidInput);
        assertNotNull(result, "Should return error label");

        System.out.println("✅ Error case handled correctly");
    }
}
