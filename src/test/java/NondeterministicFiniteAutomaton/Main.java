package NondeterministicFiniteAutomaton;

import common.Automaton;
import common.Automaton.ParseResult;
import common.Automaton.ValidationMessage;
import common.Automaton.ValidationMessage.ValidationMessageType;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String correctFilePath = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/NFAtest-Correct.txt";

        //Gets 10 Errors, 1 Warning, 0 info and 2 correct transitions
        String incorrectFilePath = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/NFAtest-Incorrect.txt";

        String correctText = "Start: q1\n" +
                "Finals: q2 q3\n" +
                "Alphabet: a b c\n" +
                "transitions:\n" +
                "q1 -> q2 (a eps)\n" +
                "q3 -> q1 (a)\n" +
                "q2 -> q2 (a c)\n" +
                "q2 -> q3 (b c)\n";

        //Gets 10 Errors, 1 Warning, 0 info and 2 correct transitions
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

        NFA nfa = new NFA();

        ParseResult parseResult = nfa.parse(incorrectText);

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
            System.out.println(nfa.prettyPrint());
        }

        System.out.println("\n Syntax: " + errorCount + " error(s), " + warningCount + " warning(s) and " + infoCount + " info(s). ");

        int validationErrors = 0;
        int validationWarnings = 0;
        int validationInfos = 0;

        if (parseResult.isSuccess()) {
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
            System.out.println("\n Validation: " + validationErrors + " error(s), " + validationWarnings + " warning(s) and " + validationInfos + " info(s). ");
        }
    }

}
