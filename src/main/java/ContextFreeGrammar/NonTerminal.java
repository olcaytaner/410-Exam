package ContextFreeGrammar;

/**
 * Represents a non-terminal symbol in a Context-Free Grammar.
 * Non-terminals are variables that can be replaced by other symbols
 * according to the production rules. They are typically represented
 * by uppercase letters.
 *
 * @author yenennn
 * @version 1.0
 */
public class NonTerminal extends Symbol {

    /**
     * Constructs a new non-terminal symbol with the specified name.
     *
     * @param name the name of the non-terminal (should be uppercase)
     */
    public NonTerminal(String name) {
        super(name);
    }
}