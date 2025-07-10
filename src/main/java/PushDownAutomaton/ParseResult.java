package PushDownAutomaton;

import java.util.List;

public class ParseResult {
    private final PDA pda;
    private final List<ParserWarning> warnings;

    public ParseResult(PDA pda, List<ParserWarning> warnings) {
        this.pda = pda;
        this.warnings = warnings;
    }

    public PDA getPda() {
        return pda;
    }

    public List<ParserWarning> getWarnings() {
        return warnings;
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}