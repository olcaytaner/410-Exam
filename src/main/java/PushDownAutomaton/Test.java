package PushDownAutomaton;
import java.io.IOException;
import PushDownAutomaton.Exceptions.*;


public class Test {
    public static void main(String[] args) {
        String[] testFiles = {
                "src/main/java/PushDownAutomaton/example.txt"
        };

        for (String filePath : testFiles) {
            System.out.println("\n>>> Testing file: " + filePath);
            testParserFromFile(filePath);
        }

        System.out.println("\n--- All Tests Completed ---");
    }

    public static void testParserFromFile(String filePath) {
        try {
            PDAParser parser = new PDAParser();
            ParseResult result = parser.parseFromFile(filePath);

            System.out.println("PDA object created successfully.");

            if (result.hasWarnings()) {
                for (ParserWarning warning : result.getWarnings()) {
                    System.out.println("      " + warning);
                }
            }

        } catch (ParserException e) {
            System.out.println("    Parser FAILED: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("    FILE ERROR: Could not find or read the file. Please check the path: " + filePath);
        }
    }
}