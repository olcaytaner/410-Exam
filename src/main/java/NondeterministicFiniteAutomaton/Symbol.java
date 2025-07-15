package NondeterministicFiniteAutomaton;

public class Symbol {

    private char c;

    public Symbol(char c) {
        this.c = c;
    }

    public char getC() {
        return c;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "c=" + c +
                "}\n";
    }
}
