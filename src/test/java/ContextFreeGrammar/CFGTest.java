package ContextFreeGrammar;

import java.io.IOException;

/**
 * Main class for the Context-Free Grammar parser application.
 * This class demonstrates the usage of the CFG parser by reading a grammar file,
 * validating it, and displaying the results.
 *
 * @author yenennn
 * @version 1.0
 */
public class CFGTest {

    /**
     * Main method that runs the CFG parser demonstration.
     * Reads a grammar file, parses it, validates the grammar, and displays
     * the results in a readable format.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        String filePath = "src/test/java/ContextFreeGrammar/test.txt";

        try {
            // Parse the grammar file
            System.out.println("Parsing grammar from file: " + filePath);
            CFG grammar = CFGParser.parse(filePath);

            // Validate the grammar
            System.out.println("\nValidating grammar...");
            boolean isValid = grammar.validateGrammar();
            System.out.println("Grammar is " + (isValid ? "valid" : "invalid"));

            // Pretty print the grammar
            System.out.println("\nGrammar in readable format:");
            grammar.prettyPrint();

            System.out.println(grammar);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (GrammarParseException e) {
            System.err.println("Error parsing grammar: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}