package TuringMachine;

public class Issue {
    public final int line;
    public final Level level;
    public final String code;
    public final String message;

    public Issue(int line, Level level, String code, String message) {
        this.line = line;
        this.level = level;
        this.code = code;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public Level getLevel() {
        return level;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Line %d | %-7s | %-25s | %s",
                line, level, code, message);
    }
}

