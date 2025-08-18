package PushDownAutomaton;

import common.State;
import common.Symbol;

/**
 * Represents a single transition rule specific to a Push-down Automaton.
 * It uses {@link Symbol} for single-character inputs (input, pop) and
 * {@link String} for the multi-character stack push operation.
 */
public class PDATransition {

    private final State fromState;
    private final State toState;
    private final Symbol inputSymbol;
    private final Symbol stackPop;
    private final String stackPush;

    /**
     * Constructs a new PDA transition.
     * @param fromState The state where the transition originates.
     * @param inputSymbol The input {@link Symbol} that triggers the transition.
     * @param stackPop The {@link Symbol} to be popped from the stack.
     * @param toState The destination state of the transition.
     * @param stackPush The {@link String} to be pushed onto the stack.
     */
    public PDATransition(State fromState, Symbol inputSymbol, Symbol stackPop, State toState, String stackPush) {
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

    public Symbol getInputSymbol() {
        return inputSymbol;
    }

    public Symbol getStackPop() {
        return stackPop;
    }

    public String getStackPush() {
        return stackPush;
    }
}
