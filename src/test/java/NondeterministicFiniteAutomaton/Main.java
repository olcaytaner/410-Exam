package NondeterministicFiniteAutomaton;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Warning> warnings = NFA.validate("src/test/java/NondeterministicFiniteAutomaton/NFAtest.txt");
        System.out.println("warning count: " + warnings.size());
        for (int i = 0; i < warnings.size(); i++) {
            System.out.println(warnings.get(i));
        }
    }

}
