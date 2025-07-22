package TuringMachine;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class TMTestRunner {

    public static void runTests(String tmDefinitionFilePath, List<String> testInputs) {
        System.out.println("\n--- Running TM Tests ---");

        // Step 1: Validate the TM definition file
        List<Issue> validationIssues;
        try {
            validationIssues = TMFileValidator.validate(tmDefinitionFilePath); // Adjusted to use the current TMFileValidator
        } catch (IOException e) {
            System.err.println("Error reading TM definition file: " + e.getMessage());
            return;
        }

        if (!validationIssues.isEmpty()) {
            System.err.println("TM Definition is INVALID. Cannot run tests. Issues found:");
            for (Issue issue : validationIssues) {
                System.err.println(issue);
            }
            return;
        }

        System.out.println("TM Definition is VALID. Proceeding to parse and run tests.");

        // Step 2: Parse the TM definition to create a TuringMachine object
        TuringMachine tm;
        try {
            String tmContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(tmDefinitionFilePath)));
            tm = TMParser.parse(tmContent);
        } catch (IOException e) {
            System.err.println("Error reading TM definition file for parsing: " + e.getMessage());
            return;
        } catch (Exception e) {
            // Catch any unexpected parsing errors, though ideally validation should prevent them
            System.err.println("Error during TM parsing: " + e.getMessage());
            return;
        }

        System.out.println("Turing Machine parsed successfully.");

        // Step 3: Run tests with provided inputs
        for (int i = 0; i < testInputs.size(); i++) {
            String input = testInputs.get(i);
            System.out.println("\nTest " + (i + 1) + ": Input = \"" + input + "\"");
            boolean accepted = tm.simulate(input);
            System.out.println("Result: " + (accepted ? "ACCEPTED" : "REJECTED"));
            System.out.println("Final tape: " + tm.getTape().getTapeContents());
        }

        System.out.println("--- TM Tests Finished ---");
    }

    public static void main(String[] args) {
        // Example usage (you would replace these with actual test data)
        String sampleTmFilePath = "src/test/java/TuringMachine/tm_sample.txt";
        List<String> sampleInputs = Arrays.asList("0011", "0101", "111", "000");

        runTests(sampleTmFilePath, sampleInputs);
    }
}