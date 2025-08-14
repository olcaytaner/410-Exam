package UserInterface;

import common.Automaton;

/**
 * NFA (Nondeterministic Finite Automaton) panel implementation.
 * Extends AbstractAutomatonPanel to eliminate code duplication.
 */
public class NFAPanel extends AbstractAutomatonPanel {

    public NFAPanel(MainPanel mainPanel, Automaton automaton) {
        super(mainPanel, automaton);
    }

    @Override
    protected String getTabLabelText() {
        return "NFA Tab";
    }
}