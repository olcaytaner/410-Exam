package TuringMachine;

import common.Automaton.ParseResult;
import common.Automaton.ValidationMessage;
import common.Automaton.ExecutionResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A test runner for the Turing Machine.
 */
public class TMTestRunner {

    /**
     * Runs tests on a Turing Machine definition.
     * @param tmDefinitionFilePath The path to the Turing Machine definition file.
     * @param testInputsFilePath The path to the file containing the test inputs.
     */
    public static void runTests(String tmDefinitionFilePath, String testInputsFilePath) {
        System.out.println("\n--- Running TM Tests ---");

        String tmContent;
        try {
            tmContent = new String(Files.readAllBytes(Paths.get(tmDefinitionFilePath)));
        } catch (IOException e) {
            System.err.println("Error reading TM definition file: " + e.getMessage());
            return;
        }

        TuringMachine tm = new TuringMachine(null, null, null, null, null, null, null);
        ParseResult parseResult = tm.parse(tmContent);

        if (!parseResult.isSuccess()) {
            System.err.println("TM Definition is INVALID. Cannot run tests. Issues found:");
            for (ValidationMessage message : parseResult.getValidationMessages()) {
                System.err.println(message);
            }
            return;
        }

        TuringMachine parsedTM = (TuringMachine) parseResult.getAutomaton();
        System.out.println("TM Definition is VALID. Proceeding to run tests.");

        List<String> testInputs;
        try {
            List<String> allLines = Files.readAllLines(Paths.get(testInputsFilePath));
            testInputs = new ArrayList<>();
            for (String line : allLines) {
                if (!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                    testInputs.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading test inputs file: " + e.getMessage());
            return;
        }

        for (int i = 0; i < testInputs.size(); i++) {
            String input = testInputs.get(i);
            System.out.println("\nTest " + (i + 1) + ": Input = \"" + input + "\"");
            ExecutionResult executionResult = parsedTM.execute(input);
            System.out.println("Result: " + (executionResult.isAccepted() ? "ACCEPTED" : "REJECTED"));
            System.out.println("Final tape: " + parsedTM.getTape().getTapeContents());
        }

        System.out.println("--- TM Tests Finished ---");
    }

    /**
     * The main method.
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        String sampleTmFilePath = "src/test/java/TuringMachine/tm_sample.txt";
        String testInputsFilePath = "src/test/java/TuringMachine/tm_test_inputs.txt";

        runTests(sampleTmFilePath, testInputsFilePath);
    }
}
