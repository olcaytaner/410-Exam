package NondeterministicFiniteAutomaton;

import common.Automaton;

import java.util.*;

public class ExecuteTest {

    public static void main(String[] args) {

        Map<State, List<Transition>> transitions = new HashMap<>();

        State startState = new State("q1");
        startState.setStartState(true);

        Map<String, State> states = new HashMap<>();

        State state2 = new State("q2");
        State state3 = new State("q3");
        state3.setFinalState(true);

        Set<State> finalstates = new HashSet<>();
        finalstates.add(state3);

        states.put("q1", startState);
        states.put("q2", state2);
        states.put("q3", state3);


        Symbol symbol1 = new Symbol('a');
        Symbol symbol2 = new Symbol('b');
        Symbol symbol3 = new Symbol('c');
        Symbol epsilon = new Symbol('_');

        Set<Symbol> alphabet = new HashSet<>();
        alphabet.add(symbol1);
        alphabet.add(symbol2);
        alphabet.add(symbol3);
        alphabet.add(epsilon);

        Transition transition = new Transition(startState, state2, symbol2);
        Transition transition1 = new Transition(startState, state3, symbol2);
        Transition transition9 = new Transition(startState, state3, epsilon);

        Transition transition2 = new Transition(state2, state2, symbol1);
        Transition transition3 = new Transition(state2, state2, symbol3);
        Transition transition4 = new Transition(state2, state3, symbol2);
        Transition transition5 = new Transition(state2, state3, symbol3);
        Transition transition8 = new Transition(state2, state3, epsilon);

        Transition transition6 = new Transition(state3, startState, symbol1);
        Transition transition7 = new Transition(state3, startState, epsilon);

        transitions.put(startState, Arrays.asList(transition, transition1, transition9));
        transitions.put(state2, Arrays.asList(transition2, transition3, transition4, transition5, transition8));
        transitions.put(state3, Arrays.asList(transition6, transition7));


        NFA nfa = new NFA(states, alphabet, startState, finalstates, transitions);

        List<Automaton.ValidationMessage> messages = nfa.validate();

        System.out.println("To DotCode");
        System.out.println(nfa.toDotCode(null));

        System.out.println("Validation Messages:");
        if (!messages.isEmpty()){
            System.out.println(messages);
        }

        Automaton.ExecutionResult executionResult = nfa.execute("bcacb");

        System.out.println("trace: " + executionResult.getTrace());
        System.out.println("messages: " + executionResult.getRuntimeMessages());
        System.out.println("is accepted? " + executionResult.isAccepted());

    }

}
