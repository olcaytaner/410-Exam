package NondeterministicFiniteAutomaton;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAutomaton {

    protected Map<String, State> states;
    protected Set<Symbol> alphabet;
    protected State startState;
    protected Set<State> finalStates;
    protected Map<State, List<Transition>> transitions;

    public AbstractAutomaton(Map<String, State> states, Set<Symbol> alphabet, State startState, Set<State> finalStates, Map<State, List<Transition>> transitions) {
        this.states = states;
        this.alphabet = alphabet;
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitions = transitions;
    }

    public abstract boolean accepts(String s);

    public abstract String toGraphViz();

    public static List<Warning> validate(String path) {
        return null;
    }

    public abstract void loadInput(String s);

    public Map<String, State> getStates() {
        return states;
    }

    public Set<Symbol> getAlphabet() {
        return alphabet;
    }

    public State getStartState() {
        return startState;
    }

    public Set<State> getFinalStates() {
        return finalStates;
    }

    public Map<State, List<Transition>> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return "AbstractAutomaton{ \n" +
                "states=\n" + states.toString() +
                ", \nalphabet=\n" + alphabet.toString() +
                ", \nstartState=\n" + startState.toString() +
                ", \nfinalStates=\n" + finalStates.toString() +
                ", \ntransitions=\n" + transitions.toString() +
                "}\n";
    }
}
