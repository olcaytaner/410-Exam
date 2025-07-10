package PushDownAutomaton;
import PushDownAutomaton.Exceptions.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class PDA extends Automaton<PDATransition> {
    private Set<String> stackAlphabet;
    private String stackStartSymbol;
    private Stack stack;

    public PDA(
            Set<State> states,
            Set<String> inputAlphabet,
            Set<String> stackAlphabet,
            State startState,
            Set<State> finalStates,
            String stackStartSymbol,
            Map<State, List<PDATransition>> transitionMap
    ) {
        super(states, inputAlphabet, startState, finalStates, transitionMap);
        this.stackAlphabet = stackAlphabet;
        this.stackStartSymbol = stackStartSymbol;
        this.stack = new Stack();
    }

    @Override
    public boolean simulate(String input) {
        return false;
    }

    @Override
    public String generateGraphviz() {
        return null;
    }
}