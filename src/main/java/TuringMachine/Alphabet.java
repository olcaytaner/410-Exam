package TuringMachine;

import common.Symbol;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an alphabet, a set of symbols, for a Turing Machine.
 */
public class Alphabet {
    private Set<Symbol> symbols;

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
    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    /**
     * Adds a symbol to the alphabet from a character.
     * @param symbolChar The character to create a symbol from.
     */
    public void addSymbol(char symbolChar) {
        symbols.add(new Symbol(symbolChar));
    }

    /**
     * Checks if the alphabet contains a specific symbol.
     * @param symbol The symbol to check.
     * @return True if the symbol is in the alphabet, false otherwise.
     */
    public boolean contains(Symbol symbol) {
        return symbols.contains(symbol);
    }

    /**
     * Checks if the alphabet contains a specific symbol from a character.
     * @param symbolChar The character to check.
     * @return True if the symbol is in the alphabet, false otherwise.
     */
    public boolean contains(char symbolChar) {
        return symbols.contains(new Symbol(symbolChar));
    }

    /**
     * Returns the set of symbols in the alphabet.
     * @return The set of symbols.
     */
    public Set<Symbol> getSymbols() {
        return symbols;
    }

    /**
     * Adds all symbols from another set to this alphabet.
     * @param other The set of symbols to add.
     */
    public void addAll(Set<Symbol> other) {
        symbols.addAll(other);
    }
}