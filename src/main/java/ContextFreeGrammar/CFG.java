package ContextFreeGrammar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * Represents a Context-Free Grammar (CFG) with variables, terminals, productions, and a start symbol.
 * This class provides functionality to validate the grammar, manipulate productions, and format output.
 *
 * @author yenennn
 * @version 1.0
 */
public class CFG {
    /** Pattern for validating variable names (must start with uppercase letter) */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[A-Z][A-Za-z0-9]*$");

    /** Pattern for validating terminal names (must be lowercase alphanumeric) */
    private static final Pattern TERMINAL_PATTERN = Pattern.compile("^[a-z0-9]+$");

    /** Set of non-terminal symbols (variables) in the grammar */
    private Set<NonTerminal> variables;

    /** Set of terminal symbols in the grammar */
    private Set<Terminal> terminals;

    /** List of production rules in the grammar */
    private List<Production> productions;

    /** The start symbol of the grammar */
    private NonTerminal startSymbol;

    /**
     * Constructs a new Context-Free Grammar with the specified components.
     *
     * @param variables the set of non-terminal symbols
     * @param terminals the set of terminal symbols
     * @param productions the list of production rules
     * @param startSymbol the start symbol of the grammar
     */
    public CFG(Set<NonTerminal> variables,
               Set<Terminal> terminals,
               List<Production> productions,
               NonTerminal startSymbol) {
        this.variables = variables;
        this.terminals = terminals;
        this.productions = new ArrayList<>(productions);
        this.startSymbol = startSymbol;
    }

    /**
     * Adds a new production rule to the grammar.
     *
     * @param p the production rule to add
     * @throws IllegalArgumentException if the left-hand side of the production is not in the variables set
     */
    public void addProduction(Production p) {
        if (!variables.contains(p.getLeft())) {
            throw new IllegalArgumentException("Left-hand side not in variables: " + p.getLeft().getName());
        }
        productions.add(p);
    }

    /**
     * Removes a production rule from the grammar.
     *
     * @param p the production rule to remove
     */
    public void removeProduction(Production p) {
        productions.remove(p);
    }

    /**
     * Retrieves all production rules that have the specified variable as their left-hand side.
     *
     * @param v the non-terminal variable to find productions for
     * @return a list of productions with the specified variable on the left-hand side
     */
    public List<Production> getProductionsFor(NonTerminal v) {
        return productions.stream()
                .filter(p -> p.getLeft().equals(v))
                .collect(Collectors.toList());
    }

    /**
     * Validates the grammar by checking various structural requirements.
     * This includes verifying that:
     * <ul>
     * <li>Variables, terminals, and start symbol are defined</li>
     * <li>Start symbol is in the variables set</li>
     * <li>All symbols in productions are properly defined</li>
     * <li>Variables have productions and are reachable from start symbol</li>
     * </ul>
     *
     * @return true if the grammar is valid, false otherwise
     */
    public boolean validateGrammar() {
        // Check if the required components exist
        if (variables.isEmpty()) {
            System.err.println("Error: No variables defined in the grammar.");
            return false;
        }

        if (terminals.isEmpty()) {
            System.err.println("Error: No terminals defined in the grammar.");
            return false;
        }

        if (startSymbol == null) {
            System.err.println("Error: No start symbol defined in the grammar.");
            return false;
        }

        // Check if the start symbol is in the set of variables
        if (!variables.contains(startSymbol)) {
            System.err.println("Error: Start symbol " + startSymbol.getName() +
                    " is not in the set of variables.");
            return false;
        }

        // Check if all non-terminals in productions are in the variables set
        for (Production p : productions) {
            if (!variables.contains(p.getLeft())) {
                System.err.println("Error: Production uses undefined variable: " +
                        p.getLeft().getName());
                return false;
            }

            // Check if all symbols on the right side are either variables or terminals
            for (Symbol symbol : p.getRight()) {
                if (symbol instanceof NonTerminal && !variables.contains(symbol)) {
                    System.err.println("Error: Production uses undefined variable: " +
                            ((NonTerminal)symbol).getName());
                    return false;
                } else if (symbol instanceof Terminal &&
                        !terminals.contains(symbol) && !symbol.getName().equals("eps")) {
                    System.err.println("Error: Production uses undefined terminal: " +
                            ((Terminal)symbol).getName());
                    return false;
                }
            }
        }

        // Validate each variable has at least one production
        Set<NonTerminal> variablesWithProductions = productions.stream()
                .map(Production::getLeft)
                .collect(Collectors.toSet());

        Set<NonTerminal> unusedVariables = new HashSet<>(variables);
        unusedVariables.removeAll(variablesWithProductions);

        if (!unusedVariables.isEmpty()) {
            System.err.println("Warning: The following variables have no productions: " +
                    unusedVariables.stream()
                            .map(NonTerminal::getName)
                            .collect(Collectors.joining(", ")));
            // Not returning false as this is just a warning
        }

        // Check reachability from the start symbol
        Set<NonTerminal> reachable = new HashSet<>();
        reachable.add(startSymbol);

        boolean added;
        do {
            added = false;
            for (Production p : productions) {
                if (reachable.contains(p.getLeft())) {
                    for (Symbol s : p.getRight()) {
                        if (s instanceof NonTerminal && !reachable.contains(s)) {
                            reachable.add((NonTerminal) s);
                            added = true;
                        }
                    }
                }
            }
        } while (added);

        Set<NonTerminal> unreachableVariables = new HashSet<>(variables);
        unreachableVariables.removeAll(reachable);

        if (!unreachableVariables.isEmpty()) {
            System.err.println("Warning: The following variables are unreachable from the start symbol: " +
                    unreachableVariables.stream()
                            .map(NonTerminal::getName)
                            .collect(Collectors.joining(", ")));
            // Not returning false as this is just a warning
        }

        return true;
    }

