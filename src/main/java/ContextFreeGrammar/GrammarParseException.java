package ContextFreeGrammar;

/**
 * Custom exception class for handling grammar parsing errors.
 * This exception is thrown when errors occur during the parsing of Context-Free Grammar files,
 * such as invalid syntax, missing components, or malformed production rules.
 *
 * @author yenennn
 * @version 1.0
 */
public class GrammarParseException extends RuntimeException {

    /**
     * Constructs a new GrammarParseException with the specified error message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public GrammarParseException(String message) {
        super(message);
    }
}
