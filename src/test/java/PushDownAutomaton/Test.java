package PushDownAutomaton;

import common.Automaton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {

    public static void main(String[] args) {
        String filePath = "src/test/java/PushDownAutomaton/example.txt";

        System.out.println("\n>>> Testing PDA parsing from file: " + filePath);

        try {
            String pdaDescription = new String(Files.readAllBytes(Paths.get(filePath)));

            PDA pda = new PDA();

            Automaton.ParseResult result = pda.parse(pdaDescription);

            if (result.isSuccess()) {
                System.out.println("    SUCCESS: PDA parsed successfully.");

                System.out.println("\n--- Generated .dot code ---\n");
                System.out.println(pda.toDotCode(null));
                System.out.println("\n---------------------------\n");

            } else {
                System.out.println("    FAILED: PDA could not be parsed.");
            }

            if (result.getValidationMessages() != null && !result.getValidationMessages().isEmpty()) {
                System.out.println("\n--- Validation Messages ---");
                for (Automaton.ValidationMessage message : result.getValidationMessages()) {
                    System.out.println("    " + message);
                }
                System.out.println("---------------------------");
            }

        } catch (IOException e) {
            System.err.println("    FILE ERROR: Could not read the file at path: " + filePath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("    UNEXPECTED ERROR during parsing: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- Test Completed ---");
    }
}