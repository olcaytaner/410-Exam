package PushDownAutomaton;

/**
 * Represents a single transition rule specific to a Push-down Automaton.
 * It includes the source and destination states, input symbol,
 * and stack operations (pop and push).
 */
public class PDATransition {

    private final State fromState;
    private final State toState;
    private final String inputSymbol;
    private final String stackPop;
    private final String stackPush;

    /**
     * Constructs a new PDA transition.
     * @param fromState The state where the transition originates.
     * @param inputSymbol The input symbol that triggers the transition (can be "eps").
     * @param stackPop The symbol to be popped from the stack (can be "eps").
     * @param toState The destination state of the transition.
     * @param stackPush The string to be pushed onto the stack (can be "eps").
     */
    public PDATransition(State fromState, String inputSymbol, String stackPop, State toState, String stackPush) {
        this.fromState = fromState;
        this.inputSymbol = inputSymbol;
        this.toState = toState;
        this.stackPop = stackPop;
        this.stackPush = stackPush;
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

    public String getStackPop() {
        return stackPop;
    }

    public String getStackPush() {
        return stackPush;
    }
}