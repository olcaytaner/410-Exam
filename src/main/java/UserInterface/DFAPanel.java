package UserInterface;

import common.Automaton;

/**
 * DFA (Deterministic Finite Automaton) panel implementation.
 * Extends AbstractAutomatonPanel to eliminate code duplication.
 */
public class DFAPanel extends AbstractAutomatonPanel {

    public DFAPanel(MainPanel mainPanel, Automaton automaton) {
        super(mainPanel, automaton);
    }

    @Override
    protected String getTabLabelText() {
        return "DFA Tab";
    }
}
 