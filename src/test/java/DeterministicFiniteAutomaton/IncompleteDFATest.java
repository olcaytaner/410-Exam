package DeterministicFiniteAutomaton;

import common.Automaton;
import org.junit.jupiter.api.Test;

public class IncompleteDFATest {
    
    @Test
    public void testIncompleteDFA() {
        String content = "Start: q0\n" +
                        "Finals: q1\n" +
                        "Alphabet: a b\n" +
                        "States: q0 q1\n" +
                        "\n" +
                        "Transitions:\n" +
                        "q0 -> q1 (a)\n";
        
        DFA dfa = new DFA();
        dfa.setInputText(content); // Set input text first
        Automaton.ParseResult result = dfa.parse(content);
        
        System.out.println("Parse success: " + result.isSuccess());
        System.out.println("Parse Messages:");
        for (Automaton.ValidationMessage msg : result.getValidationMessages()) {
            System.out.println("  " + msg.getType() + ": " + msg.getMessage());
        }
        
        if (result.isSuccess()) {
            System.out.println("\nValidation Messages:");
            for (Automaton.ValidationMessage msg : dfa.validate()) {
                System.out.println("  " + msg.getType() + ": " + msg.getMessage());
            }
        }
    }
}
