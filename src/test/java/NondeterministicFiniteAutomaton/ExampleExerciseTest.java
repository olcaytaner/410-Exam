package NondeterministicFiniteAutomaton;

import common.Automaton;
import common.Automaton.ParseResult;
import common.Automaton.ValidationMessage;
import common.Automaton.ValidationMessage.ValidationMessageType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ExampleExerciseTest {

    public static void main(String[] args) throws IOException {

        String s = "week4-";

        for (int i = 9; i <= 16; i++) {
            System.out.println(test(s + i, false));
        }

    }

    public static String test(String path, boolean verbose) throws IOException {

        String week = getExercisePath(path);

        String input = readFile(week);
        NFA nfa = new NFA();
        ParseResult parseResult = nfa.parse(input);

        int errorCount = 0;
        int warningCount = 0;
        int infoCount = 0;

        for (ValidationMessage warning : parseResult.getValidationMessages()){
            if (warning.getType() == ValidationMessage.ValidationMessageType.ERROR){
                errorCount++;
            }else if (warning.getType() == ValidationMessage.ValidationMessageType.WARNING){
                warningCount++;
            }else if (warning.getType() == ValidationMessage.ValidationMessageType.INFO){
                infoCount++;
            }
        }

        if (errorCount == 0){
            nfa = (NFA) parseResult.getAutomaton();
            //System.out.println(nfa.prettyPrint());
        }

        if (verbose){
            System.out.println("\n Syntax: " + errorCount + " error(s), " + warningCount + " warning(s) and " + infoCount + " info(s). ");
        }

        int validationErrors = 0;
        int validationWarnings = 0;
        int validationInfos = 0;

        if (parseResult.isSuccess()) {
            if (verbose) {
                System.out.println(nfa.toDotCode(null));
            }

            List<Automaton.ValidationMessage> warnings = nfa.validate();
            for (ValidationMessage warning : warnings){
                if (warning.getType() == ValidationMessageType.ERROR){
                    validationErrors++;
                }else if (warning.getType() == ValidationMessageType.WARNING){
                    validationWarnings++;
                }else if (warning.getType() == ValidationMessageType.INFO){
                    validationInfos++;
                }
            }

            if (verbose) {
                System.out.println("\n Validation: " + validationErrors + " error(s), " + validationWarnings + " warning(s) and " + validationInfos + " info(s). ");
            }
            if (validationErrors == 0){
                return path + " Passed";
            }
        }
        return path + " Failed";
    }

    private static String readFile(String file) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.lineSeparator();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }

    }

    private static String getExercisePath(String week){
        String s = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/Exercises/%s/%s.txt";
        return String.format(s, week.substring(0, 5), week);
    }

}
