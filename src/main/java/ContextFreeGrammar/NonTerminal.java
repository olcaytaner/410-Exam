package ContextFreeGrammar;

import common.Symbol;

/**
 * Represents a non-terminal symbol in a context-free grammar.
 * Non-terminal symbols can be replaced by other symbols according to production rules.
 */
public class NonTerminal extends Symbol {
    private final String name;

    public NonTerminal(String name) {
        super(name.isEmpty() ? 'A' : name.charAt(0));
        this.name = name;
    }

    public NonTerminal(char value) {
        super(value);
        this.name = Character.toString(value);
    }

    @Override
    public boolean isEpsilon() {
        return false; // Non-terminals cannot be epsilon
    }

    @Override
    public String prettyPrint() {
        return "NonTerminal: " + name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonTerminal)) return false;
        NonTerminal that = (NonTerminal) o;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}