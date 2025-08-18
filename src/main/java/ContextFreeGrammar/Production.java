package ContextFreeGrammar;

import common.Symbol;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a production rule in a Context-Free Grammar.
 * A production consists of a left-hand side (a non-terminal) and a right-hand side
 * (a list of symbols that can replace the left-hand side).
 *
 * @author yenennn
 * @version 1.0
 */
public class Production {
    /** The left-hand side of the production (always a non-terminal) */
    private NonTerminal left;

    /** The right-hand side of the production (list of symbols) */
    private List<Symbol> right;

    /**
     * Constructs a new production rule.
     *
     * @param left the left-hand side non-terminal
     * @param right the right-hand side list of symbols
     */
    public Production(NonTerminal left, List<Symbol> right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left-hand side of this production.
     *
     * @return the non-terminal on the left-hand side
     */
    public NonTerminal getLeft() {
        return left;
    }

    /**
     * Returns the right-hand side of this production.
     *
     * @return the list of symbols on the right-hand side
     */
    public List<Symbol> getRight() {
        return right;
    }

    /**
     * Returns a string representation of this production in the format "A -> B C".
     *
     * @return a formatted string representation of the production
     */
    @Override
    public String toString() {
        String rhs = right.stream()
                .map(Symbol::getName)
                .collect(Collectors.joining(" "));
        return left.getName() + " -> " + rhs;
    }
}