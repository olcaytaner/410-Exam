package ContextFreeGrammar;

import java.io.IOException;

public class CFGTest {
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
