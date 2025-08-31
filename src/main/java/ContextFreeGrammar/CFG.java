package ContextFreeGrammar;

import common.Automaton;
import common.Symbol;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a Context-Free Grammar (CFG) with variables, terminals, productions, and a start symbol.
 * This class provides functionality to validate the grammar, manipulate productions, format output,
 * and parse CFG definition files.
 *
 * @author yenennn
 * @version 1.0
 */
public class CFG extends Automaton {
    /** Pattern for validating variable names (must start with uppercase letter) */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[A-Z][A-Za-z0-9]*$");

    /** Pattern for validating terminal names (must be lowercase alphanumeric) */
    private static final Pattern TERMINAL_PATTERN = Pattern.compile("^[a-z0-9]+$");

    /** Maximum number of lines allowed in a grammar file to prevent infinite loops */
    private static final int MAX_LINES = 200;

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

    // Parser functionality integrated from CFGParser
    /**
     * Parses a Context-Free Grammar from a file.
     * The file format should contain:
     * - Variables = [list of uppercase variables]
     * - Terminals = [list of lowercase terminals]
     * - Start = [start symbol]
     * - Production rules in the format: A -> B C | D
     *
     * @param filePath the path to the grammar file to parse
     * @return a CFG object representing the parsed grammar
     * @throws IOException if there's an error reading the file
     * @throws GrammarParseException if the grammar file has invalid syntax or structure
     */
    public static CFG parseFromFile(String filePath) throws IOException {
        Set<NonTerminal> variables = new HashSet<>();
        Set<Terminal> terminals = new HashSet<>();
        NonTerminal startSymbol = null;
        List<Production> productions = new ArrayList<>();
        int lineNumber = 0;
        boolean variablesHeaderFound = false;
        boolean terminalsHeaderFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Check if we've exceeded the maximum line threshold
                if (lineNumber > MAX_LINES) {
                    throw new GrammarParseException("File exceeds maximum line limit (" + MAX_LINES +
                            " lines). Check for malformed production rules or infinite loops.");
                }

                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    if (line.toLowerCase().startsWith("variables =")) {
                        variablesHeaderFound = true;
                        String[] varNames = line.substring("Variables =".length()).trim().split("\\s+");
                        if (varNames.length == 1 && varNames[0].isEmpty()) {
                            throw new GrammarParseException("No variables specified");
                        }
                        for (String name : varNames) {
                            if (!name.isEmpty()) {
                                if (!name.equals(name.toUpperCase())) {
                                    throw new GrammarParseException("Variable '" + name + "' must be uppercase");
                                }
                                variables.add(new NonTerminal(name));
                            }
                        }
                    } else if (line.toLowerCase().startsWith("terminals =")) {
                        terminalsHeaderFound = true;
                        String[] termNames = line.substring("Terminals =".length()).trim().split("\\s+");
                        if (termNames.length == 1 && termNames[0].isEmpty()) {
                            throw new GrammarParseException("No terminals specified");
                        }
                        for (String name : termNames) {
                            if (!name.isEmpty()) {
                                if (!name.equals(name.toLowerCase())) {
                                    throw new GrammarParseException("Terminal '" + name + "' must be lowercase");
                                }
                                terminals.add(new Terminal(name));
                            }
                        }
                    } else if (line.toLowerCase().startsWith("start =")) {
                        String startName = line.substring("Start =".length()).trim();
                        if (startName.isEmpty()) {
                            throw new GrammarParseException("No start symbol specified");
                        }
                        startSymbol = new NonTerminal(startName);
                    } else if (line.contains("->")) {
                        parseProduction(line, variables, terminals, productions);
                    } else if (line.contains("->") == false && line.contains("-") && line.contains(">")) {
                        throw new GrammarParseException("Arrow typo detected at line " + lineNumber + ": Did you mean '->'?");
                    } else {
                        throw new GrammarParseException("Unrecognized line format");
                    }
                } catch (GrammarParseException e) {
                    throw new GrammarParseException("Error at line " + lineNumber + ": " + e.getMessage() + "\nLine content: " + line);
                }
            }
        }

        // Check for missing headers
        if (!variablesHeaderFound) {
            throw new GrammarParseException("Variables missing");
        }
        if (!terminalsHeaderFound) {
            throw new GrammarParseException("Terminals missing");
        }

        // Validate required grammar components
        if (variables.isEmpty()) {
            throw new GrammarParseException("No variables defined in the grammar");
        }
        if (terminals.isEmpty()) {
            throw new GrammarParseException("No terminals defined in the grammar");
        }
        if (startSymbol == null) {
            throw new GrammarParseException("No start symbol defined in the grammar");
        }
        if (productions.isEmpty()) {
            throw new GrammarParseException("No productions defined in the grammar");
        }

        // Validate start symbol exists in variables
        boolean startExists = false;
        for (NonTerminal var : variables) {
            if (var.getName().equals(startSymbol.getName())) {
                startExists = true;
                startSymbol = var; // Use the actual object from the set
                break;
            }
        }

        if (!startExists) {
            throw new GrammarParseException("Start symbol '" + startSymbol.getName() + "' is not defined in Variables");
        }

        return new CFG(variables, terminals, productions, startSymbol);
    }

    /**
     * Parses a Context-Free Grammar from a string input.
     *
     * @param inputText the string containing the grammar definition
     * @return a CFG object representing the parsed grammar
     * @throws GrammarParseException if the grammar text has invalid syntax or structure
     */
    public static CFG parseFromString(String inputText) {
        Set<NonTerminal> variables = new HashSet<>();
        Set<Terminal> terminals = new HashSet<>();
        NonTerminal startSymbol = null;
        List<Production> productions = new ArrayList<>();
        int lineNumber = 0;
        boolean variablesHeaderFound = false;
        boolean terminalsHeaderFound = false;

        try (BufferedReader reader = new BufferedReader(new StringReader(inputText))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Check if we've exceeded the maximum line threshold
                if (lineNumber > MAX_LINES) {
                    throw new GrammarParseException("Input exceeds maximum line limit (" + MAX_LINES +
                            " lines). Check for malformed production rules or infinite loops.");
                }

                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    if (line.toLowerCase().startsWith("variables =")) {
                        variablesHeaderFound = true;
                        String[] varNames = line.substring("Variables =".length()).trim().split("\\s+");
                        if (varNames.length == 1 && varNames[0].isEmpty()) {
                            throw new GrammarParseException("No variables specified");
                        }
                        for (String name : varNames) {
                            if (!name.isEmpty()) {
                                if (!name.equals(name.toUpperCase())) {
                                    throw new GrammarParseException("Variable '" + name + "' must be uppercase");
                                }
                                variables.add(new NonTerminal(name));
                            }
                        }
                    } else if (line.toLowerCase().startsWith("terminals =")) {
                        terminalsHeaderFound = true;
                        String[] termNames = line.substring("Terminals =".length()).trim().split("\\s+");
                        if (termNames.length == 1 && termNames[0].isEmpty()) {
                            throw new GrammarParseException("No terminals specified");
                        }
                        for (String name : termNames) {
                            if (!name.isEmpty()) {
                                if (!name.equals(name.toLowerCase())) {
                                    throw new GrammarParseException("Terminal '" + name + "' must be lowercase");
                                }
                                terminals.add(new Terminal(name));
                            }
                        }
                    } else if (line.toLowerCase().startsWith("start =")) {
                        String startName = line.substring("Start =".length()).trim();
                        if (startName.isEmpty()) {
                            throw new GrammarParseException("No start symbol specified");
                        }
                        startSymbol = new NonTerminal(startName);
                    } else if (line.contains("->")) {
                        parseProduction(line, variables, terminals, productions);
                    } else if (line.contains("->") == false && line.contains("-") && line.contains(">")) {
                        throw new GrammarParseException("Arrow typo detected at line " + lineNumber + ": Did you mean '->'?");
                    } else {
                        throw new GrammarParseException("Unrecognized line format");
                    }
                } catch (GrammarParseException e) {
                    throw new GrammarParseException("Error at line " + lineNumber + ": " + e.getMessage() + "\nLine content: " + line);
                }
            }
        } catch (IOException e) {
            throw new GrammarParseException("Error reading input: " + e.getMessage());
        }

        // Check for missing headers
        if (!variablesHeaderFound) {
            throw new GrammarParseException("Variables missing");
        }
        if (!terminalsHeaderFound) {
            throw new GrammarParseException("Terminals missing");
        }

        // Validate required grammar components
        if (variables.isEmpty()) {
            throw new GrammarParseException("No variables defined in the grammar");
        }
        if (terminals.isEmpty()) {
            throw new GrammarParseException("No terminals defined in the grammar");
        }
        if (startSymbol == null) {
            throw new GrammarParseException("No start symbol defined in the grammar");
        }
        if (productions.isEmpty()) {
            throw new GrammarParseException("No productions defined in the grammar");
        }

        // Validate start symbol exists in variables
        boolean startExists = false;
        for (NonTerminal var : variables) {
            if (var.getName().equals(startSymbol.getName())) {
                startExists = true;
                startSymbol = var; // Use the actual object from the set
                break;
            }
        }

        if (!startExists) {
            throw new GrammarParseException("Start symbol '" + startSymbol.getName() + "' is not defined in Variables");
        }

        return new CFG(variables, terminals, productions, startSymbol);
    }

    /**
     * Parses a single production rule line and adds the resulting productions to the list.
     * Handles multiple alternatives separated by the '|' symbol.
     *
     * @param line the production rule line to parse
     * @param variables the set of defined variables
     * @param terminals the set of defined terminals
     * @param productions the list to add parsed productions to
     * @throws GrammarParseException if the production format is invalid
     */
    private static void parseProduction(String line, Set<NonTerminal> variables,
                                        Set<Terminal> terminals, List<Production> productions) {
        String[] parts = line.split("->");
        if (parts.length != 2) {
            throw new GrammarParseException("Invalid production format, missing '->' operator or has too many");
        }

        String leftSide = parts[0].trim();
        if (leftSide.isEmpty()) {
            throw new GrammarParseException("Left side of production is empty");
        }

        NonTerminal left = findNonTerminal(leftSide, variables);
        if (left == null) {
            throw new GrammarParseException("Undefined variable '" + leftSide + "' in production");
        }

        String[] alternatives = parts[1].trim().split("\\|");
        if (alternatives.length == 0 || (alternatives.length == 1 && alternatives[0].trim().isEmpty())) {
            throw new GrammarParseException("Right side of production is empty");
        }

        for (String alternative : alternatives) {
            alternative = alternative.trim();
            List<Symbol> rightSide = new ArrayList<>();

            if (!alternative.isEmpty()) {
                String[] symbols = alternative.split("\\s+");
                for (String symbol : symbols) {
                    Symbol s = findSymbol(symbol, variables, terminals);
                    if (s != null) {
                        rightSide.add(s);
                    } else {
                        throw new GrammarParseException("Undefined symbol '" + symbol + "' in production");
                    }
                }
            }

            productions.add(new Production(left, rightSide));
        }
    }

    /**
     * Finds a non-terminal with the specified name in the variables set.
     *
     * @param name the name of the non-terminal to find
     * @param variables the set of variables to search in
     * @return the NonTerminal object if found, null otherwise
     */
    private static NonTerminal findNonTerminal(String name, Set<NonTerminal> variables) {
        for (NonTerminal var : variables) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    /**
     * Finds a symbol (terminal or non-terminal) with the specified name.
     * Also handles the special case of epsilon ("_").
     *
     * @param name the name of the symbol to find
     * @param variables the set of variables to search in
     * @param terminals the set of terminals to search in
     * @return the Symbol object if found, null otherwise
     */
    private static Symbol findSymbol(String name, Set<NonTerminal> variables, Set<Terminal> terminals) {
        // Check if it's a variable
        NonTerminal var = findNonTerminal(name, variables);
        if (var != null) {
            return var;
        }

        // Check if it's a terminal
        for (Terminal term : terminals) {
            if (term.getName().equals(name)) {
                return term;
            }
        }

        // Special case for epsilon
        if (name.equals("_")) {
            return new Terminal("_");
        }

        return null;
    }

    /**
     * Converts the grammar to Chomsky Normal Form (CNF).
     * This method performs the following steps:
     * 1. Eliminate epsilon productions
     * 2. Eliminate unit productions
     * 3. Convert to CNF format (A -> BC or A -> a)
     * 4. Handle mixed productions and long productions
     *
     * @return a new CFG in Chomsky Normal Form
     */
    public CFG toChomskyNormalForm() {
        // Create copies to avoid modifying the original grammar
        Set<NonTerminal> newVariables = new HashSet<>(variables);
        Set<Terminal> newTerminals = new HashSet<>(terminals);
        List<Production> newProductions = new ArrayList<>(productions);
        NonTerminal newStartSymbol = startSymbol;

        // Step 1: Add new start symbol to handle epsilon in original start symbol
        NonTerminal originalStart = startSymbol;
        NonTerminal newStart = new NonTerminal("S'");
        while (newVariables.contains(newStart)) {
            newStart = new NonTerminal(newStart.getName() + "'");
        }

        newVariables.add(newStart);
        newProductions.add(new Production(newStart, Arrays.asList(originalStart)));
        newStartSymbol = newStart;

        // Step 2: Eliminate epsilon productions
        newProductions = eliminateEpsilonProductions(newProductions, newVariables);

        // Step 3: Eliminate unit productions
        newProductions = eliminateUnitProductions(newProductions, newVariables);

        // Step 4: Convert long productions and mixed productions to CNF
        newProductions = convertToCNFFormat(newProductions, newVariables, newTerminals);

        return new CFG(newVariables, newTerminals, newProductions, newStartSymbol);
    }

    /**
     * Step 1: Eliminate epsilon (empty) productions from the grammar.
     * This method finds all nullable variables and creates new productions
     * without the nullable variables.
     */
    private List<Production> eliminateEpsilonProductions(List<Production> productions, Set<NonTerminal> variables) {
        // Find all nullable variables (variables that can derive epsilon)
        Set<NonTerminal> nullable = findNullableVariables(productions);

        List<Production> newProductions = new ArrayList<>();

        for (Production p : productions) {
            // Skip epsilon productions
            if (p.getRight().isEmpty()) {
                continue;
            }

            // Generate all combinations by including/excluding nullable variables
            List<List<Symbol>> combinations = generateCombinations(p.getRight(), nullable);

            for (List<Symbol> combination : combinations) {
                if (!combination.isEmpty()) { // Don't add empty productions
                    newProductions.add(new Production(p.getLeft(), combination));
                }
            }
        }

        // Remove duplicate productions
        return removeDuplicateProductions(newProductions);
    }

    /**
     * Finds all variables that can derive epsilon (empty string).
     */
    private Set<NonTerminal> findNullableVariables(List<Production> productions) {
        Set<NonTerminal> nullable = new HashSet<>();
        boolean changed = true;

        while (changed) {
            changed = false;
            for (Production p : productions) {
                if (!nullable.contains(p.getLeft())) {
                    // Check if right side is empty (direct epsilon production)
                    if (p.getRight().isEmpty()) {
                        nullable.add(p.getLeft());
                        changed = true;
                    }
                    // Check if all symbols on right side are nullable
                    else if (p.getRight().stream().allMatch(symbol ->
                            symbol instanceof NonTerminal && nullable.contains(symbol))) {
                        nullable.add(p.getLeft());
                        changed = true;
                    }
                }
            }
        }

        return nullable;
    }

    /**
     * Generates all possible combinations by including/excluding nullable variables.
     */
    private List<List<Symbol>> generateCombinations(List<Symbol> symbols, Set<NonTerminal> nullable) {
        List<List<Symbol>> result = new ArrayList<>();
        generateCombinationsHelper(symbols, nullable, 0, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinationsHelper(List<Symbol> symbols, Set<NonTerminal> nullable,
                                            int index, List<Symbol> current, List<List<Symbol>> result) {
        if (index == symbols.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        Symbol symbol = symbols.get(index);

        // Always include non-nullable symbols
        if (!(symbol instanceof NonTerminal) || !nullable.contains(symbol)) {
            current.add(symbol);
            generateCombinationsHelper(symbols, nullable, index + 1, current, result);
            current.remove(current.size() - 1);
        } else {
            // For nullable variables, try both including and excluding
            // Include the variable
            current.add(symbol);
            generateCombinationsHelper(symbols, nullable, index + 1, current, result);
            current.remove(current.size() - 1);

            // Exclude the variable
            generateCombinationsHelper(symbols, nullable, index + 1, current, result);
        }
    }

    /**
     * Step 2: Eliminate unit productions (A -> B where B is a single non-terminal).
     */
    private List<Production> eliminateUnitProductions(List<Production> productions, Set<NonTerminal> variables) {
        List<Production> newProductions = new ArrayList<>();

        // Separate unit and non-unit productions
        List<Production> unitProductions = new ArrayList<>();
        List<Production> nonUnitProductions = new ArrayList<>();

        for (Production p : productions) {
            if (p.getRight().size() == 1 && p.getRight().get(0) instanceof NonTerminal) {
                unitProductions.add(p);
            } else {
                nonUnitProductions.add(p);
            }
        }

        // Build unit production chains
        Map<NonTerminal, Set<NonTerminal>> unitChains = buildUnitChains(unitProductions, variables);

        // For each variable, add all non-unit productions reachable through unit chains
        for (NonTerminal var : variables) {
            Set<NonTerminal> reachable = unitChains.getOrDefault(var, new HashSet<>());
            reachable.add(var); // Include itself

            for (NonTerminal reachableVar : reachable) {
                for (Production p : nonUnitProductions) {
                    if (p.getLeft().equals(reachableVar)) {
                        newProductions.add(new Production(var, p.getRight()));
                    }
                }
            }
        }

        return removeDuplicateProductions(newProductions);
    }

    /**
     * Builds chains of unit productions (A -> B -> C -> ...).
     */
    private Map<NonTerminal, Set<NonTerminal>> buildUnitChains(List<Production> unitProductions, Set<NonTerminal> variables) {
        Map<NonTerminal, Set<NonTerminal>> chains = new HashMap<>();

        // Initialize
        for (NonTerminal var : variables) {
            chains.put(var, new HashSet<>());
        }

        // Add direct unit production relationships
        for (Production p : unitProductions) {
            NonTerminal from = p.getLeft();
            NonTerminal to = (NonTerminal) p.getRight().get(0);
            chains.get(from).add(to);
        }

        // Compute transitive closure using Floyd-Warshall
        for (NonTerminal k : variables) {
            for (NonTerminal i : variables) {
                for (NonTerminal j : variables) {
                    if (chains.get(i).contains(k) && chains.get(k).contains(j)) {
                        chains.get(i).add(j);
                    }
                }
            }
        }

        return chains;
    }

    /**
     * Step 3: Convert remaining productions to CNF format.
     * This handles:
     * - Mixed productions (A -> aB, A -> Ba, etc.)
     * - Long productions (A -> BCD, A -> BCDE, etc.)
     * - Productions with multiple terminals
     */
    private List<Production> convertToCNFFormat(List<Production> productions, Set<NonTerminal> variables, Set<Terminal> terminals) {
        List<Production> newProductions = new ArrayList<>();
        Map<String, NonTerminal> terminalVariables = new HashMap<>();
        int newVarCounter = 0;

        for (Production p : productions) {
            List<Symbol> rightSide = p.getRight();

            // Case 1: A -> a (already in CNF)
            if (rightSide.size() == 1 && rightSide.get(0) instanceof Terminal) {
                newProductions.add(p);
                continue;
            }

            // Case 2: A -> BC where B and C are non-terminals (already in CNF)
            if (rightSide.size() == 2 &&
                    rightSide.get(0) instanceof NonTerminal &&
                    rightSide.get(1) instanceof NonTerminal) {
                newProductions.add(p);
                continue;
            }

            // Case 3: Need to convert to CNF
            List<Symbol> processedRight = new ArrayList<>();

            // Replace terminals with new variables (except for single terminal productions)
            for (Symbol symbol : rightSide) {
                if (symbol instanceof Terminal && rightSide.size() > 1) {
                    NonTerminal termVar = getOrCreateTerminalVariable(symbol.getName(), terminalVariables, variables, newVarCounter++);
                    processedRight.add(termVar);

                    // Add production: NewVar -> terminal
                    newProductions.add(new Production(termVar, Arrays.asList(symbol)));
                } else {
                    processedRight.add(symbol);
                }
            }

            // Handle long productions (more than 2 symbols)
            if (processedRight.size() > 2) {
                newProductions.addAll(breakLongProduction(p.getLeft(), processedRight, variables, newVarCounter));
                newVarCounter += processedRight.size() - 2;
            } else {
                newProductions.add(new Production(p.getLeft(), processedRight));
            }
        }

        return removeDuplicateProductions(newProductions);
    }

    private static boolean nameExists(Set<NonTerminal> vars, String name) {
        for (NonTerminal v : vars) {
            if (v.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Gets or creates a new variable to represent a terminal.
     * NOTE: 'counter' here is optional; increments don't escape to the caller.
     */
    private NonTerminal getOrCreateTerminalVariable(String terminalName,
                                                    Map<String, NonTerminal> terminalVariables,
                                                    Set<NonTerminal> variables,
                                                    int counter) {

        NonTerminal existing = terminalVariables.get(terminalName);
        if (existing != null) return existing;

        String base = "T_" + terminalName;
        String newVarName = base;
        int suffix = Math.max(1, counter); // seed from counter if you want
        while (nameExists(variables, newVarName)) {
            newVarName = base + "_" + (suffix++);
        }

        NonTerminal newVar = new NonTerminal(newVarName);
        terminalVariables.put(terminalName, newVar);
        variables.add(newVar);
        return newVar;
    }

    /**
     * Breaks a long production (A -> BCDE) into binary productions.
     */
    private List<Production> breakLongProduction(NonTerminal left, List<Symbol> rightSide,
                                                 Set<NonTerminal> variables, int startCounter) {

        List<Production> result = new ArrayList<>();
        if (rightSide.size() <= 2) {
            result.add(new Production(left, rightSide));
            return result;
        }

        NonTerminal currentLeft = left;
        int suffix = Math.max(1, startCounter);

        for (int i = 0; i < rightSide.size() - 2; i++) {
            // Create unique intermediate variable name without lambdas
            String base = "X_" + i;
            String newVarName = base + "_" + (suffix++);
            while (nameExists(variables, newVarName)) {
                newVarName = base + "_" + (suffix++);
            }

            NonTerminal newVar = new NonTerminal(newVarName);
            variables.add(newVar);

            // CurrentLeft -> Symbol[i] NewVar
            result.add(new Production(currentLeft,
                    Arrays.asList(rightSide.get(i), newVar)));

            currentLeft = newVar;
        }

        // LastNewVar -> Symbol[n-2] Symbol[n-1]
        result.add(new Production(currentLeft, Arrays.asList(
                rightSide.get(rightSide.size() - 2),
                rightSide.get(rightSide.size() - 1))));

        return result;
    }

    /**
     * Removes duplicate productions from the list.
     */
    private List<Production> removeDuplicateProductions(List<Production> productions) {
        Set<String> seen = new HashSet<>();
        List<Production> unique = new ArrayList<>();

        for (Production p : productions) {
            String key = p.getLeft().getName() + "->" +
                    p.getRight().stream().map(Symbol::getName).collect(Collectors.joining(""));

            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(p);
            }
        }

        return unique;
    }

    // Automaton abstract method implementations
    @Override
    public ParseResult parse(String inputText) {
        List<ValidationMessage> messages = new ArrayList<>();
        try {
            CFG parsedCFG = parseFromString(inputText);

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
        StringBuilder trace = new StringBuilder();
        trace.append("CFG execution trace for input: \"").append(inputText).append("\"\n\n");

        boolean accepted = false;

        try {
            // Validate grammar first
            if (startSymbol == null) {
                messages.add(new ValidationMessage("No start symbol defined", 0, ValidationMessage.ValidationMessageType.ERROR));
                trace.append("ERROR: No start symbol defined\n");
                return new ExecutionResult(false, messages, trace.toString());
            }

            if (productions.isEmpty()) {
                messages.add(new ValidationMessage("No productions defined", 0, ValidationMessage.ValidationMessageType.ERROR));
                trace.append("ERROR: No productions defined\n");
                return new ExecutionResult(false, messages, trace.toString());
            }

            // Convert to CNF first
            trace.append("Converting grammar to Chomsky Normal Form...\n");
            CFG cnfGrammar = this.toChomskyNormalForm();
            trace.append("CNF conversion completed.\n");
            trace.append("Original productions: ").append(this.productions.size()).append("\n");
            trace.append("CNF productions: ").append(cnfGrammar.getProductions().size()).append("\n\n");

            // Handle empty string case
            if (inputText == null || inputText.isEmpty()) {
                trace.append("Input is empty string (ε)\n");
                trace.append("Checking if start symbol can derive ε...\n");

                // Check if original start symbol has epsilon production
                for (Production p : this.productions) {
                    if (p.getLeft().equals(this.startSymbol) && p.getRight().isEmpty()) {
                        accepted = true;
                        trace.append("Found epsilon production: ").append(this.startSymbol.getName()).append(" -> ε\n");
                        break;
                    }
                }

                if (!accepted) {
                    trace.append("No epsilon production found for start symbol\n");
                }

                trace.append("Result: ").append(accepted ? "ACCEPTED" : "REJECTED").append("\n");
                return new ExecutionResult(accepted, messages, trace.toString());
            }

            trace.append("Input length: ").append(inputText.length()).append("\n");
            trace.append("Start symbol: ").append(cnfGrammar.getStartSymbol().getName()).append("\n\n");

            // Validate that all characters in input are terminals
            Set<String> terminalNames = terminals.stream()
                    .map(Terminal::getName)
                    .collect(Collectors.toSet());

            trace.append("Validating input characters...\n");
            for (char c : inputText.toCharArray()) {
                String charStr = String.valueOf(c);
                if (!terminalNames.contains(charStr)) {
                    messages.add(new ValidationMessage("Character '" + c + "' is not a terminal in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
                    trace.append("ERROR: Character '").append(c).append("' is not defined as a terminal\n");
                    return new ExecutionResult(false, messages, trace.toString());
                }
            }
            trace.append("All input characters are valid terminals\n\n");

            // Use CYK algorithm on CNF grammar
            accepted = cnfGrammar.cykParse(inputText, trace);

            trace.append("\nFinal Result: ").append(accepted ? "ACCEPTED" : "REJECTED").append("\n");

            if (accepted) {
                trace.append("The string can be derived from the grammar\n");
            } else {
                trace.append("The string cannot be derived from the grammar\n");
            }

        } catch (Exception e) {
            messages.add(new ValidationMessage("Execution error: " + e.getMessage(), 0, ValidationMessage.ValidationMessageType.ERROR));
            trace.append("ERROR during execution: ").append(e.getMessage()).append("\n");
            return new ExecutionResult(false, messages, trace.toString());
        }

        return new ExecutionResult(accepted, messages, trace.toString());
    }

    /**
     * Implements the CYK (Cocke-Younger-Kasami) algorithm for context-free parsing.
     * This algorithm works with grammars in Chomsky Normal Form, but we'll adapt it
     * to work with general CFGs by handling various production forms.
     *
     * @param input the input string to parse
     * @param trace the trace builder for logging
     * @return true if the string can be derived from the grammar, false otherwise
     */
    private boolean cykParse(String input, StringBuilder trace) {
        int n = input.length();
        trace.append("Starting CYK parsing algorithm\n");
        trace.append("Input: \"").append(input).append("\"\n");
        trace.append("Length: ").append(n).append("\n\n");

        // Create CYK table: table[i][j] contains set of non-terminals that can derive substring from i with length j+1
        Set<NonTerminal>[][] table = new Set[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                table[i][j] = new HashSet<>();
            }
        }

        // Phase 1: Fill diagonal (length 1 substrings)
        trace.append("Phase 1: Processing single characters\n");
        for (int i = 0; i < n; i++) {
            char currentChar = input.charAt(i);
            String charStr = String.valueOf(currentChar);
            trace.append("Position ").append(i).append(": '").append(currentChar).append("'\n");

            // Find all productions A -> a where a is the current character
            for (Production p : productions) {
                if (p.getRight().size() == 1 &&
                        p.getRight().get(0) instanceof Terminal &&
                        p.getRight().get(0).getName().equals(charStr)) {

                    table[i][0].add(p.getLeft());
                    trace.append("  Added ").append(p.getLeft().getName())
                            .append(" (from production: ").append(p.getLeft().getName())
                            .append(" -> ").append(charStr).append(")\n");
                }
            }

            // Handle unit productions iteratively
            boolean changed = true;
            while (changed) {
                changed = false;
                Set<NonTerminal> newSymbols = new HashSet<>();

                for (NonTerminal nt : table[i][0]) {
                    // Look for unit productions A -> B where B is already in the set
                    for (Production p : productions) {
                        if (p.getRight().size() == 1 &&
                                p.getRight().get(0) instanceof NonTerminal &&
                                p.getRight().get(0).equals(nt) &&
                                !table[i][0].contains(p.getLeft())) {

                            newSymbols.add(p.getLeft());
                            trace.append("  Added ").append(p.getLeft().getName())
                                    .append(" (unit production: ").append(p.getLeft().getName())
                                    .append(" -> ").append(nt.getName()).append(")\n");
                            changed = true;
                        }
                    }
                }
                table[i][0].addAll(newSymbols);
            }

            if (table[i][0].isEmpty()) {
                trace.append("  No productions found for '").append(currentChar).append("'\n");
            }
            trace.append("\n");
        }

        // Phase 2: Fill table for substrings of length 2 to n
        trace.append("Phase 2: Processing longer substrings\n");
        for (int length = 2; length <= n; length++) {
            trace.append("Processing substrings of length ").append(length).append(":\n");

            for (int i = 0; i <= n - length; i++) {
                int j = length - 1; // j represents length - 1 for 0-based indexing
                String substring = input.substring(i, i + length);
                trace.append("  Substring [").append(i).append(",").append(i + length - 1)
                        .append("]: \"").append(substring).append("\"\n");

                // Try all possible splits of the substring
                for (int k = 0; k < length - 1; k++) {
                    Set<NonTerminal> leftSet = table[i][k];
                    Set<NonTerminal> rightSet = table[i + k + 1][j - k - 1];

                    if (!leftSet.isEmpty() && !rightSet.isEmpty()) {
                        trace.append("    Split at position ").append(k + 1)
                                .append(": \"").append(input.substring(i, i + k + 1))
                                .append("\" | \"").append(input.substring(i + k + 1, i + length))
                                .append("\"\n");

                        // Check all combinations of non-terminals from left and right sets
                        for (NonTerminal left : leftSet) {
                            for (NonTerminal right : rightSet) {
                                // Look for productions A -> BC where B ∈ leftSet and C ∈ rightSet
                                for (Production p : productions) {
                                    if (p.getRight().size() == 2 &&
                                            p.getRight().get(0) instanceof NonTerminal &&
                                            p.getRight().get(1) instanceof NonTerminal &&
                                            p.getRight().get(0).equals(left) &&
                                            p.getRight().get(1).equals(right)) {

                                        if (!table[i][j].contains(p.getLeft())) {
                                            table[i][j].add(p.getLeft());
                                            trace.append("      Added ").append(p.getLeft().getName())
                                                    .append(" (from production: ").append(p.getLeft().getName())
                                                    .append(" -> ").append(left.getName())
                                                    .append(" ").append(right.getName()).append(")\n");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Handle longer productions (A -> BCD, A -> BCDE, etc.)
                // This extends the basic CYK to handle productions with more than 2 symbols
                for (Production p : productions) {
                    if (p.getRight().size() > 2 && p.getRight().size() == length) {
                        boolean canDerive = true;
                        trace.append("    Checking production: ").append(p.getLeft().getName())
                                .append(" -> ");
                        for (Symbol s : p.getRight()) {
                            trace.append(s.getName()).append(" ");
                        }
                        trace.append("\n");

                        // Check if each symbol in the production can derive the corresponding part of the input
                        for (int symbolIndex = 0; symbolIndex < p.getRight().size(); symbolIndex++) {
                            Symbol symbol = p.getRight().get(symbolIndex);
                            char inputChar = input.charAt(i + symbolIndex);

                            if (symbol instanceof Terminal) {
                                if (!symbol.getName().equals(String.valueOf(inputChar))) {
                                    canDerive = false;
                                    break;
                                }
                            } else if (symbol instanceof NonTerminal) {
                                if (!table[i + symbolIndex][0].contains(symbol)) {
                                    canDerive = false;
                                    break;
                                }
                            }
                        }

                        if (canDerive) {
                            table[i][j].add(p.getLeft());
                            trace.append("      Added ").append(p.getLeft().getName())
                                    .append(" (from long production)\n");
                        }
                    }
                }

                // Handle unit productions for the current cell
                boolean changed = true;
                while (changed) {
                    changed = false;
                    Set<NonTerminal> newSymbols = new HashSet<>();

                    for (NonTerminal nt : table[i][j]) {
                        for (Production p : productions) {
                            if (p.getRight().size() == 1 &&
                                    p.getRight().get(0) instanceof NonTerminal &&
                                    p.getRight().get(0).equals(nt) &&
                                    !table[i][j].contains(p.getLeft())) {

                                newSymbols.add(p.getLeft());
                                trace.append("      Added ").append(p.getLeft().getName())
                                        .append(" (unit production: ").append(p.getLeft().getName())
                                        .append(" -> ").append(nt.getName()).append(")\n");
                                changed = true;
                            }
                        }
                    }
                    table[i][j].addAll(newSymbols);
                }

                if (table[i][j].isEmpty()) {
                    trace.append("    No non-terminals can derive this substring\n");
                }
                trace.append("\n");
            }
        }

        // Check if start symbol can derive the entire string
        boolean result = table[0][n-1].contains(startSymbol);

        trace.append("CYK Table Summary:\n");
        for (int length = 1; length <= n; length++) {
            for (int i = 0; i <= n - length; i++) {
                int j = length - 1;
                String substring = input.substring(i, i + length);
                Set<NonTerminal> symbols = table[i][j];

                if (!symbols.isEmpty()) {
                    trace.append("  \"").append(substring).append("\" can be derived by: ")
                            .append(symbols.stream()
                                    .map(NonTerminal::getName)
                                    .collect(Collectors.joining(", ")))
                            .append("\n");
                }
            }
        }

        trace.append("\nStart symbol ").append(startSymbol.getName())
                .append(result ? " CAN" : " CANNOT")
                .append(" derive the entire input string\n");

        return result;
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

    @Override
    public String getDefaultTemplate() {
        return "Variables = S A B\n" +
               "Terminals = a b\n" +
               "Start = S\n" +
               "\n" +
               "S -> A B\n" +
               "A -> a\n" +
               "B -> b\n";
    }
}