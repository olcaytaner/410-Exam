package NondeterministicFiniteAutomaton;

import java.util.Objects;

/**
 * Represents a transition between two states in a nondeterministic finite automaton (NFA).
 * <p>
 * A transition connects a source state to a destination state using a given input symbol.
 * </p>
 */
public class Transition {

    private final State from;
    private final State to;
    private final Symbol symbol;

    /**
     * Constructs a Transition from one state to another with a given symbol.
     *
     * @param from   the source state
     * @param to     the destination state
     * @param symbol the symbol used for the transition
     */
    public Transition(State from, State to, Symbol symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    public State getFrom() {
        return from;
    }
    public State getTo() {
        return to;
    }
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Checks equality based on source, destination, and symbol.
     *
     * @param o the object to compare
     * @return true if the transitions are equivalent, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, symbol);
    }

    @Override
    public String toString() {
        return "Transition{" +
                "from=" + from +
                ", to=" + to +
                ", symbol=" + symbol +
                "}\n";
    }

    /**
     * Returns a human-readable format of the transition.
     *
     * @return formatted transition string
     */
    public String prettyPrint() {
        String s = String.valueOf(symbol.getC());
        if (symbol.isEpsilon()){
            s = "ε";
        }
        return from.getName() + " -> " + to.getName() + " (" + s + ")" + "\n";
    }
}
