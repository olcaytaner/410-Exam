package NondeterministicFiniteAutomaton;

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
    public String toString() {
        return "Transition{" +
                "from=" + from +
                ", to=" + to +
                ", symbol=" + symbol +
                "}\n";
    }
}
