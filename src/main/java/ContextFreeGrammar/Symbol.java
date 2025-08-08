package ContextFreeGrammar;

/**
 * Abstract base class for grammar symbols.
 * This class serves as the parent for both terminal and non-terminal symbols
 * in a Context-Free Grammar, providing common functionality for symbol naming.
 *
 * @author yenennn
 * @version 1.0
 */
public class Symbol {
    /** The name of the symbol */
    protected String name;

    /**
     * Constructs a new symbol with the specified name.
     *
     * @param name the name of the symbol
     */
    public Symbol(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this symbol.
     *
     * @return the symbol's name
     */
    public String getName() {
        return name;
    }
}