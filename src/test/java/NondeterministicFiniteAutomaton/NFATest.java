package NondeterministicFiniteAutomaton;

import java.util.List;

public class NFATest {

    public static void main(String[] args) {

        String correctFilePath = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/NFAtest-Correct.txt";

        //Gets 10 Errors and 1 Warning and 2 correct transitions
        String incorrectFilePath = "src/test/java/NondeterministicFiniteAutomaton/TestFiles/NFAtest-Incorrect.txt";

        NFAParser nfaParser = new NFAParser(
                correctFilePath
        );

        ParseResult parseResult = nfaParser.parseNFA();

        NFA nfa = null;
        if (parseResult.getErrorCount() == 0){
            nfa = parseResult.getNfa();
            System.out.println(nfa.prettyPrint());
        }

        System.out.println("Syntax warning(s) count: " + parseResult.getWarnings().size());
        for (int i = 0; i < parseResult.getWarnings().size(); i++) {
            System.out.println(parseResult.getWarnings().get(i));
        }

        if (nfa != null) {
            List<Warning> warnings = nfa.validate();
            System.out.println("Validation warning(s) count: " + warnings.size());
            for (Warning warning : warnings){
                System.out.println(warning);
            }
        }

    }

}
