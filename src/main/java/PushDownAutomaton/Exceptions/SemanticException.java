package PushDownAutomaton.Exceptions;

public class SemanticException extends ParserException {
    public SemanticException(String message, int lineNumber) {
        super(message, lineNumber);
    }
}