package TuringMachine;

import common.Automaton;
import common.Symbol;
import common.Automaton.ValidationMessage.ValidationMessageType;

import java.util.*;

/**
 * Represents a Turing Machine.
 */
public class TM extends Automaton {
    private Set<State> states;
    private Alphabet inputAlphabet;
    private Alphabet tapeAlphabet;
    private Map<ConfigurationKey, Transition> transitionFunction;
    private State startState;
    private State acceptState;
    private State rejectState;
    private State currentState;
    private final Tape tape;

    public TM() {
        super(MachineType.TM);
        this.states = new HashSet<>();
        this.inputAlphabet = new Alphabet();
        this.tapeAlphabet = new Alphabet();
        this.transitionFunction = new HashMap<>();
        this.startState = null;
        this.acceptState = null;
        this.rejectState = null;
        this.tape = new Tape();
        this.currentState = null;
    }

    /**
     * Constructs a new TuringMachine.
     * @param states The set of states.
     * @param inputAlphabet The input alphabet.
     * @param tapeAlphabet The tape alphabet.
     * @param transitionFunction The transition function.
     * @param startState The start state.
     * @param acceptState The accept state.
     * @param rejectState The reject state.
     */
    public TM(Set<State> states,
                         Alphabet inputAlphabet,
                         Alphabet tapeAlphabet,
                         Map<ConfigurationKey, Transition> transitionFunction,
                         State startState,
                         State acceptState,
                         State rejectState) {
        super(MachineType.TM);
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

    @Override
    public String toDotCode(String inputText) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph TuringMachine {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : states) {
            if (state.isAccept()) {
                dot.append("  \"").append(state.getName()).append("\" [shape = doublecircle];\n");
            /*} else if (state.isReject()) {
                dot.append("  \"").append(state.getName()).append("\" [shape = square];\n");*///            } else {
                dot.append("  \"").append(state.getName()).append("\";\n");
            }
        }

        dot.append("  \"\" [shape = none];\n");
        dot.append("  \"\" -> \"").append(startState.getName()).append("\";\n");

        transitionFunction.forEach((key, value) -> {
            dot.append("  \"").append(key.getState().getName()).append("\" -> \"").append(value.getNextState().getName()).append("\" [label = \"")
               .append(key.getSymbolToRead()).append(" -> ").append(value.getSymbolToWrite()).append(", ").append(value.getMoveDirection() == Direction.LEFT ? "L" : "R").append("\"];\n");
        });

        dot.append("}\n");
        return dot.toString();
    }

    /**
     * Performs a single step of the Turing Machine's computation.
     */
    public void step() {
        Symbol currentSymbol = new Symbol(tape.read());
        Transition transition = transitionFunction.get(new ConfigurationKey(currentState, currentSymbol.getValue()));

        if (transition == null) {
            currentState = rejectState;
            return;
        }

        tape.write(transition.getSymbolToWrite());
        tape.move(transition.getMoveDirection());
        currentState = transition.getNextState();
    }

    /**
     * Resets the Turing Machine to its initial state.
     */
    public void reset() {
        tape.clear();
        currentState = startState;
    }

    

    /**
     * Returns the set of states in the Turing Machine.
     * @return The set of states.
     */
    public Set<State> getStates() {
        return states;
    }

    /**
     * Returns the input alphabet of the Turing Machine.
     * @return The input alphabet.
     */
    public Alphabet getInputAlphabet() {
        return inputAlphabet;
    }

    /**
     * Returns the start state of the Turing Machine.
     * @return The start state.
     */
    public State getStartState() {
        return startState;
    }

    /**
     * Returns the current state of the Turing Machine.
     * @return The current state.
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Returns the tape of the Turing Machine.
     * @return The tape.
     */
    public Tape getTape() {
        return tape;
    }

    @Override
    public ParseResult parse(String inputText) {
        List<ValidationMessage> validationMessages = validate(inputText);

        boolean hasErrors = validationMessages.stream().anyMatch(i -> i.getType() == ValidationMessageType.ERROR);
        if (hasErrors) {
            return new ParseResult(false, validationMessages, null);
        }

        TM machine = TMParser.parse(inputText);
        return new ParseResult(true, validationMessages, machine);
    }


    @Override
    public ExecutionResult execute(String inputText) {
        StringBuilder trace = new StringBuilder();
        reset();
        tape.initialize(inputText);
        currentState = startState;

        trace.append("Initial State: ").append(currentState.getName()).append(", Tape: ");
        tape.appendTapeTo(trace);
        trace.append("\n");

        while (!currentState.isAccept() && !currentState.isReject()) {
            step();
            trace.append("State: ").append(currentState.getName()).append(", Tape: ");
            tape.appendTapeTo(trace);
            trace.append("\n");
        }
        return new ExecutionResult(currentState.isAccept(), new ArrayList<>(), trace.toString());
    }

    @Override
    public List<ValidationMessage> validate() {
        return TMFileValidator.validateFromString(inputText);
    }

    public List<ValidationMessage> validate(String inputText) {
        return TMFileValidator.validateFromString(inputText);
}
}
