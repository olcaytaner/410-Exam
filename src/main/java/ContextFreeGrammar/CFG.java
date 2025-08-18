package ContextFreeGrammar;

import common.Automaton;
import common.Symbol;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a Context-Free Grammar (CFG) with variables, terminals, productions, and a start symbol.
 * This class provides functionality to validate the grammar, manipulate productions, and format output.
 *
 * @author yenennn
 * @version 1.0
 */
public class CFG extends Automaton {
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
     * Default constructor for Automaton compatibility.
     */
    public CFG() {
        super(MachineType.CFG);
        this.variables = new HashSet<>();
        this.terminals = new HashSet<>();
        this.productions = new ArrayList<>();
    }

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
        super(MachineType.CFG);
        this.variables = variables;
        this.terminals = terminals;
        this.productions = new ArrayList<>(productions);
        this.startSymbol = startSymbol;
    }

    // Getters and setters for Automaton compatibility
    public Set<NonTerminal> getVariables() {
        return variables;
    }

    public void setVariables(Set<NonTerminal> variables) {
        this.variables = variables;
    }

    public Set<Terminal> getTerminals() {
        return terminals;
    }

    public void setTerminals(Set<Terminal> terminals) {
        this.terminals = terminals;
    }

    public List<Production> getProductions() {
        return productions;
    }

    public void setProductions(List<Production> productions) {
        this.productions = productions;
    }

    public NonTerminal getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(NonTerminal startSymbol) {
        this.startSymbol = startSymbol;
    }

    // Automaton abstract method implementations
    @Override
    public ParseResult parse(String inputText) {
        List<ValidationMessage> messages = new ArrayList<>();
        try {
            CFGParser parser = new CFGParser();
            CFG parsedCFG = parser.parse(inputText);

            this.variables = parsedCFG.getVariables();
            this.terminals = parsedCFG.getTerminals();
            this.productions = parsedCFG.getProductions();
            this.startSymbol = parsedCFG.getStartSymbol();

            return new ParseResult(true, messages, this);
        } catch (Exception e) {
            messages.add(new ValidationMessage(e.getMessage(), 0, ValidationMessage.ValidationMessageType.ERROR));
            return new ParseResult(false, messages, null);
        }
    }

    @Override
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> messages = new ArrayList<>();
        String trace = "CFG execution trace for: " + inputText + "\n";
        boolean accepted = false;

        // Basic string derivation check (simplified implementation)
        try {
            // This is a placeholder for actual parsing logic
            // You can implement CYK algorithm or recursive descent parsing here
            trace += "Checking if string can be derived from grammar...\n";
            trace += "Start symbol: " + (startSymbol != null ? startSymbol.getName() : "undefined") + "\n";

            // For now, just check if the string contains only terminal symbols
            if (inputText != null && !inputText.isEmpty()) {
                Set<String> terminalNames = terminals.stream()
                        .map(Terminal::getName)
                        .collect(Collectors.toSet());

                boolean allTerminals = inputText.chars()
                        .mapToObj(c -> String.valueOf((char) c))
                        .allMatch(terminalNames::contains);

                accepted = allTerminals && startSymbol != null;
                trace += "String contains only terminals: " + allTerminals + "\n";
                trace += "Result: " + (accepted ? "ACCEPTED" : "REJECTED") + "\n";
            }
        } catch (Exception e) {
            messages.add(new ValidationMessage("Execution error: " + e.getMessage(), 0, ValidationMessage.ValidationMessageType.ERROR));
            trace += "Error during execution: " + e.getMessage() + "\n";
        }

        return new ExecutionResult(accepted, messages, trace);
    }

    @Override
    public List<ValidationMessage> validate() {
        List<ValidationMessage> messages = new ArrayList<>();

        // Check if the required components exist
        if (variables.isEmpty()) {
            messages.add(new ValidationMessage("No variables defined in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (terminals.isEmpty()) {
            messages.add(new ValidationMessage("No terminals defined in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (startSymbol == null) {
            messages.add(new ValidationMessage("No start symbol defined in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        // Check if the start symbol is in the set of variables
        if (startSymbol != null && !variables.contains(startSymbol)) {
            messages.add(new ValidationMessage("Start symbol " + startSymbol.getName() + " is not in the set of variables", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        // Check if all non-terminals in productions are in the variables set
        for (Production p : productions) {
            if (!variables.contains(p.getLeft())) {
                messages.add(new ValidationMessage("Production uses undefined variable: " + p.getLeft().getName(), 0, ValidationMessage.ValidationMessageType.ERROR));
            }

            for (Symbol symbol : p.getRight()) {
                if (symbol instanceof NonTerminal && !variables.contains(symbol)) {
                    messages.add(new ValidationMessage("Production uses undefined variable: " + ((NonTerminal)symbol).getName(), 0, ValidationMessage.ValidationMessageType.ERROR));
                } else if (symbol instanceof Terminal && !terminals.contains(symbol) && !symbol.getName().equals("_")) {
                    messages.add(new ValidationMessage("Production uses undefined terminal: " + ((Terminal)symbol).getName(), 0, ValidationMessage.ValidationMessageType.WARNING));
                }
            }
        }

        return messages;
    }

    @Override
    public String toDotCode(String inputText) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph CFG {\n");
        dot.append("    rankdir=TB;\n");
        dot.append("    node [shape=box, style=rounded];\n");
        dot.append("    edge [arrowhead=vee];\n\n");

        // Add start symbol with special styling
        if (startSymbol != null) {
            dot.append("    start [label=\"Start\", shape=circle, style=filled, fillcolor=lightgreen];\n");
            dot.append("    \"").append(startSymbol.getName()).append("\" [style=filled, fillcolor=lightblue];\n");
            dot.append("    start -> \"").append(startSymbol.getName()).append("\";\n\n");
        }

        // Add production nodes and edges
        int prodId = 0;
        for (Production production : productions) {
            String prodNode = "prod" + prodId++;
            String rightSide = production.getRight().stream()
                    .map(Symbol::getName)
                    .collect(Collectors.joining(" "));

            if (rightSide.isEmpty()) {
                rightSide = "ε";
            }

            dot.append("    ").append(prodNode).append(" [label=\"").append(rightSide).append("\", shape=ellipse, style=filled, fillcolor=lightyellow];\n");
            dot.append("    \"").append(production.getLeft().getName()).append("\" -> ").append(prodNode).append(";\n");

            // Connect to symbols on the right side
            for (Symbol symbol : production.getRight()) {
                if (symbol instanceof NonTerminal) {
                    dot.append("    ").append(prodNode).append(" -> \"").append(symbol.getName()).append("\" [style=dashed];\n");
                }
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    // Your existing methods preserved exactly as they were

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
                        !terminals.contains(symbol) && !symbol.getName().equals("_")) {
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
                    sb.append(rightSide.isEmpty() ? "_" : rightSide);
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
        sb.append("Start = ").append(startSymbol != null ? startSymbol.getName() : "undefined")
                .append("\n\n");

        // Add each production on its own line
        for (Production p : productions) {
            sb.append(p.getLeft().getName()).append(" -> ");

            // Append all symbols on the right side
            String rightSide = p.getRight().stream()
                    .map(Symbol::getName)
                    .collect(Collectors.joining(" "));

            // Use "eps" for empty string
            sb.append(rightSide.isEmpty() ? "_" : rightSide);
            sb.append("\n");
        }

        return sb.toString();
    }
}