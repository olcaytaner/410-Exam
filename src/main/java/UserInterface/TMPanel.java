package UserInterface;

import common.Automaton;

/**
 * TM (Turing Machine) panel implementation.
 * Extends AbstractAutomatonPanel to eliminate code duplication.
 */
public class TMPanel extends AbstractAutomatonPanel {

    public TMPanel(MainPanel mainPanel, Automaton automaton) {
        super(mainPanel, automaton);
    }

    @Override
    protected String getTabLabelText() {
        return "TM Tab";
    }
}