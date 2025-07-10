package PushDownAutomaton;

public abstract class Transition {
    private final State fromState;
    private final State toState;
    private final String inputSymbol;

    public Transition(State fromState, String inputSymbol, State toState) {
        this.fromState = fromState;
        this.inputSymbol = inputSymbol;
        this.toState = toState;
    }

    public State getFromState() {
        return fromState;
    }

    public State getToState() {
        return toState;
    }

    public String getInputSymbol() {
        return inputSymbol;
    }
}