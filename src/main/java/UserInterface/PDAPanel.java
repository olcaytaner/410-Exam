package UserInterface;

import common.Automaton;

/**
 * PDA (Push-down Automaton) panel implementation.
 * Extends AbstractAutomatonPanel to eliminate code duplication.
 */
public class PDAPanel extends AbstractAutomatonPanel {

    public PDAPanel(MainPanel mainPanel, Automaton automaton) {
        super(mainPanel, automaton);
    }

    @Override
    protected String getTabLabelText() {
        return "PDA Tab";
    }

    @Override
    protected boolean showMaxTransitionsField() {
        return true;
    }
}