package ContextFreeGrammar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parser for Context-Free Grammar files.
 * This class provides static methods to parse CFG definition files and convert them
 * into CFG objects. The parser expects files in a specific format with sections for
 * variables, terminals, start symbol, and production rules.
 *
 * @author yenennn
 * @version 1.0
 */
public class CFGParser {

    /** Maximum number of lines allowed in a grammar file to prevent infinite loops */
    private static final int MAX_LINES = 200;

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
    public static CFG parse(String filePath) throws IOException {
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
     * Also handles the special case of epsilon ("eps").
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
        if (name.equals("eps")) {
            return new Terminal("eps");
        }

        return null;
    }
}