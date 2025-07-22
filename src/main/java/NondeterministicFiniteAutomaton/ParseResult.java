package NondeterministicFiniteAutomaton;

import java.util.List;

public class ParseResult {
    private NFA nfa;
    private List<Warning> warnings;
    private int errorCount;
    private int warnCount;

    public ParseResult(NFA nfa, List<Warning> warnings) {
        this.nfa = nfa;
        this.warnings = warnings;

        errorCount = 0;
        warnCount = 0;

        for (Warning warning : warnings) {
            if (warning.getLevel() == Warning.ERROR){
                errorCount++;
            } else if (warning.getLevel() == Warning.WARN) {
                warnCount++;
            }
        }

    }

    public NFA getNfa() {
        return nfa;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarnCount() {
        return warnCount;
    }
}
