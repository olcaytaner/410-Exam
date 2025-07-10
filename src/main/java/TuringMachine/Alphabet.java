package TuringMachine;

import java.util.HashSet;
import java.util.Set;

public class Alphabet {
    private Set<Character> symbols;

    public Alphabet() {
        symbols = new HashSet<>();
    }

    public void addSymbol(char symbol) {
        symbols.add(symbol);
    }

    public boolean contains(char symbol) {
        return symbols.contains(symbol);
    }

    public Set<Character> getSymbols() {
        return symbols;
    }

    public void addAll(Set<Character> other) {
        symbols.addAll(other);
    }

    public static boolean isValidSymbol(String symbol) {

        if (symbol.equals("_")) return true;

        if (symbol.length() == 1) return true;

        if (symbol.matches("\\d")) return true;

        return false;
    }
}
