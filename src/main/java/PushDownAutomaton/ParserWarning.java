package PushDownAutomaton;

public class ParserWarning {
    private final String message;
    private final int lineNumber;

    public ParserWarning(String message, int lineNumber) {
        this.message = message;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        if (lineNumber > 0) {
            return "WARNING (Line " + lineNumber + "): " + message;
        }
        return "WARNING: " + message;
    }
}