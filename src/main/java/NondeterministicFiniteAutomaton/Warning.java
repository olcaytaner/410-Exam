package NondeterministicFiniteAutomaton;

public class Warning {

    private String message;
    private int level;
    private int lineNo;
    private int lineName;

    public static final int WARN = 0;
    public static final int ERROR = 1;

    public static final int START_LINE = 0;
    public static final int FINALS_LINE = 1;
    public static final int ALPHABET_LINE = 2;
    public static final int TRANS_LINE = 3;
    public static final int OTHER = 4;

    public Warning(String message, int level, int lineNo,  int lineName) {
        this.message = message;
        this.level = level;
        this.lineNo = lineNo;
        this.lineName = lineName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public int getLineName() {
        return lineName;
    }

    public void setLineName(int lineName) {
        this.lineName = lineName;
    }

    @Override
    public String toString() {
        String s = level == 1 ? "Error" : "Warning";
        String name = "";
        if (lineName == START_LINE) {
            name += "Start Line";
        }
        if (lineName == FINALS_LINE) {
            name += "Final Line";
        }
        if (lineName == ALPHABET_LINE) {
            name += "Alphabet Line";
        }
        if (lineName == TRANS_LINE) {
            name += "Transition Line";
        }
        if (lineName == OTHER) {
            name += "Unknown Line";
        }
        return "Warning: " + "\n" +
                "   Message: " + message + '\n' +
                "   Level: " + s + "\n" +
                "   LineNo: " + lineNo + "\n" +
                "   LineName: " + name;
    }
}
