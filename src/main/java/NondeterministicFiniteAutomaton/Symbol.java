package NondeterministicFiniteAutomaton;

import common.Automaton.ValidationMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a symbol in a nondeterministic finite automaton (NFA).
 * <p>
 * A symbol can either be a regular alphabet character or a special epsilon symbol ('_')
 * that denotes a transition without consuming input.
 * </p>
 */
public class Symbol {

    private final char c;

    /**
     * Constructs a Symbol with the specified character.
     *
     * @param c the character representing the symbol
     */
    public Symbol(char c) {
        this.c = c;
    }

    public char getC() {
        return c;
    }

    /**
     * Validates this symbol.
     * Ensures that the symbol is either a letter or the epsilon character ('_').
     *
     * @return list of validation messages; empty if the symbol is valid
     */
    public List<ValidationMessage> validate(){
        List<ValidationMessage> warnings = new ArrayList<>();

        if (!(isEpsilon() || Character.isLetter(getC()))){
            warnings.add(new ValidationMessage("Symbol is not valid: " + getC(), -1, ValidationMessage.ValidationMessageType.ERROR));
        }
        return warnings;
    }

    /**
     * Checks if the symbol represents an epsilon transition.
     *
     * @return true if the symbol is '_', false otherwise
     */
    public boolean isEpsilon() {
        return c == '_';
    }

    /**
     * Checks equality based on the character value.
     *
     * @param o the object to compare
     * @return true if the symbols are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return c == symbol.c;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(c);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "c=" + c +
                "}\n";
    }

    /**
     * Returns a human-readable description of this symbol.
     *
     * @return descriptive string
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        String s;
        if (isEpsilon()){
            s = "epsilon";
        }else {
            s = String.valueOf(c);
        }
        sb.append("Symbol: ").append(s).append("\n");
        return sb.toString();
    }

}
