package PushDownAutomaton.Exceptions;

public class SyntaxException extends ParserException {
    public SyntaxException(String message, int lineNumber) {
        super(message, lineNumber);
    }
}
