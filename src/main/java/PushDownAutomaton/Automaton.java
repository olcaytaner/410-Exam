package PushDownAutomaton;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Automaton<T extends Transition> {
    protected Set<State> states;
    protected Set<String> inputAlphabet;
    protected State startState;
    protected Set<State> finalStates;
    protected Map<State, List<T>> transitionMap;

    public Automaton(Set<State> states,
                     Set<String> inputAlphabet,
                     State startState,
                     Set<State> finalStates,
                     Map<State, List<T>> transitionMap) {
        this.states = states;
        this.inputAlphabet = inputAlphabet;
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitionMap = transitionMap;
    }

    public abstract boolean simulate(String input);

    public abstract String generateGraphviz();
}