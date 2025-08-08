package TuringMachine;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an alphabet, a set of symbols, for a Turing Machine.
 */
public class Alphabet {
    private Set<Character> symbols;

    /**
     * Constructs a new empty alphabet.
     */
    public Alphabet() {
        symbols = new HashSet<>();
    }

    /**
     * Adds a symbol to the alphabet.
     * @param symbol The symbol to add.
     */
    public void addSymbol(char symbol) {
        symbols.add(symbol);
    }

    /**
     * Checks if the alphabet contains a specific symbol.
     * @param symbol The symbol to check.
     * @return True if the symbol is in the alphabet, false otherwise.
     */
    public boolean contains(char symbol) {
        return symbols.contains(symbol);
    }

    /**
     * Returns the set of symbols in the alphabet.
     * @return The set of symbols.
     */
    public Set<Character> getSymbols() {
        return symbols;
    }

    /**
     * Adds all symbols from another set to this alphabet.
     * @param other The set of symbols to add.
     */
    public void addAll(Set<Character> other) {
        symbols.addAll(other);
    }

    /**
     * Checks if a given string is a valid symbol.
     * A symbol is valid if it is a single character, a digit, or the blank symbol '_'.
     * @param symbol The string to validate.
     * @return True if the string is a valid symbol, false otherwise.
     */
    public static boolean isValidSymbol(String symbol) {

        if (symbol.equals("_")) return true;

        if (symbol.length() == 1) return true;

        if (symbol.matches("\\d")) return true;

        return false;
    }
}
