package PushDownAutomaton.Exceptions;


public class ParserException extends Exception {
    private int lineNumber;

    public ParserException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        if (lineNumber > 0) {
            return "ERROR (Line " + lineNumber + "): " + super.getMessage();
        }
        return "ERROR: " + super.getMessage();
    }
}
