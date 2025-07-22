package TuringMachine;

import java.util.Map;
import java.util.Set;

public class TuringMachine {
    private final Set<State> states;
    private final Alphabet inputAlphabet;
    private final Alphabet tapeAlphabet;
    private final Map<ConfigurationKey, Transition> transitionFunction;
    private final State startState;
    private final State acceptState;
    private final State rejectState;
    private State currentState;
    private final Tape tape;

    public TuringMachine(Set<State> states,
                         Alphabet inputAlphabet,
                         Alphabet tapeAlphabet,
                         Map<ConfigurationKey, Transition> transitionFunction,
                         State startState,
                         State acceptState,
                         State rejectState) {
        this.states = states;
        this.inputAlphabet = inputAlphabet;
        this.tapeAlphabet = tapeAlphabet;
        this.transitionFunction = transitionFunction;
        this.startState = startState;
        this.acceptState = acceptState;
        this.rejectState = rejectState;
        this.tape = new Tape();
        reset();
    }

    public boolean simulate(String input) {
        reset();
        tape.initialize(input);
        currentState = startState;

        while (!currentState.isAccept() && !currentState.isReject()) {
            step();
        }
        return currentState.isAccept();
    }

    public void step() {
        char currentSymbol = tape.read();
        Transition transition = transitionFunction.get(new ConfigurationKey(currentState, currentSymbol));

        if (transition == null) {
            currentState = rejectState;
            return;
        }

        tape.write(transition.getWriteSymbol());
        tape.move(transition.getMoveDirection());
        currentState = transition.getNextState();
    }

    public void reset() {
        tape.clear();
        currentState = startState;
    }

    public void printState() {
        tape.printTape();
    }

    public Set<State> getStates() {
        return states;
    }

    public Alphabet getInputAlphabet() {
        return inputAlphabet;
    }

    public State getStartState() {
        return startState;
    }

    public State getCurrentState() {
        return currentState;
    }

    public Tape getTape() {
        return tape;
    }
}