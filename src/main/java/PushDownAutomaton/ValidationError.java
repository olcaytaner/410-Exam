package PushDownAutomaton;

public class ValidationError {
    private ErrorType type;
    private String message;
    private int lineNumber;

    public ValidationError(ErrorType type, String message, int lineNumber) {
        this.type = type;
        this.message = message;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return type.name() + " (Satır " + lineNumber + "): " + message;
    }
}