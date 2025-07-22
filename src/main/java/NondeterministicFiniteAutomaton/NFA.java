package NondeterministicFiniteAutomaton;

import java.util.*;

public class NFA extends AbstractAutomaton {

    public NFA(Map<String, State> states, Set<Symbol> alphabet, State startState, Set<State> finalStates, Map<State, List<Transition>> transitions){

        super(states, alphabet, startState, finalStates, transitions);

    }

    public void addTransition(State from, State to, Symbol symbol){

        transitions.putIfAbsent(from, new ArrayList<>());
        transitions.get(from).add(new Transition(from, to, symbol));

    }

    public List<Warning> validate(){
        List<Warning> warnings = new ArrayList<>();

        Map<String, State> states = getStates();

        State startState = getStartState();

        if (startState == null){
            warnings.add(new Warning("No start state in NFA object", Warning.ERROR, -1, Warning.START_LINE));
        }else{
            if (!startState.isStartState()){
                warnings.add(new Warning("Start state is not a start state", Warning.ERROR, -1, Warning.START_LINE));
            }
            if (!startState.getName().matches(NFAParser.statePattern)){
                warnings.add(new Warning("Start state does not match valid state name", Warning.ERROR, -1, Warning.START_LINE));
            }
            if (!states.containsKey(startState.getName())){
                warnings.add(new Warning("States does not contain the start state",  Warning.ERROR, -1, Warning.START_LINE));
            }else {
                if (!states.get(startState.getName()).equals(startState)){
                    warnings.add(new Warning("Start state in states is not equal to the start state", Warning.ERROR, -1, Warning.START_LINE));
                }
            }
        }

        Set<State> finalStates = getFinalStates();
        if (finalStates == null || finalStates.isEmpty()){
            warnings.add(new Warning("No final state in NFA object", Warning.ERROR, -1, Warning.FINALS_LINE));
        }else {
            for (State finalState : finalStates){
                if (!finalState.isFinalState()){
                    warnings.add(new Warning("Final state: " + finalState.getName() + " is not a final state", Warning.ERROR, -1, Warning.FINALS_LINE));
                }
                if (!finalState.getName().matches(NFAParser.statePattern)){
                    warnings.add(new Warning("Final state: " + finalState.getName() + " does not match valid state name", Warning.ERROR, -1, Warning.FINALS_LINE));
                }
                if (!states.containsKey(finalState.getName())){
                    warnings.add(new Warning("States does not contain the final state: " + finalState.getName(),  Warning.ERROR, -1, Warning.FINALS_LINE));
                }else {
                    if (!states.get(finalState.getName()).equals(finalState)){
                        warnings.add(new Warning("Final state in states is not equal to the final state: " + finalState.getName(), Warning.ERROR, -1, Warning.FINALS_LINE));
                    }
                }
            }
        }

        Set<Symbol> alphabet = getAlphabet();
        if (alphabet == null || alphabet.isEmpty()){
            warnings.add(new Warning("Alphabet is empty", Warning.ERROR, -1, Warning.ALPHABET_LINE));
        }else {
            for (Symbol symbol : alphabet){
                for (Warning warning : symbol.validate()) {
                    warning.setLineName(Warning.ALPHABET_LINE);
                    warnings.add(warning);
                }
            }
        }

        Map<State, List<Transition>> transitions = getTransitions();
        for (State fromState : transitions.keySet()){
            if (!states.containsKey(fromState.getName())){
                warnings.add(new Warning("States does not contain the transition fromState: " + fromState.getName(), Warning.ERROR, -1, Warning.TRANS_LINE));
            }
            if (!fromState.getName().matches(NFAParser.statePattern)){
                warnings.add(new Warning("Transition fromState: " + fromState.getName() + " does not match valid state name", Warning.ERROR, -1, Warning.TRANS_LINE));
            }

            if (transitions.get(fromState) != null) {
                for (Transition t : transitions.get(fromState)){
                    List<Warning> transitionSymbolWarnings = t.getSymbol().validate();
                    warnings.addAll(transitionSymbolWarnings);

                    if (alphabet != null && !t.getSymbol().isEpsilon() && !alphabet.contains(t.getSymbol())){
                        warnings.add(new Warning("Alphabet does not contain transition symbol: " + t.getSymbol().getC(), Warning.ERROR, -1, Warning.TRANS_LINE));
                    }

                    State toState = t.getTo();
                    if (!states.containsKey(toState.getName())){
                        warnings.add(new Warning("States does not contain the transition toState: " + toState.getName(), Warning.ERROR, -1, Warning.TRANS_LINE));
                    }
                    if (!toState.getName().matches(NFAParser.statePattern)){
                        warnings.add(new Warning("Transition toState: " + toState.getName() + " does not match valid state name", Warning.ERROR, -1, Warning.TRANS_LINE));
                    }
                }
            }
        }

        int startCount = 0;
        int finalCount = 0;
        for (State state : states.values()){
            if (!state.getName().matches(NFAParser.statePattern)){
                warnings.add(new Warning("State name: " + state.getName() + " does not match valid state name", Warning.ERROR, -1, -1));
            }
            if (state.isStartState()){
                startCount++;
            }
            if (state.isFinalState()){
                finalCount++;
            }
        }

        if (startCount == 0){
            warnings.add(new Warning("No start state in states", Warning.ERROR, -1, -1));
        }else if (startCount > 1){
            warnings.add(new Warning("More than one start state in states", Warning.ERROR, -1, -1));
        }

        if (finalCount == 0){
            warnings.add(new Warning("No final state in states", Warning.ERROR, -1, -1));
        }

        return warnings;
    }

    @Override
    public boolean accepts(String s) {
        return false;
    }

    public String toGraphViz(){
        return null;
    }

    @Override
    public void loadInput(String s) {

    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String prettyPrint(){
        StringBuilder sb = new StringBuilder();

        sb.append("NFA\n");
        sb.append("States:\n");
        for (State state : this.states.values()){
            sb.append(state.prettyPrint()).append("\n");
        }

        sb.append("Alphabet:\n");
        for (Symbol symbol : this.alphabet){
            sb.append(symbol.prettyPrint()).append("\n");
        }

        sb.append("StartState:\n");
        sb.append(this.startState.prettyPrint()).append("\n");

        sb.append("FinalStates:\n");
        for (State state : this.finalStates){
            sb.append(state.prettyPrint()).append("\n");
        }

        sb.append("Transitions:\n");
        for (List<Transition> transitions : this.transitions.values()){
            for (Transition transition : transitions){
                sb.append(transition.prettyPrint()).append("\n");
            }
        }

        return sb.toString();
    }
}
