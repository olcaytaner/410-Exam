package TuringMachine;

import common.Automaton;
import common.Automaton.ValidationMessage.ValidationMessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TuringMachine extends Automaton {
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
                dot.append("  \"").append(state.getName()).append("\" [shape = square];\n");*/
            } else {
                dot.append("  \"").append(state.getName()).append("\";\n");
            }
        }

        dot.append("  \"\" [shape = none];\n");
        dot.append("  \"\" -> \"").append(startState.getName()).append("\";\n");

        transitionFunction.forEach((key, value) -> {
            dot.append("  \"").append(key.getState().getName()).append("\" -> \"").append(value.getNextState().getName()).append("\" [label = \"")
               .append(key.getReadSymbol()).append(" -> ").append(value.getWriteSymbol()).append(", ").append(value.getMoveDirection()).append("\"];\n");
        });

        dot.append("}\n");
        return dot.toString();
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

    @Override
    public ParseResult parse(String inputText) {
        List<ValidationMessage> validationMessages = validate(inputText);

        boolean hasErrors = validationMessages.stream().anyMatch(i -> i.getType() == ValidationMessageType.ERROR);
        if (hasErrors) {
            return new ParseResult(false, validationMessages, null);
        }

        TuringMachine machine = TMParser.parse(inputText);
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
        return new ArrayList<>();
    }

    public List<ValidationMessage> validate(String inputText) {
        return TMFileValidator.validateFromString(inputText);
}
}