package NondeterministicFiniteAutomaton;

import java.util.Objects;

public class Transition {

    private State from;
    private State to;
    private Symbol symbol;

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

    public String prettyPrint() {
        return from.getName() + " -> " + to.getName() + " (" + symbol.getC() + ")" + "\n";
    }
}
