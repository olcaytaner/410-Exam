package ContextFreeGrammar;

import java.util.List;
import java.util.stream.Collectors;

public class Production {
    private NonTerminal left;
    private List<Symbol> right;

    public Production(NonTerminal left, List<Symbol> right) {
        this.left = left;
        this.right = right;
    }

    public NonTerminal getLeft() {
        return left;
    }

    public List<Symbol> getRight() {
        return right;
    }

    @Override
    public String toString() {
        String rhs = right.stream()
                .map(Symbol::getName)
                .collect(Collectors.joining(" "));
        return left.getName() + " -> " + rhs;
    }
}