    /**
     * Converts the grammar to Chomsky Normal Form (CNF).
     * This method eliminates epsilon productions, unit productions, and ensures
     * all productions are in the form A -> BC or A -> a.
     *
     * @return a new CFG in Chomsky Normal Form
     * @throws UnsupportedOperationException as this method is not yet implemented
     */
    public CFG toChomskyNormalForm() {
        // TODO: Implement CNF conversion (elimınate epsilon, unit productions, etc.)
        throw new UnsupportedOperationException("toChomskyNormalForm not implemented yet");
    }

    /**
     * Prints a formatted representation of the grammar to standard output.
     * The output includes variables, terminals, start symbol, and productions
     * grouped by their left-hand side with alternatives separated by '|'.
     */
    public void prettyPrint() {
        // Print variables
        System.out.println("Variables = " + variables.stream()
                .map(NonTerminal::getName)
                .collect(Collectors.joining(" ")));

        // Print terminals
        System.out.println("Terminals = " + terminals.stream()
                .map(Terminal::getName)
                .collect(Collectors.joining(" ")));

        // Print start symbol
        System.out.println("Start = " + startSymbol.getName());
        System.out.println();

        // Print productions, grouped by left-hand side
        Map<NonTerminal, List<Production>> productionMap = productions.stream()
                .collect(Collectors.groupingBy(Production::getLeft));

        for (NonTerminal variable : variables) {
            List<Production> prods = productionMap.getOrDefault(variable, new ArrayList<>());
            if (!prods.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(variable.getName()).append(" -> ");

                // Combine productions with same left-hand side
                for (int i = 0; i < prods.size(); i++) {
                    Production p = prods.get(i);
                    if (i > 0) {
                        sb.append(" | ");
                    }

                    // Append all symbols on the right side
                    String rightSide = p.getRight().stream()
                            .map(Symbol::getName)
                            .collect(Collectors.joining(" "));

                    // Use "eps" for empty string
                    sb.append(rightSide.isEmpty() ? "eps" : rightSide);
                }

                System.out.println(sb);
            }
        }
    }

    /**
     * Returns a string representation of the grammar.
     * The format includes variables, terminals, start symbol, and all productions
     * listed separately (not grouped by alternatives).
     *
     * @return a formatted string representation of the grammar
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Add variables
        sb.append("Variables = ").append(variables.stream()
                        .map(NonTerminal::getName)
                        .collect(Collectors.joining(" ")))
                .append("\n");

        // Add terminals
        sb.append("Terminals = ").append(terminals.stream()
                        .map(Terminal::getName)
                        .collect(Collectors.joining(" ")))
                .append("\n");

        // Add start symbol
        sb.append("Start = ").append(startSymbol.getName())
                .append("\n\n");

        // Add each production on its own line
        for (Production p : productions) {
            sb.append(p.getLeft().getName()).append(" -> ");

            // Append all symbols on the right side
            String rightSide = p.getRight().stream()
                    .map(Symbol::getName)
                    .collect(Collectors.joining(" "));

            // Use "eps" for empty string
            sb.append(rightSide.isEmpty() ? "eps" : rightSide);
            sb.append("\n");
        }

        return sb.toString();
    }
}